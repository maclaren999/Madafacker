package com.bbuddies.madafaker.presentation.usecase

import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.ui.splash.SplashNavigationDestination
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import javax.inject.Inject

class GetNextScreenAfterLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationPermissionHelper: NotificationPermissionHelper,
    private val preferenceManager: PreferenceManager
) {

    suspend operator fun invoke(): SplashNavigationDestination {
        // Use awaitCurrentUser to wait for authentication state to complete
        // This avoids duplicate API calls by reusing the authenticationState flow
        val currentUser = userRepository.awaitCurrentUser()
        val promptDismissed = preferenceManager.notificationPermissionPromptDismissed.value

        return when {
            currentUser == null -> SplashNavigationDestination.Auth
            !notificationPermissionHelper.isNotificationPermissionGranted() && !promptDismissed ->
                SplashNavigationDestination.NotificationPermission

            else -> SplashNavigationDestination.Main
        }
    }
}
