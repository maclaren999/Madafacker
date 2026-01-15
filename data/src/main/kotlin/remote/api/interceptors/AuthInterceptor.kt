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
                // Return a new 401 response since original was closed
                return Response.Builder()
                    .request(originalRequest)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized - Firebase session expired")
                    .body(okhttp3.ResponseBody.create(null, ByteArray(0)))
                    .build()
            }

            // Attempt to refresh the token using TokenRefreshService with force refresh
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
                    // Ensure original response is closed if not already
                    if (!originalResponse.body?.contentLength().let { it == null || it == 0L }) {
                        originalResponse.close()
                    }
                    
                    preferenceManager.clearUserData()
                } catch (clearException: Exception) {
                    Timber.e(clearException, "Failed to clear auth data")
                }
            }

            // Return a new 401 response
            Response.Builder()
                .request(originalRequest)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized - Token refresh failed")
                .body(okhttp3.ResponseBody.create(null, ByteArray(0)))
                .build()
        }
    }
}