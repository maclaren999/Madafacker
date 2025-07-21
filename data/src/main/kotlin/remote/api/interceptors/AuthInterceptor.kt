package remote.api.interceptors

import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authToken = preferenceManager.firebaseIdToken.value

        val newRequest =
            if (authToken != null)
                originalRequest.newBuilder()
                    .header("Authentication", "Bearer $authToken")
                    .build()
            else
                originalRequest

        return chain.proceed(newRequest)
    }
}