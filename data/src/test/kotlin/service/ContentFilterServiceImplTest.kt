package service

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.ViolationType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContentFilterServiceImplTest {

    private lateinit var contentFilterService: ContentFilterServiceImpl

    @Before
    fun setUp() {
        contentFilterService = ContentFilterServiceImpl()
    }

    @Test
    fun `filterContent should allow clean text in SHINE mode`() = runTest {
        // Given
        val cleanText = "Hello world, this is a nice message!"
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(cleanText, mode)

        // Then
        assertTrue("Clean text should be allowed", result.isAllowed)
        assertNull("No violation type should be detected", result.violationType)
        assertTrue("No words should be detected", result.detectedWords.isEmpty())
    }

    @Test
    fun `filterContent should allow any text in SHADOW mode`() = runTest {
        // Given
        val profaneText = "This is some damn bad shit"
        val mode = Mode.SHADOW

        // When
        val result = contentFilterService.filterContent(profaneText, mode)

        // Then
        assertTrue("All text should be allowed in SHADOW mode", result.isAllowed)
        assertNull("No violation type should be detected in SHADOW mode", result.violationType)
    }

    @Test
    fun `filterContent should detect profanity in SHINE mode`() = runTest {
        // Given
        val profaneText = "This is some damn bad shit"
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(profaneText, mode)

        // Then
        assertFalse("Profane text should not be allowed", result.isAllowed)
        assertEquals("Should detect profanity", ViolationType.PROFANITY, result.violationType)
        assertTrue("Should detect profane words", result.detectedWords.isNotEmpty())
        assertTrue("Should contain 'damn'", result.detectedWords.contains("damn"))
        assertTrue("Should contain 'shit'", result.detectedWords.contains("shit"))
        assertNotNull("Should provide suggestion", result.suggestion)
    }

    @Test
    fun `filterContent should detect harassment in SHINE mode`() = runTest {
        // Given
        val harassmentText = "You suck and you're a loser"
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(harassmentText, mode)

        // Then
        assertFalse("Harassment should not be allowed", result.isAllowed)
        assertEquals("Should detect harassment", ViolationType.HARASSMENT, result.violationType)
        assertTrue("Should detect harassment words", result.detectedWords.isNotEmpty())
        assertNotNull("Should provide suggestion", result.suggestion)
    }

    @Test
    fun `filterContent should detect violence in SHINE mode`() = runTest {
        // Given
        val violentText = "I want to fight and use a gun"
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(violentText, mode)

        // Then
        assertFalse("Violent content should not be allowed", result.isAllowed)
        assertEquals("Should detect violence", ViolationType.VIOLENCE, result.violationType)
        assertTrue("Should detect violent words", result.detectedWords.isNotEmpty())
        assertNotNull("Should provide suggestion", result.suggestion)
    }

    @Test
    fun `filterContent should handle empty text`() = runTest {
        // Given
        val emptyText = ""
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(emptyText, mode)

        // Then
        assertTrue("Empty text should be allowed", result.isAllowed)
        assertNull("No violation type for empty text", result.violationType)
    }

    @Test
    fun `filterContent should handle whitespace-only text`() = runTest {
        // Given
        val whitespaceText = "   \n\t  "
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(whitespaceText, mode)

        // Then
        assertTrue("Whitespace-only text should be allowed", result.isAllowed)
        assertNull("No violation type for whitespace", result.violationType)
    }

    @Test
    fun `checkProfanity should detect case-insensitive profanity`() = runTest {
        // Given
        val mixedCaseText = "This is DAMN bad ShIt"

        // When
        val result = contentFilterService.checkProfanity(mixedCaseText)

        // Then
        assertFalse("Should detect case-insensitive profanity", result.isAllowed)
        assertEquals("Should detect profanity", ViolationType.PROFANITY, result.violationType)
        assertTrue("Should detect mixed case words", result.detectedWords.isNotEmpty())
    }

    @Test
    fun `checkProfanity should not detect partial word matches`() = runTest {
        // Given
        val partialMatchText = "I love classical music"

        // When
        val result = contentFilterService.checkProfanity(partialMatchText)

        // Then
        assertTrue("Should not detect partial matches", result.isAllowed)
        assertNull("No violation for partial matches", result.violationType)
    }

    @Test
    fun `filterContent should provide appropriate suggestions for different violation types`() = runTest {
        // Test profanity suggestion
        val profanityResult = contentFilterService.filterContent("damn", Mode.SHINE)
        assertTrue(
            "Profanity suggestion should mention Shadow mode",
            profanityResult.suggestion?.contains("Shadow mode") == true
        )

        // Test harassment suggestion
        val harassmentResult = contentFilterService.filterContent("you suck", Mode.SHINE)
        assertTrue(
            "Harassment suggestion should mention respect",
            harassmentResult.suggestion?.contains("respectful") == true
        )

        // Test violence suggestion
        val violenceResult = contentFilterService.filterContent("fight", Mode.SHINE)
        assertTrue(
            "Violence suggestion should mention Shadow mode",
            violenceResult.suggestion?.contains("Shadow mode") == true
        )
    }

    @Test
    fun `filterContent should handle multiple violation types`() = runTest {
        // Given - text with both profanity and harassment
        val multiViolationText = "You're a damn idiot"
        val mode = Mode.SHINE

        // When
        val result = contentFilterService.filterContent(multiViolationText, mode)

        // Then
        assertFalse("Multi-violation text should not be allowed", result.isAllowed)
        // Should detect the first violation type encountered (profanity in this case)
        assertEquals("Should detect first violation type", ViolationType.PROFANITY, result.violationType)
        assertTrue("Should detect multiple words", result.detectedWords.size >= 1)
    }
}
