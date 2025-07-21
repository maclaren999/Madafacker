package com.bbuddies.madafaker.notification_domain.model

import com.bbuddies.madafaker.common_domain.enums.Mode

/**
 * Mode-specific placeholder messages for notifications
 */
data class PlaceholderMessage(
    val text: String,
    val mode: Mode
)

object PlaceholderMessages {
    val SHINE_MESSAGES = listOf(
        "Someone shared a thought with you ✨",
        "A stranger left you something to consider 💭",
        "Fresh perspective from the universe 🌟",
        "You've got a random message waiting 🎲",
        "Someone reached out to you ☀️",
        "A soul dropped a line for you 📝",
        "Something meaningful just arrived 🌸",
        "A human moment awaits ⚡"
    )

    val SHADOW_MESSAGES = listOf(
        "Someone shared their unfiltered thoughts 🌙",
        "A wild message appeared from the shadows 🎪",
        "Raw thoughts from a stranger 🔮",
        "Someone dropped their guard for you 🎯",
        "Unfiltered wisdom just landed 🌊",
        "A stranger's honest take awaits 🎭",
        "Something real just surfaced 🌑",
        "Truth from the underground 🔥"
    )

    fun getRandomMessage(mode: Mode): String {
        return when (mode) {
            Mode.SHINE -> SHINE_MESSAGES.random()
            Mode.SHADOW -> SHADOW_MESSAGES.random()
        }
    }
}
