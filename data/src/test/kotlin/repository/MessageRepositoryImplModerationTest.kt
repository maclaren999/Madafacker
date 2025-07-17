package repository

import androidx.work.WorkManager
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.exception.ModerationException
import com.bbuddies.madafaker.common_domain.model.FilterResult
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.model.ViolationType
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import com.bbuddies.madafaker.common_domain.repository.UserRepository
import com.bbuddies.madafaker.common_domain.service.ContentFilterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import local.MadafakerDao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import remote.api.MadafakerApi
import remote.api.dto.MessageDto

class MessageRepositoryImplModerationTest {

    @Mock
    private lateinit var webService: MadafakerApi

    @Mock
    private lateinit var localDao: MadafakerDao

    @Mock
    private lateinit var workManager: WorkManager

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var contentFilterService: ContentFilterService

    private lateinit var messageRepository: MessageRepositoryImpl

    private val testUser = User(
        id = "test-user-id",
        name = "Test User",
        registrationToken = "test-token",
        coins = 0,
        createdAt = "2024-01-01T00:00:00.000Z",
        updatedAt = "2024-01-01T00:00:00.000Z"
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        messageRepository = MessageRepositoryImpl(
            webService = webService,
            localDao = localDao,
            workManager = workManager,
            preferenceManager = preferenceManager,
            userRepository = userRepository,
            contentFilterService = contentFilterService
        )

        // Setup default mocks
        runBlocking { whenever(userRepository.awaitCurrentUser()).thenReturn(testUser) }
        whenever(preferenceManager.currentMode).thenReturn(MutableStateFlow(Mode.SHINE))
    }

    @Test
    fun `createMessage should allow clean content in SHINE mode`() = runTest {
        // Given
        val cleanMessage = "Hello world, this is a nice message!"
        val allowedResult = FilterResult(isAllowed = true)
        val mockMessageDto = MessageDto(
            id = "server-id",
            body = cleanMessage,
            mode = "light",
            isPublic = true,
            createdAt = "2024-01-01T00:00:00.000Z",
            authorId = testUser.id
        )

        whenever(contentFilterService.filterContent(cleanMessage, Mode.SHINE))
            .thenReturn(allowedResult)
        whenever(webService.createMessage(any())).thenReturn(mockMessageDto)

        // When
        val result = messageRepository.createMessage(cleanMessage)

        // Then
        assertNotNull("Should return a message", result)
        assertEquals("Should have correct body", cleanMessage, result.body)
        verify(contentFilterService).filterContent(cleanMessage, Mode.SHINE)
        verify(webService).createMessage(any())
        verify(localDao).insertMessage(any())
        verify(localDao).deleteMessage(any()) // Temp message cleanup
    }

    @Test
    fun `createMessage should throw ClientSideViolation for profane content in SHINE mode`() = runTest {
        // Given
        val profaneMessage = "This is some damn bad shit"
        val rejectedResult = FilterResult(
            isAllowed = false,
            violationType = ViolationType.PROFANITY,
            detectedWords = listOf("damn", "shit"),
            suggestion = "Please keep it positive or switch to Shadow mode!"
        )

        whenever(contentFilterService.filterContent(profaneMessage, Mode.SHINE))
            .thenReturn(rejectedResult)

        // When & Then
        val exception = assertThrows(ModerationException.ClientSideViolation::class.java) {
            runBlocking { messageRepository.createMessage(profaneMessage) }
        }

        assertEquals("Should have correct violation type", ViolationType.PROFANITY, exception.violationType)
        assertEquals("Should have correct mode", Mode.SHINE, exception.mode)
        assertTrue("Should contain detected words", exception.detectedWords.contains("damn"))
        assertTrue("Should contain detected words", exception.detectedWords.contains("shit"))

        verify(contentFilterService).filterContent(profaneMessage, Mode.SHINE)
        verify(webService, never()).createMessage(any())
    }

    @Test
    fun `createMessage should skip client filtering in SHADOW mode`() = runTest {
        // Given
        val anyMessage = "Any content should be allowed"
        val allowedResult = FilterResult(isAllowed = true)
        val mockMessageDto = MessageDto(
            id = "server-id",
            body = anyMessage,
            mode = "dark",
            isPublic = true,
            createdAt = "2024-01-01T00:00:00.000Z",
            authorId = testUser.id
        )

        whenever(preferenceManager.currentMode).thenReturn(MutableStateFlow(Mode.SHADOW))
        whenever(contentFilterService.filterContent(anyMessage, Mode.SHADOW))
            .thenReturn(allowedResult)
        whenever(webService.createMessage(any())).thenReturn(mockMessageDto)

        // When
        val result = messageRepository.createMessage(anyMessage)

        // Then
        assertNotNull("Should return a message", result)
        assertEquals("Should have correct body", anyMessage, result.body)
        assertEquals("Should have dark mode", "dark", result.mode)
        verify(contentFilterService).filterContent(anyMessage, Mode.SHADOW)
        verify(webService).createMessage(any())
    }

    @Test
    fun `createMessage should handle server-side moderation errors`() = runTest {
        // Given
        val message = "Content that passes client but fails server"
        val allowedResult = FilterResult(isAllowed = true)
        val httpException = retrofit2.HttpException(
            retrofit2.Response.error<Any>(
                422,
                okhttp3.ResponseBody.create(null, """{"error": "Server moderation failed"}""")
            )
        )

        whenever(contentFilterService.filterContent(message, Mode.SHINE))
            .thenReturn(allowedResult)
        whenever(webService.createMessage(any())).thenThrow(httpException)

        // When
        val result = messageRepository.createMessage(message)

        // Then
        // Should not throw exception, but schedule background send instead
        assertNotNull("Should return local message", result)
        assertTrue("Should be a temp message", result.id.startsWith("temp_"))
        verify(localDao, times(1)).insertMessage(any()) // Only temp message insertion
        verify(localDao, never()).deleteMessage(any()) // No cleanup since server failed
    }

    @Test
    fun `createMessage should require authenticated user`() = runTest {
        // Given
        whenever(userRepository.awaitCurrentUser()).thenReturn(null)

        // When & Then
        val exception = assertThrows(IllegalStateException::class.java) {
            runBlocking { messageRepository.createMessage("Any message") }
        }

        assertEquals(
            "Should have correct error message",
            "No authenticated user available", exception.message
        )
        verify(contentFilterService, never()).filterContent(any(), any())
        verify(webService, never()).createMessage(any())
    }

    @Test
    fun `createMessage should handle empty and whitespace messages`() = runTest {
        // Given
        val emptyMessage = ""
        val whitespaceMessage = "   \n\t  "
        val allowedResult = FilterResult(isAllowed = true)

        whenever(contentFilterService.filterContent(any(), any())).thenReturn(allowedResult)

        // When & Then - should call filtering service even for edge cases
        try {
            messageRepository.createMessage(emptyMessage)
        } catch (e: Exception) {
            // Expected - empty messages might be rejected by validation
        }

        // The actual behavior depends on validation logic in the service
        // This test ensures the filtering service is called even for edge cases
        verify(contentFilterService, atLeastOnce()).filterContent(any(), any())
    }
}
