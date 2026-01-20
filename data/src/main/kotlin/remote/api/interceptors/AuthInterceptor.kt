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
 * 1. Adds Firebase ID token to all requests from cached storage
 * 2. On 401: attempts token refresh via Firebase and retry
 * 3. On retry 401: logs error but does NOT clear session (let the app handle it)
 * 4. Token refresh failures are logged but don't block the request
 *
 * IMPORTANT: This interceptor uses cached Firebase ID tokens, NOT Firebase.currentUser.
 * On cold start, Firebase.currentUser may be null, but cached token may still be valid.
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
     *
     * Token refresh flow:
     * 1. Check if Firebase has a user (it might not on cold start!)
     * 2. If yes, refresh token and retry
     * 3. If no, retry with cached token (might work if token not expired)
     * 4. If retry still fails, return the error (UserRepository will handle logout if needed)
     */
    private fun handleUnauthorizedResponse(
        chain: Interceptor.Chain,
        originalRequest: okhttp3.Request,
        originalResponse: Response
    ): Response {
        val requestUrl = originalRequest.url.encodedPath
        Timber.tag(TAG).w("=== Received 401 for $requestUrl ===")
        Timber.tag(TAG).w("Attempting token refresh...")

        return try {
            // Close the original response to free resources
            originalResponse.close()

            // Check if Firebase has a user
            val hasFirebaseUser = tokenRefreshService.hasFirebaseUser()
            Timber.tag(TAG).d("hasFirebaseUser: $hasFirebaseUser")

            if (!hasFirebaseUser) {
                Timber.tag(TAG).w("No Firebase user available for token refresh")
                Timber.tag(TAG).w("This is expected on cold start if Firebase hasn't restored session yet")

                // On cold start, Firebase might not have user yet, but cached token might still work
                // Return the original request (which already has cached token)
                // The caller will handle the 401 appropriately
                return chain.proceed(originalRequest)
            }

            // Attempt to refresh the token using TokenRefreshService
            val newToken = runBlocking {
                Timber.tag(TAG).d("Refreshing Firebase token (forceRefresh=true)...")
                val refreshedToken = tokenRefreshService.refreshFirebaseIdToken(forceRefresh = true)
                // Update the stored token
                preferenceManager.updateFirebaseIdToken(refreshedToken)
                Timber.tag(TAG).d("Token refresh successful (length=${refreshedToken.length})")
                refreshedToken
            }

            // Retry the original request with the new token
            val retryRequest = originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$newToken")
                .build()

            Timber.tag(TAG).d("Retrying request with new token...")
            val retryResponse = chain.proceed(retryRequest)
            Timber.tag(TAG).d("Retry response: ${retryResponse.code} for $requestUrl")

            if (retryResponse.code == 401) {
                // Token refresh worked but server still returns 401
                // This is a real auth problem - the token is valid but server doesn't accept it
                // This might indicate account was deleted, suspended, or server has stale data
                Timber.tag(TAG).e("=== CRITICAL: Token refresh succeeded but retry still got 401 ===")
                Timber.tag(TAG).e("Token was refreshed successfully but server still rejects it")
                Timber.tag(TAG).e("This might indicate: account deleted, suspended, or server issue")
            }

            retryResponse

        } catch (e: IllegalStateException) {
            // This is thrown by refreshFirebaseIdToken when no user is signed in
            Timber.tag(TAG).e(e, "Token refresh failed: no Firebase user signed in")

            // Re-execute request without token refresh - let the 401 propagate
            try {
                chain.proceed(originalRequest)
            } catch (retryException: Exception) {
                Timber.tag(TAG).e(retryException, "Failed to re-execute request")
                throw retryException
            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Token refresh failed for $requestUrl: ${e.message}")

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