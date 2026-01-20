package com.bbuddies.madafaker.presentation.ui.splash

import android.content.Context
import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.auth.FirebaseAuthStatus
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.auth.GoogleAuthManager
import com.bbuddies.madafaker.presentation.auth.SilentReauthResult
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.usecase.GetNextScreenAfterLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "SPLASH_VM"

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getNextScreenAfterLoginUseCase: GetNextScreenAfterLoginUseCase,
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager,
    private val googleAuthManager: GoogleAuthManager
) : BaseViewModel() {

    private val _navigationEvent = MutableStateFlow<SplashNavigationDestination?>(null)
    val navigationEvent: StateFlow<SplashNavigationDestination?> = _navigationEvent

    val animationState = MutableTransitionState(false)
    private var hasStarted = false

    fun start(context: Context) {
        if (hasStarted) return
        hasStarted = true
        Timber.tag(TAG).d("SplashViewModel start")
        viewModelScope.launch {
            try {
                val forceAuth = attemptReauthIfNeeded(context)
                Timber.tag(TAG).d("Determining next screen...")
                val nextScreen = if (forceAuth) {
                    SplashNavigationDestination.Auth
                } else {
                    getNextScreenAfterLoginUseCase()
                }
                Timber.tag(TAG).d("Next screen determined: $nextScreen")
                _navigationEvent.value = nextScreen
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error determining next screen, defaulting to Auth")
                _navigationEvent.value = SplashNavigationDestination.Auth
            }
        }
    }

    private suspend fun attemptReauthIfNeeded(context: Context): Boolean {
        val sessionActive = preferenceManager.isSessionActive.value
        if (!sessionActive) return false

        val firebaseStatus = googleAuthManager.awaitInitialization(timeoutMs = 5000)
        if (firebaseStatus == FirebaseAuthStatus.SignedIn) return false

        Timber.tag(TAG).w("Firebase SignedOut with cached session - attempting silent reauth")
        return when (val silentResult = googleAuthManager.trySilentReauth(context)) {
            is SilentReauthResult.Success -> {
                storeSessionAndSyncUser(
                    googleIdToken = silentResult.googleIdToken,
                    googleUserId = silentResult.googleUserId,
                    firebaseIdToken = silentResult.firebaseIdToken,
                    firebaseUid = silentResult.firebaseUid
                )
                false
            }

            is SilentReauthResult.Failure -> {
                Timber.tag(TAG).w("Silent reauth failed (${silentResult.reason}) - starting interactive prompt")
                val interactiveSuccessful = performInteractiveReauth(context)
                if (!interactiveSuccessful) {
                    userRepository.clearAllUserData()
                    true
                } else {
                    false
                }
            }
        }
    }

    private suspend fun performInteractiveReauth(context: Context): Boolean {
        return try {
            val response = googleAuthManager.performGoogleAuthentication(context) ?: return false
            val googleAuthResult = googleAuthManager.extractAndStoreCredentials(response) ?: return false

            val firebaseUser = googleAuthManager.firebaseAuthWithGoogle(googleAuthResult.idToken)
                ?: return false
            val firebaseIdToken = firebaseUser.getIdToken(true).await()?.token ?: return false

            storeSessionAndSyncUser(
                googleIdToken = googleAuthResult.idToken,
                googleUserId = googleAuthResult.googleUserId,
                firebaseIdToken = firebaseIdToken,
                firebaseUid = firebaseUser.uid
            )
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Interactive reauth failed")
            false
        }
    }

    private suspend fun storeSessionAndSyncUser(
        googleIdToken: String,
        googleUserId: String,
        firebaseIdToken: String,
        firebaseUid: String
    ) {
        userRepository.storeGoogleAuth(
            googleIdToken,
            googleUserId,
            firebaseIdToken,
            firebaseUid
        )

        try {
            userRepository.authenticateWithGoogle(googleIdToken, googleUserId)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to sync user after reauth")
        }
    }
}
