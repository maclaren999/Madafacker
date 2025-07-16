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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState.INITIAL)
    val authUiState: StateFlow<AuthUiState> = _authUiState

    private val _draftNickname = MutableStateFlow("")
    val draftNickname: StateFlow<String> = _draftNickname

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn

    private val draftValidator = NicknameDraftValidator(userRepository, viewModelScope)
    val nicknameDraftValidationResult = draftValidator.validationResult

    private val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

    private val credentialManager = CredentialManager.create(context)

    // Store Google credentials for later use in account creation
    private var storedGoogleIdToken: String? = null
    private var storedGoogleUserId: String? = null

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
                val googleResult = performGoogleAuthentication()

                if (googleResult != null) {
                    // Store credentials for potential account creation
                    val credential = googleResult.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {

                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        storedGoogleIdToken = googleIdTokenCredential.idToken
                        storedGoogleUserId = googleIdTokenCredential.id

                        // Store Google auth and check if user exists
                        userRepository.storeGoogleAuth(storedGoogleIdToken!!, storedGoogleUserId!!)

                        try {
                            // Try to authenticate existing user
                            userRepository.authenticateWithGoogle(storedGoogleIdToken!!, storedGoogleUserId!!)
                            // User exists, proceed to main screen
                            onSuccessfulSignIn(notificationPermissionHelper)
                        } catch (e: Exception) {
                            // User doesn't exist, show nickname input
                            _authUiState.value = AuthUiState.POST_GOOGLE_AUTH
                        }
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
                if (storedGoogleIdToken != null && storedGoogleUserId != null) {
                    userRepository.createUserWithGoogle(nickname, storedGoogleIdToken!!, storedGoogleUserId!!)
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

    private suspend fun performGoogleAuthentication(): GetCredentialResponse? {
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
            Timber.e(e, "Google authentication failed")
            throw e
        }
    }


}