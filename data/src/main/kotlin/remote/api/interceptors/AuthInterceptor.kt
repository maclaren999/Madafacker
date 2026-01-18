package remote.api.interceptors

import com.bbuddies.madafaker.common_domain.auth.TokenRefreshService
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "AUTH_INTERCEPTOR"

/**
 * OkHttp Interceptor that handles authentication.
 *
 * Strategy:
 * 1. Adds Firebase ID token to all requests
 * 2. On 401: attempts token refresh and retry
 * 3. On retry 401: logs error but does NOT clear session (let the app handle it)
 * 4. Token refresh failures are logged but don't block the request
 */
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
        val requestUrl = originalRequest.url.encodedPath

        Timber.tag(TAG).d("Request: ${originalRequest.method} $requestUrl (hasToken=${authToken != null})")

        // Add auth token to request if available
        val requestWithAuth = if (authToken != null) {
            originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$authToken")
                .build()
        } else {
            Timber.tag(TAG).w("No auth token available for request: $requestUrl")
            originalRequest
        }

        // Execute the request
        val response = chain.proceed(requestWithAuth)

        Timber.tag(TAG).d("Response: ${response.code} for $requestUrl")

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
     * Important: We do NOT clear the session on 401 - that's handled by the UserRepository
     * based on overall auth state. This interceptor just handles token refresh.
     */
    private fun handleUnauthorizedResponse(
        chain: Interceptor.Chain,
        originalRequest: okhttp3.Request,
        originalResponse: Response
    ): Response {
        val requestUrl = originalRequest.url.encodedPath
        Timber.tag(TAG).w("Received 401 for $requestUrl - attempting token refresh")

        return try {
            // Close the original response to free resources
            originalResponse.close()

            // Check if Firebase has a user before trying to refresh
            if (!tokenRefreshService.hasFirebaseUser()) {
                Timber.tag(TAG).e("Cannot refresh token - no Firebase user")
                // Return a new response with the same error
                // Don't clear session here - let the repo handle it
                return chain.proceed(originalRequest) // Re-try without token refresh
            }

            // Attempt to refresh the token using TokenRefreshService
            val newToken = runBlocking {
                Timber.tag(TAG).d("Refreshing Firebase token...")
                val refreshedToken = tokenRefreshService.refreshFirebaseIdToken()
                // Update the stored token
                preferenceManager.updateFirebaseIdToken(refreshedToken)
                Timber.tag(TAG).d("Token refresh successful (length=${refreshedToken.length})")
                refreshedToken
            }

            // Retry the original request with the new token
            val retryRequest = originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$newToken")
                .build()

            val retryResponse = chain.proceed(retryRequest)
            Timber.tag(TAG).d("Retry response: ${retryResponse.code} for $requestUrl")

            if (retryResponse.code == 401) {
                // Token refresh worked but server still returns 401
                // This is a real auth problem - but don't clear session here
                // The app will detect the issue via the error response
                Timber.tag(TAG).e("Token refresh succeeded but retry still got 401 - auth may be invalid")
            }

            retryResponse

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Token refresh failed for $requestUrl")

            // Token refresh failed - return original 401 response
            // Don't clear session here - transient errors (network, Firebase issues) shouldn't log out user
            // The UserRepository will handle persistent auth failures appropriately

            // Re-execute request to return a valid response
            try {
                chain.proceed(originalRequest)
            } catch (retryException: Exception) {
                Timber.tag(TAG).e(retryException, "Failed to re-execute request after token refresh failure")
                // If even that fails, we have no response to return
                // This is a network error, not an auth error
                throw retryException
            }
        }
    }
}