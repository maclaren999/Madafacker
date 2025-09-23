package com.bbuddies.madafaker.presentation.ui.auth

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.BuildConfig
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.auth.GoogleAuthManager
import com.bbuddies.madafaker.presentation.auth.GoogleAuthResult
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.utils.ClipboardManager
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val googleAuthManager: GoogleAuthManager,
    private val clipboardManager: ClipboardManager
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

    /**
     * Initiates Google Sign-In flow and handles authentication.
     * @param context Activity context required for credential UI
     * @param onSuccessfulSignIn Callback invoked when sign-in is successful
     */
    fun onGoogleSignIn(context: Context, onSuccessfulSignIn: (NotificationPermissionHelper) -> Unit) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authUiState.value = AuthUiState.LOADING

            try {
                val authenticationResult = performCompleteGoogleAuthentication(context)

                when (authenticationResult) {
                    is AuthenticationResult.Success -> {
                        handleExistingUserAuthentication(authenticationResult, onSuccessfulSignIn)
                    }

                    is AuthenticationResult.NewUser -> {
                        _authUiState.value = AuthUiState.POST_GOOGLE_AUTH
                    }

                    is AuthenticationResult.Failure -> {
                        handleAuthenticationFailure(authenticationResult.error, authenticationResult.userMessage)
                    }
                }
            } catch (e: Exception) {
                handleAuthenticationFailure(e, "Google Sign-In failed. Please try again.")
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    /**
     * Creates a new user account with the provided nickname.
     * @param onSuccessfulCreation Callback invoked when account creation is successful
     */
    fun onCreateAccount(onSuccessfulCreation: (NotificationPermissionHelper) -> Unit) {
        viewModelScope.launch {
            _isSigningIn.value = true

            try {
                val validationResult = validateNicknameInput()
                if (validationResult != null) {
                    _warningsFlow.emit { validationResult }
                    return@launch
                }

                val nickname = _draftNickname.value.trim()
                val accountCreationResult = createUserAccount(nickname)

                when (accountCreationResult) {
                    is AccountCreationResult.Success -> {
                        onSuccessfulCreation(notificationPermissionHelper)
                    }

                    is AccountCreationResult.Failure -> {
                        handleAccountCreationFailure(accountCreationResult.error, accountCreationResult.userMessage)
                    }
                }
            } catch (e: Exception) {
                handleAccountCreationFailure(e, "Account creation failed. Please try again.")
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
                _warningsFlow.emit { ctx ->
                    ctx.getString(
                        R.string.error_sign_out_failed,
                        e.localizedMessage ?: "Unknown error"
                    )
                }
            }
        }
    }

    // MARK: - Private Helper Methods

    /**
     * Validates the nickname input and returns error message if invalid.
     * @return Error message if validation fails, null if valid
     */
    private fun validateNicknameInput(): String? {
        val nickname = _draftNickname.value.trim()

        return when {
            nickname.isEmpty() -> "Please enter a nickname"
            nicknameDraftValidationResult.value !is ValidationState.Success -> "Please enter a valid nickname"
            else -> null
        }
    }

    /**
     * Creates a new user account with the given nickname.
     * @param nickname The nickname for the new account
     * @return AccountCreationResult indicating success or failure
     */
    private suspend fun createUserAccount(nickname: String): AccountCreationResult {
        return try {
            val storedCredentials = googleAuthManager.getStoredCredentials()
                ?: return AccountCreationResult.Failure(
                    IllegalStateException("No stored Google credentials found"),
                    "Authentication error. Please sign in again."
                )

            userRepository.createUserWithGoogle(
                nickname,
                storedCredentials.idToken,
                storedCredentials.googleUserId
            )

            AccountCreationResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Account creation failed for nickname: $nickname")
            AccountCreationResult.Failure(e, "Account creation failed. Please try again.")
        }
    }

    /**
     * Handles account creation failures with proper logging and user messaging.
     * @param error The error that occurred
     * @param userMessage User-friendly error message
     */
    private suspend fun handleAccountCreationFailure(error: Throwable, userMessage: String) {
        Timber.e(error, "Account creation failed - $userMessage")
        _warningsFlow.emit { userMessage }
        _authUiState.value = AuthUiState.INITIAL
    }

    /**
     * Performs complete Google authentication including Firebase sign-in and credential storage.
     * @param context Activity context required for credential UI
     * @return AuthenticationResult indicating success, new user, or failure
     */
    private suspend fun performCompleteGoogleAuthentication(context: Context): AuthenticationResult {
        return try {
            val googleCredentialResponse = googleAuthManager.performGoogleAuthentication(context)
                ?: return AuthenticationResult.Failure(
                    IllegalStateException("Google authentication returned null"),
                    "Google authentication failed. Please try again."
                )

            val googleAuthResult = googleAuthManager.extractAndStoreCredentials(googleCredentialResponse)
                ?: return AuthenticationResult.Failure(
                    IllegalStateException("Failed to extract Google credentials"),
                    "Failed to process Google authentication. Please try again."
                )

            val firebaseAuthResult = authenticateWithFirebase(googleAuthResult.idToken)
            when (firebaseAuthResult) {
                is FirebaseAuthResult.Success -> {
                    storeAllAuthenticationData(googleAuthResult, firebaseAuthResult)
                    checkIfUserExists(googleAuthResult)
                }

                is FirebaseAuthResult.Failure -> {
                    AuthenticationResult.Failure(firebaseAuthResult.error, firebaseAuthResult.userMessage)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Authentication failed")
            AuthenticationResult.Failure(e, "Authentication failed. Please try again.")
        }
    }

    /**
     * Authenticates with Firebase using Google ID token.
     * @param googleIdToken The Google ID token to authenticate with
     * @return FirebaseAuthResult indicating success or failure
     */
    private suspend fun authenticateWithFirebase(googleIdToken: String): FirebaseAuthResult {
        return try {
            val firebaseUser = googleAuthManager.firebaseAuthWithGoogle(googleIdToken)
                ?: return FirebaseAuthResult.Failure(
                    IllegalStateException("Firebase authentication returned null user"),
                    "Firebase authentication failed. Please try again."
                )

            val firebaseIdToken = firebaseUser.getIdToken(true).await()?.token
                ?: return FirebaseAuthResult.Failure(
                    IllegalStateException("Failed to get Firebase ID token"),
                    "Failed to get authentication token. Please try again."
                )

            FirebaseAuthResult.Success(firebaseUser.uid, firebaseIdToken)
        } catch (e: Exception) {
            Timber.e(e, "Firebase authentication failed")
            FirebaseAuthResult.Failure(
                e,
                "Firebase authentication failed. Please try again."
            ) // This will be handled by context in the UI
        }
    }

    /**
     * Stores all authentication data in preferences.
     * @param googleAuthResult Google authentication result
     * @param firebaseAuthResult Firebase authentication result
     */
    private suspend fun storeAllAuthenticationData(
        googleAuthResult: GoogleAuthResult,
        firebaseAuthResult: FirebaseAuthResult.Success,
    ) {
        userRepository.storeGoogleAuth(
            googleAuthResult.idToken,
            googleAuthResult.googleUserId,
            firebaseAuthResult.firebaseIdToken,
            firebaseAuthResult.firebaseUid
        )
        if (BuildConfig.DEBUG) {
            clipboardManager.setText(firebaseAuthResult.firebaseIdToken)
        }
    }

    /**
     * Checks if user exists in the backend and returns appropriate result.
     * @param googleAuthResult Google authentication result
     * @return AuthenticationResult indicating existing user or new user
     */
    private suspend fun checkIfUserExists(googleAuthResult: GoogleAuthResult): AuthenticationResult {
        return try {
            userRepository.authenticateWithGoogle(googleAuthResult.idToken, googleAuthResult.googleUserId)
            AuthenticationResult.Success(googleAuthResult)
        } catch (e: Exception) {
            AuthenticationResult.NewUser(googleAuthResult)
        }
    }

    /**
     * Handles successful authentication for existing users.
     * @param result Authentication result
     * @param onSuccessfulSignIn Success callback
     */
    private suspend fun handleExistingUserAuthentication(
        result: AuthenticationResult.Success,
        onSuccessfulSignIn: (NotificationPermissionHelper) -> Unit
    ) {
        onSuccessfulSignIn(notificationPermissionHelper)
    }

    /**
     * Handles authentication failures with proper logging and user messaging.
     * @param error The error that occurred
     * @param userMessage User-friendly error message
     */
    private suspend fun handleAuthenticationFailure(error: Throwable, userMessage: String) {
        Timber.e(error, "Authentication failed - $userMessage")
        _warningsFlow.emit { userMessage }
        _authUiState.value = AuthUiState.INITIAL
    }

    // MARK: - Helper Classes

    /**
     * Represents the result of a complete authentication flow.
     */
    private sealed class AuthenticationResult {
        data class Success(val googleAuthResult: GoogleAuthResult) : AuthenticationResult()
        data class NewUser(val googleAuthResult: GoogleAuthResult) : AuthenticationResult()
        data class Failure(val error: Throwable, val userMessage: String) : AuthenticationResult()
    }

    /**
     * Represents the result of Firebase authentication.
     */
    private sealed class FirebaseAuthResult {
        data class Success(val firebaseUid: String, val firebaseIdToken: String) : FirebaseAuthResult()
        data class Failure(val error: Throwable, val userMessage: String) : FirebaseAuthResult()
    }

    /**
     * Represents the result of account creation.
     */
    private sealed class AccountCreationResult {
        object Success : AccountCreationResult()
        data class Failure(val error: Throwable, val userMessage: String) : AccountCreationResult()
    }

}