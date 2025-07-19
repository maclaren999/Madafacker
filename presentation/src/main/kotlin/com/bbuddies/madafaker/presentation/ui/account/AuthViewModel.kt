package com.bbuddies.madafaker.presentation.ui.account

import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.auth.GoogleAuthManager
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.MfResult
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class AuthUiState {
    INITIAL,           // Show welcome + Google Sign-In
    POST_GOOGLE_AUTH,  // Show nickname input for new users
    LOADING           // Show loading states
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationPermissionHelper: NotificationPermissionHelper,
    private val googleAuthManager: GoogleAuthManager
) : BaseViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState.INITIAL)
    val authUiState: StateFlow<AuthUiState> = _authUiState

    private val _draftNickname = MutableStateFlow("")
    val draftNickname: StateFlow<String> = _draftNickname

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn

    private val draftValidator = NicknameDraftValidator(userRepository, viewModelScope)
    val nicknameDraftValidationResult = draftValidator.validationResult

    fun onDraftNickChanged(newNickname: String) {
        _draftNickname.value = newNickname
        draftValidator.onDraftNickChanged(newNickname)
    }

    fun onGoogleSignIn(onSuccessfulSignIn: (NotificationPermissionHelper) -> Unit) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authUiState.value = AuthUiState.LOADING

            try {
                // Perform Google authentication
                val googleResult = googleAuthManager.performGoogleAuthentication()

                if (googleResult != null) {
                    // Extract and store credentials
                    val authResult = googleAuthManager.extractAndStoreCredentials(googleResult)

                    if (authResult != null) {
                        // Authenticate with Firebase using Google ID token
                        val firebaseUser = googleAuthManager.firebaseAuthWithGoogle(authResult.idToken)

                        if (firebaseUser != null) {
                            // Store Google auth and check if user exists
                            userRepository.storeGoogleAuth(authResult.idToken, authResult.googleUserId)

                            try {
                                // Try to authenticate existing user
                                userRepository.authenticateWithGoogle(authResult.idToken, authResult.googleUserId)
                                // User exists, proceed to main screen
                                onSuccessfulSignIn(notificationPermissionHelper)
                            } catch (e: Exception) {
                                // User doesn't exist, show nickname input
                                _authUiState.value = AuthUiState.POST_GOOGLE_AUTH
                            }
                        } else {
                            _warningsFlow.emit { "Firebase authentication failed" }
                            _authUiState.value = AuthUiState.INITIAL
                        }
                    } else {
                        _warningsFlow.emit { "Failed to process Google authentication" }
                        _authUiState.value = AuthUiState.INITIAL
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Google Sign-In failed")
                _warningsFlow.emit { "Google Sign-In failed: ${e.localizedMessage}" }
                _authUiState.value = AuthUiState.INITIAL
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun onCreateAccount(onSuccessfulCreation: (NotificationPermissionHelper) -> Unit) {
        viewModelScope.launch {
            _isSigningIn.value = true

            try {
                val nickname = _draftNickname.value.trim()

                if (nickname.isEmpty()) {
                    _warningsFlow.emit { "Please enter a nickname" }
                    return@launch
                }

                // Validate nickname
                if (nicknameDraftValidationResult.value !is MfResult.Success) {
                    _warningsFlow.emit { "Please enter a valid nickname" }
                    return@launch
                }

                // Create user with stored Google credentials
                val storedCredentials = googleAuthManager.getStoredCredentials()
                if (storedCredentials != null) {
                    userRepository.createUserWithGoogle(
                        nickname,
                        storedCredentials.idToken,
                        storedCredentials.googleUserId
                    )
                    onSuccessfulCreation(notificationPermissionHelper)
                } else {
                    _warningsFlow.emit { "Authentication error. Please try again." }
                    _authUiState.value = AuthUiState.INITIAL
                }
            } catch (e: Exception) {
                Timber.e(e, "Account creation failed")
                _warningsFlow.emit { "Account creation failed: ${e.localizedMessage}" }
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    /**
     * Signs out the current user from both Firebase and clears all credential state.
     */
    fun signOut(onSignOutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Sign out from Google and Firebase
                googleAuthManager.signOut()

                // Clear user data from repository
                userRepository.clearAllUserData()

                // Reset UI state
                _authUiState.value = AuthUiState.INITIAL
                _draftNickname.value = ""

                onSignOutComplete()
            } catch (e: Exception) {
                Timber.e(e, "Sign out failed")
                _warningsFlow.emit { "Sign out failed: ${e.localizedMessage}" }
            }
        }
    }

}