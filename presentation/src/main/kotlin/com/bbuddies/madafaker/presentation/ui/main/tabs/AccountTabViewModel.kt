package com.bbuddies.madafaker.presentation.ui.main.tabs

import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.R
import com.bbuddies.madafaker.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountTabViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog: StateFlow<Boolean> = _showDeleteAccountDialog

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog

    val currentUser = userRepository.currentUser

    fun onDeleteAccountClick() {
        _showDeleteAccountDialog.value = true
    }

    fun onLogoutClick() {
        _showLogoutDialog.value = true
    }

    fun dismissDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
    }

    fun dismissLogoutDialog() {
        _showLogoutDialog.value = false
    }

    fun sendDeleteAccountEmail(context: Context, user: User?) {
        if (user == null) return

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("group.byte.buddies@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Account Deletion Request")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                Dear Support Team,
                
                I would like to request the deletion of my account from the Madafaker app.
                
                Account Details:
                - User ID: ${user.id}
                - Username: ${user.name}
                - Account Created: ${user.createdAt}
                
                Please process this request and confirm once my account and all associated data have been permanently deleted.
                
                Thank you for your assistance.
                
                Best regards
                """.trimIndent()
            )
        }

        try {
            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.email_chooser_title)))
            _showDeleteAccountDialog.value = false
        } catch (e: Exception) {
            viewModelScope.launch {
                _warningsFlow.emit { ctx -> ctx.getString(R.string.error_send_email_app_not_found) }
            }
        }
    }

    fun performLogout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.clearAllUserData()
                _showLogoutDialog.value = false
                onLogoutComplete()
            } catch (e: Exception) {
                _warningsFlow.emit { ctx ->
                    ctx.getString(
                        R.string.error_logout_failed,
                        e.localizedMessage ?: "Unknown error"
                    )
                }
            }
        }
    }
}