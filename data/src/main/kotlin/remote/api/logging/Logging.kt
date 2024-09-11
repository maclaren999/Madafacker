package remote.api.logging

import android.content.Context
import com.chuckerteam.chucker.api.BodyDecoder
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.logging.HttpLoggingInterceptor


/**
 * Configures and creates a ChuckerInterceptor.
 *
 * @param context An Android [Context].
 * @param decoder A [BodyDecoder] to be added to the interceptor.
 * @return A configured [ChuckerInterceptor] instance.
 */
fun createChuckerInterceptor(context: Context, decoder: BodyDecoder? = null): ChuckerInterceptor {
    // Create the Collector
    val chuckerCollector = ChuckerCollector(
        context = context,
        // Toggles visibility of the notification
        showNotification = true,
        // Allows to customize the retention period of collected data
        retentionPeriod = RetentionManager.Period.ONE_HOUR
    )

    // Create the Interceptor
    return ChuckerInterceptor.Builder(context).apply {
        // The previously created Collector
        collector(chuckerCollector)
        // The max body content length in bytes, after this responses will be truncated.
        maxContentLength(250_000L)
        // List of headers to replace with ** in the Chucker UI
        redactHeaders("Auth-Token", "Bearer")
        // Read the whole response body even when the client does not consume the response completely.
        // This is useful in case of parsing errors or when the response body
        // is closed before being read like in Retrofit with Void and Unit types.
        alwaysReadResponseBody(true)
        // Use decoder when processing request and response bodies. When multiple decoders are installed they
        // are applied in an order they were added.
        if (decoder != null) addBodyDecoder(decoder)
        // Controls Android shortcut creation.
        createShortcut(true)
    }.build()
}

/**
 * Logging interceptor for OkHttp.
 * */
fun loggingInterceptor() = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}