package remote.api.interceptors

import com.bbuddies.madafaker.common_domain.auth.TokenRefreshService
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val tokenRefreshService: TokenRefreshService
) : Interceptor {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private val EMPTY_RESPONSE_BODY = okhttp3.ResponseBody.create(null, "")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authToken = preferenceManager.firebaseIdToken.value

        // Add auth token to request if available
        val requestWithAuth = if (authToken != null) {
            originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$authToken")
                .build()
        } else {
            originalRequest
        }

        // Execute the request
        val response = chain.proceed(requestWithAuth)

        // Handle 401 Unauthorized responses
        if (response.code == 401 && authToken != null) {
            return handleUnauthorizedResponse(chain, originalRequest, response)
        }

        return response
    }

    /**
     * Handles 401 Unauthorized responses by attempting to refresh the Firebase ID token
     * and retrying the original request.
     * 
     * Note: Uses runBlocking for token refresh operations because OkHttp Interceptor.intercept()
     * is synchronous and cannot be made suspend. This is a known limitation, but the impact is minimal:
     * - Token refresh only happens on 401 errors (rare after proactive refresh)
     * - OkHttp uses a thread pool that can handle blocking operations
     * - The alternative (failing immediately) would be worse for UX
     */
    private fun handleUnauthorizedResponse(
        chain: Interceptor.Chain,
        originalRequest: okhttp3.Request,
        originalResponse: Response
    ): Response {
        Timber.w("Received 401 Unauthorized, attempting token refresh")

        return try {
            // Close the original response to free resources
            originalResponse.close()

            // Check if user is still signed in to Firebase before attempting refresh
            if (!tokenRefreshService.isSignedIn()) {
                Timber.e("User is not signed in to Firebase - cannot refresh token")
                runBlocking {
                    try {
                        preferenceManager.clearUserData()
                    } catch (clearException: Exception) {
                        Timber.e(clearException, "Failed to clear auth data")
                    }
                }
                return createUnauthorizedResponse(originalRequest, "Firebase session expired")
            }

            // Attempt to refresh the token using TokenRefreshService with force refresh
            // runBlocking is necessary here as OkHttp interceptors are synchronous
            val newToken = runBlocking {
                val refreshedToken = tokenRefreshService.refreshFirebaseIdToken(forceRefresh = true)
                // Update the stored token
                preferenceManager.updateFirebaseIdToken(refreshedToken)
                Timber.d("Firebase ID token refreshed and stored successfully")
                refreshedToken
            }

            // Retry the original request with the new token
            val retryRequest = originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$newToken")
                .build()

            val retryResponse = chain.proceed(retryRequest)

            if (retryResponse.code == 401) {
                // If retry also fails with 401, clear auth data
                Timber.e("Token refresh succeeded but retry request still returned 401 - clearing auth data")
                runBlocking {
                    try {
                        preferenceManager.clearUserData()
                    } catch (clearException: Exception) {
                        Timber.e(clearException, "Failed to clear auth data")
                    }
                }
            } else {
                Timber.d("Request retry with refreshed token succeeded")
            }

            retryResponse

        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh token, clearing auth data")

            // Handle auth failure (clear auth data)
            runBlocking {
                try {
                    preferenceManager.clearUserData()
                } catch (clearException: Exception) {
                    Timber.e(clearException, "Failed to clear auth data")
                }
            }

            createUnauthorizedResponse(originalRequest, "Token refresh failed")
        }
    }

    /**
     * Creates a new 401 Unauthorized response with a custom message.
     * Helper method to avoid code duplication.
     */
    private fun createUnauthorizedResponse(request: okhttp3.Request, reason: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized - $reason")
            .body(EMPTY_RESPONSE_BODY)
            .build()
    }
}