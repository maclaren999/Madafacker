package com.bbuddies.madafaker.presentation.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bbuddies.madafaker.common_domain.enums.Mode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModerationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moderationDialog_displaysCorrectContent() {
        // Given
        val dialogState = ModerationDialogState(
            title = "Content Not Allowed",
            message = "Please keep it positive or switch to Shadow mode!",
            showSwitchToShadow = true,
            currentMode = Mode.SHINE
        )
        var dismissCalled = false
        var switchCalled = false

        // When
        composeTestRule.setContent {
            ModerationDialog(
                state = dialogState,
                onDismiss = { dismissCalled = true },
                onSwitchToShadow = { switchCalled = true }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Content Not Allowed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please keep it positive or switch to Shadow mode!")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Got it").assertIsDisplayed()
        composeTestRule.onNodeWithText("Switch to Shadow").assertIsDisplayed()
    }

    @Test
    fun moderationDialog_dismissButton_callsOnDismiss() {
        // Given
        val dialogState = ModerationDialogState(
            title = "Content Not Allowed",
            message = "Test message",
            showSwitchToShadow = true,
            currentMode = Mode.SHINE
        )
        var dismissCalled = false

        composeTestRule.setContent {
            ModerationDialog(
                state = dialogState,
                onDismiss = { dismissCalled = true },
                onSwitchToShadow = { }
            )
        }

        // When
        composeTestRule.onNodeWithText("Got it").performClick()

        // Then
        assert(dismissCalled) { "onDismiss should be called when Got it button is clicked" }
    }

    @Test
    fun moderationDialog_switchButton_callsOnSwitchToShadow() {
        // Given
        val dialogState = ModerationDialogState(
            title = "Content Not Allowed",
            message = "Test message",
            showSwitchToShadow = true,
            currentMode = Mode.SHINE
        )
        var switchCalled = false

        composeTestRule.setContent {
            ModerationDialog(
                state = dialogState,
                onDismiss = { },
                onSwitchToShadow = { switchCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("Switch to Shadow").performClick()

        // Then
        assert(switchCalled) { "onSwitchToShadow should be called when Switch to Shadow button is clicked" }
    }

    @Test
    fun moderationDialog_hidesSwitchButton_whenShowSwitchToShadowIsFalse() {
        // Given
        val dialogState = ModerationDialogState(
            title = "Content Rejected",
            message = "This content violates legal guidelines",
            showSwitchToShadow = false,
            currentMode = Mode.SHADOW
        )

        // When
        composeTestRule.setContent {
            ModerationDialog(
                state = dialogState,
                onDismiss = { },
                onSwitchToShadow = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Content Rejected").assertIsDisplayed()
        composeTestRule.onNodeWithText("This content violates legal guidelines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Got it").assertIsDisplayed()
        composeTestRule.onNodeWithText("Switch to Shadow").assertDoesNotExist()
    }

    @Test
    fun moderationDialog_displaysAdditionalInfo_whenShowSwitchToShadowIsTrue() {
        // Given
        val dialogState = ModerationDialogState(
            title = "Content Not Allowed",
            message = "Test message",
            showSwitchToShadow = true,
            currentMode = Mode.SHINE
        )

        // When
        composeTestRule.setContent {
            ModerationDialog(
                state = dialogState,
                onDismiss = { },
                onSwitchToShadow = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(
            "Shadow mode allows more freedom of expression with minimal content filtering."
        ).assertIsDisplayed()
    }

    @Test
    fun moderationDialog_hidesAdditionalInfo_whenShowSwitchToShadowIsFalse() {
        // Given
        val dialogState = ModerationDialogState(
            title = "Content Rejected",
            message = "Test message",
            showSwitchToShadow = false,
            currentMode = Mode.SHADOW
        )

        // When
        composeTestRule.setContent {
            ModerationDialog(
                state = dialogState,
                onDismiss = { },
                onSwitchToShadow = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(
            "Shadow mode allows more freedom of expression with minimal content filtering."
        ).assertDoesNotExist()
    }
}
