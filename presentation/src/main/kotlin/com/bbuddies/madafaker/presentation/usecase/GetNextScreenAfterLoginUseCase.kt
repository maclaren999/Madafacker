package com.bbuddies.madafaker.presentation.usecase

import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.presentation.NavigationItem
import com.bbuddies.madafaker.presentation.utils.NotificationPermissionHelper
import javax.inject.Inject

class GetNextScreenAfterLoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationPermissionHelper: NotificationPermissionHelper
) {

    suspend operator fun invoke(): NavigationItem {
        // Use awaitCurrentUser to wait for authentication state to complete
        // This avoids duplicate API calls by reusing the authenticationState flow
        val currentUser = userRepository.awaitCurrentUser()

        return when {
            currentUser == null -> NavigationItem.Account
            !notificationPermissionHelper.isNotificationPermissionGranted() ->
                NavigationItem.NotificationPermission

            else -> NavigationItem.Main
        }
    }
}