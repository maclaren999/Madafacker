package com.bbuddies.madafaker.presentation.ui.main.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.MessageWithReplies
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.MainScreenContract
import com.bbuddies.madafaker.presentation.ui.main.SendMessageView
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMainScreenContract

@Composable
fun WriteTab(viewModel: MainScreenContract) {
    ScreenWithWarnings(warningsFlow = viewModel.warningsFlow) {
        SendMessageView(viewModel)
    }
}

private val previewOutgoingMessages = listOf(
    MessageWithReplies(
        message = Message(
            id = "sent-1",
            body = "Shared a reminder to drink water today.",
            mode = Mode.SHINE.apiValue,
            createdAt = "2024-04-01T08:00:00Z",
            authorId = "preview-user",
            authorName = "Preview User",
            localState = MessageState.SENT
        ),
        replies = emptyList()
    ),
    MessageWithReplies(
        message = Message(
            id = "sent-2",
            body = "Drafting a note about handling tough days.",
            mode = Mode.SHADOW.apiValue,
            createdAt = "2024-04-02T10:00:00Z",
            authorId = "preview-user",
            authorName = "Preview User",
            localState = MessageState.FAILED
        ),
        replies = emptyList()
    )
)

@Preview(showBackground = true)
@Composable
private fun WriteTabPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        WriteTab(
            viewModel = PreviewMainScreenContract(
                draftText = "Write something encouraging...",
                outgoingMessages = previewOutgoingMessages
            )
        )
    }
}


