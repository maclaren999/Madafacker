package remote.api.interceptors

import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authToken = runBlocking { preferenceManager.authToken.lastOrNull() }

        val newRequest =
            if (authToken != null)
                originalRequest.newBuilder()
                    .header("token", authToken)
                    .build()
            else
                originalRequest

        return chain.proceed(newRequest)
    }
}