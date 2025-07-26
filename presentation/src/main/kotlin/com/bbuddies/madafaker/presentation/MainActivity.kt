package com.bbuddies.madafaker.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.utils.SharedTextManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sharedTextManager: SharedTextManager

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle shared text from external apps
        handleSharedText(intent)

//        setupGoogleAuth()

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Make status bar and navigation bar transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val deepLinkData = remember { mutableStateOf<DeepLinkData?>(null) }
            val currentMode by preferenceManager.currentMode.collectAsState()

            // Handle notification deep link
            LaunchedEffect(Unit) {
                handleNotificationIntent(intent, deepLinkData)
            }

            MadafakerTheme(mode = currentMode) {
                // Main content - background is handled by individual screens
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    deepLinkData = deepLinkData.value
                )
            }
        }
    }

    private fun handleNotificationIntent(
        intent: Intent,
        deepLinkData: androidx.compose.runtime.MutableState<DeepLinkData?>
    ) {
        val messageId = intent.getStringExtra("message_id")
        val notificationId = intent.getStringExtra("notification_id")
        val modeString = intent.getStringExtra("mode")

        if (messageId != null && notificationId != null && modeString != null) {
            val mode = Mode.valueOf(modeString)

            // Set deep link data for navigation
            deepLinkData.value = DeepLinkData(
                messageId = messageId,
                notificationId = notificationId,
                mode = mode
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle shared text when app is already running (singleTop launch mode)
        handleSharedText(intent)
    }

    /**
     * Handles shared text from external apps via ACTION_SEND intent.
     * Extracts text from intent extras and passes it to SharedTextManager.
     */
    private fun handleSharedText(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                Timber.d("Received shared text: ${sharedText.take(50)}...")
                sharedTextManager.setSharedText(sharedText)
            } else {
                Timber.w("Received ACTION_SEND intent but no text found")
            }
        }
    }

}

data class DeepLinkData(
    val messageId: String,
    val notificationId: String,
    val mode: Mode
)

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MadafakerTheme(Mode.SHINE) {
        // Preview content
    }
}