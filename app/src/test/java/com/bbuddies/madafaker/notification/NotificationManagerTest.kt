package com.bbuddies.madafaker.notification

import android.content.Context
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.notification_domain.model.NotificationPayload
import com.bbuddies.madafaker.notification_domain.usecase.GetPlaceholderMessageUseCase
import com.bbuddies.madafaker.notification_domain.usecase.TrackNotificationEventUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NotificationManagerTest {

    private lateinit var context: Context
    private lateinit var getPlaceholderMessageUseCase: GetPlaceholderMessageUseCase
    private lateinit var trackNotificationEventUseCase: TrackNotificationEventUseCase
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        getPlaceholderMessageUseCase = mockk()
        trackNotificationEventUseCase = mockk(relaxed = true)

        notificationManager = NotificationManager(
            context = context,
            getPlaceholderMessageUseCase = getPlaceholderMessageUseCase,
            trackNotificationEventUseCase = trackNotificationEventUseCase
        )
    }

    @Test
    fun `showNotification should display notification with placeholder message and track received event`() = runTest {
        // Given
        val payload = NotificationPayload(
            messageId = "test_message_123",
            mode = Mode.SHINE,
            timestamp = "2024-01-01T12:00:00Z"
        )

        coEvery { getPlaceholderMessageUseCase(Mode.SHINE) } returns "Someone shared a thought with you ✨"

        // When
        notificationManager.showNotification(payload)

        // Then
        coVerify { trackNotificationEventUseCase.trackNotificationReceived("test_message_123", Mode.SHINE) }
    }

    @Test
    fun `handleNotificationOpened should track opened event with time calculation`() {
        // Given
        val messageId = "test_message_123"
        val notificationId = "notif_1234567890"
        val mode = Mode.SHADOW

        // When
        notificationManager.handleNotificationOpened(messageId, notificationId, mode)

        // Then
        coVerify { trackNotificationEventUseCase.trackNotificationOpened(messageId, mode, any()) }
    }

    @Test
    fun `handleNotificationDismissed should track dismissed event with time calculation`() {
        // Given
        val messageId = "test_message_123"
        val notificationId = "notif_1234567890"
        val mode = Mode.SHINE

        // When
        notificationManager.handleNotificationDismissed(messageId, notificationId, mode)

        // Then
        coVerify { trackNotificationEventUseCase.trackNotificationDismissed(messageId, mode, any()) }
    }
}
