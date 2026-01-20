package com.bbuddies.madafaker.presentation.usecase

import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.ui.splash.SplashNavigationDestination
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "SPLASH_NAV"

class GetNextScreenAfterLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationPermissionHelper: NotificationPermissionHelper
) {

    suspend operator fun invoke(): SplashNavigationDestination {
        Timber.tag(TAG).d("Determining next screen...")

        // Use awaitCurrentUser to wait for authentication state to complete
        // This avoids duplicate API calls by reusing the authenticationState flow
        val currentUser = userRepository.awaitCurrentUser()

        Timber.tag(TAG).d("Current user: ${currentUser?.name ?: "null"}")

        val destination = when {
            currentUser == null -> {
                Timber.tag(TAG).d("No user -> Auth")
                SplashNavigationDestination.Auth
            }

            !notificationPermissionHelper.isNotificationPermissionGranted() -> {
                Timber.tag(TAG).d("User exists but no notification permission -> NotificationPermission")
                SplashNavigationDestination.NotificationPermission
            }

            else -> {
                Timber.tag(TAG).d("User exists with permissions -> Main")
                SplashNavigationDestination.Main
            }
        }

        Timber.tag(TAG).d("Navigation destination: $destination")
        return destination
    }
}