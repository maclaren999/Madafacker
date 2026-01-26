package com.bbuddies.madafaker.presentation.ui.main.tabs.write

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.presentation.base.ScreenWithWarnings
import com.bbuddies.madafaker.presentation.design.theme.MadafakerTheme
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewMessages
import com.bbuddies.madafaker.presentation.ui.main.preview.PreviewWriteTabContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WriteTab(
    state: WriteTabState,
    warningsFlow: StateFlow<((context: android.content.Context) -> String?)?>,
    onDraftMessageChanged: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    ScreenWithWarnings(warningsFlow = warningsFlow) {
        SendMessageView(
            state = state,
            onDraftMessageChanged = onDraftMessageChanged,
            onSendMessage = onSendMessage
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WriteTabPreview() {
    MadafakerTheme(mode = Mode.SHINE) {
        WriteTab(
            state = PreviewWriteTabContract(
                draftText = "Write something encouraging...",
                outgoingMessages = PreviewMessages.sampleOutgoing
            ).state.value,
            warningsFlow = MutableStateFlow(null),
            onDraftMessageChanged = {},
            onSendMessage = {}
        )
    }
}


