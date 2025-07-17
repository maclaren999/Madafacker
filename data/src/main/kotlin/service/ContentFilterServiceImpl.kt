package service

import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.FilterResult
import com.bbuddies.madafaker.common_domain.model.ViolationType
import com.bbuddies.madafaker.common_domain.service.ContentFilterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ContentFilterService using regex-based profanity detection
 * Provides lightweight, on-device content filtering for SHINE mode
 */
@Singleton
class ContentFilterServiceImpl @Inject constructor() : ContentFilterService {

    // Basic profanity patterns - can be expanded and made configurable
    private val profanityPatterns = listOf(
        // Common profanity (using placeholder patterns for demo)
        "\\b(damn|hell|crap|shit|fuck|bitch|ass|bastard)\\b".toRegex(RegexOption.IGNORE_CASE),
        "\\b(stupid|idiot|moron|retard)\\b".toRegex(RegexOption.IGNORE_CASE),
        // Hate speech patterns
        "\\b(hate|kill|die|murder)\\s+(you|them|him|her)\\b".toRegex(RegexOption.IGNORE_CASE),
        // Sexual content patterns
        "\\b(porn|nude|naked|xxx)\\b".toRegex(RegexOption.IGNORE_CASE)
    )

    // Harassment patterns
    private val harassmentPatterns = listOf(
        "\\b(go\\s+kill\\s+yourself|kys)\\b".toRegex(RegexOption.IGNORE_CASE),
        "\\b(you\\s+suck|loser)\\b".toRegex(RegexOption.IGNORE_CASE)
    )

    // Violence patterns
    private val violencePatterns = listOf(
        "\\b(violence|fight|punch|hit|attack)\\b".toRegex(RegexOption.IGNORE_CASE),
        "\\b(gun|weapon|knife|bomb)\\b".toRegex(RegexOption.IGNORE_CASE)
    )

    override suspend fun filterContent(text: String, mode: Mode): FilterResult = withContext(Dispatchers.Default) {
        // Only apply client-side filtering for SHINE mode
        if (mode != Mode.SHINE) {
            return@withContext FilterResult(isAllowed = true)
        }

        return@withContext checkProfanity(text)
    }

    override suspend fun checkProfanity(text: String): FilterResult = withContext(Dispatchers.Default) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) {
            return@withContext FilterResult(isAllowed = true)
        }

        // Check for profanity
        val profanityResult = checkPatterns(cleanText, profanityPatterns, ViolationType.PROFANITY)
        if (!profanityResult.isAllowed) return@withContext profanityResult

        // Check for harassment
        val harassmentResult = checkPatterns(cleanText, harassmentPatterns, ViolationType.HARASSMENT)
        if (!harassmentResult.isAllowed) return@withContext harassmentResult

        // Check for violence
        val violenceResult = checkPatterns(cleanText, violencePatterns, ViolationType.VIOLENCE)
        if (!violenceResult.isAllowed) return@withContext violenceResult

        return@withContext FilterResult(isAllowed = true)
    }

    private fun checkPatterns(text: String, patterns: List<Regex>, violationType: ViolationType): FilterResult {
        val detectedWords = mutableListOf<String>()

        for (pattern in patterns) {
            val matches = pattern.findAll(text)
            if (matches.any()) {
                detectedWords.addAll(matches.map { it.value })
            }
        }

        return if (detectedWords.isNotEmpty()) {
            FilterResult(
                isAllowed = false,
                violationType = violationType,
                confidence = 1.0f,
                suggestion = getSuggestionForViolation(violationType),
                detectedWords = detectedWords.distinct()
            )
        } else {
            FilterResult(isAllowed = true)
        }
    }

    private fun getSuggestionForViolation(violationType: ViolationType): String {
        return when (violationType) {
            ViolationType.PROFANITY -> "Please keep it positive or switch to Shadow mode for uncensored expression!"
            ViolationType.HARASSMENT -> "Let's keep things respectful. Try Shadow mode if you need to express frustration."
            ViolationType.VIOLENCE -> "Violent content isn't allowed in Shine mode. Consider switching to Shadow mode."
            ViolationType.HATE_SPEECH -> "Hate speech isn't welcome here. Please rephrase or use Shadow mode."
            ViolationType.SEXUAL_CONTENT -> "Sexual content should be shared in Shadow mode only."
            ViolationType.ILLEGAL_CONTENT -> "This content cannot be shared in any mode."
        }
    }

    override suspend fun updateProfanityList(words: List<String>) {
        // Future implementation: Update local word list
        // Could store in SharedPreferences or local database
        // For now, this is a no-op as patterns are hardcoded
    }
}
