package com.bbuddies.madafaker.presentation.ui.account

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.BuildConfig
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import com.bbuddies.madafaker.presentation.base.MfResult
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class NewUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationPermissionHelper: NotificationPermissionHelper,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _draftNickname = MutableStateFlow("")
    val draftNickname: StateFlow<String> = _draftNickname

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn

    private val draftValidator = NicknameDraftValidator(userRepository, viewModelScope)
    val nicknameDraftValidationResult = draftValidator.validationResult

    private val webClientId = BuildConfig.LIBRARY_PACKAGE_NAME

    private val credentialManager = CredentialManager.create(context)

    fun onDraftNickChanged(newNickname: String) {
        _draftNickname.value = newNickname
        draftValidator.onDraftNickChanged(newNickname)
    }

    fun onGoogleSignIn(onSuccessfulSignIn: (NotificationPermissionHelper) -> Unit) {
        viewModelScope.launch {
            _isSigningIn.value = true

            try {
                // First, try to get authorized accounts (existing users)
                val authorizedResult = tryGetAuthorizedAccount()

                if (authorizedResult != null) {
                    // Existing user sign-in
                    handleGoogleSignInResult(authorizedResult, null, onSuccessfulSignIn)
                } else {
                    // New user sign-up - need nickname
                    val nickname = _draftNickname.value.trim()

                    if (nickname.isEmpty()) {
                        _warningsFlow.emit { "Please enter a nickname for your new account" }
                        return@launch
                    }

                    // Validate nickname first
                    if (nicknameDraftValidationResult.value !is MfResult.Success) {
                        _warningsFlow.emit { "Please enter a valid nickname" }
                        return@launch
                    }

                    // Try sign-up flow
                    val signUpResult = trySignUpWithGoogle()
                    if (signUpResult != null) {
                        handleGoogleSignInResult(signUpResult, nickname, onSuccessfulSignIn)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Google Sign-In failed")
                _warningsFlow.emit { "Sign-in failed: ${e.localizedMessage}" }
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    private suspend fun tryGetAuthorizedAccount(): GetCredentialResponse? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            credentialManager.getCredential(
                request = request,
                context = context
            )
        } catch (e: GetCredentialException) {
            Timber.d("No authorized accounts found: ${e.message}")
            null
        }
    }

    private suspend fun trySignUpWithGoogle(): GetCredentialResponse? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            credentialManager.getCredential(
                request = request,
                context = context
            )
        } catch (e: GetCredentialException) {
            Timber.e(e, "Google Sign-Up failed")
            throw e
        }
    }

    private suspend fun handleGoogleSignInResult(
        result: GetCredentialResponse,
        nickname: String?,
        onSuccessfulSignIn: (NotificationPermissionHelper) -> Unit
    ) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        val googleUserId = googleIdTokenCredential.id

                        Timber.d("Google Sign-In successful. User ID: $googleUserId")

                        // Store Google ID token and user ID
                        userRepository.storeGoogleAuth(idToken, googleUserId)

                        if (nickname != null) {
                            // New user - create account with nickname
                            userRepository.createUserWithGoogle(nickname, idToken, googleUserId)
                        } else {
                            // Existing user - just authenticate
                            userRepository.authenticateWithGoogle(idToken, googleUserId)
                        }

                        onSuccessfulSignIn(notificationPermissionHelper)

                    } catch (e: GoogleIdTokenParsingException) {
                        Timber.e(e, "Invalid Google ID token received")
                        _warningsFlow.emit { "Invalid Google sign-in response" }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to process Google sign-in")
                        _warningsFlow.emit { "Sign-in failed: ${e.localizedMessage}" }
                    }
                } else {
                    Timber.e("Unexpected credential type: ${credential.type}")
                    _warningsFlow.emit { "Unexpected sign-in response" }
                }
            }

            else -> {
                Timber.e("Unexpected credential type: ${credential::class.simpleName}")
                _warningsFlow.emit { "Unexpected sign-in response" }
            }
        }
    }

    fun handleDeleteAccount() {
        viewModelScope.launch {
            try {
                // Clear credential state from all providers
                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )

                // Clear user data from repository
                userRepository.clearAllUserData()

                _warningsFlow.emit { "Account deleted successfully" }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete account")
                _warningsFlow.emit { "Failed to delete account: ${e.localizedMessage}" }
            }
        }
    }
}