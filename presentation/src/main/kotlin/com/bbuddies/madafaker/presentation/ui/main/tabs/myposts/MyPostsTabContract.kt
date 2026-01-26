package com.bbuddies.madafaker.presentation.ui.main.tabs.myposts

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface MyPostsTabContract {
    val state: StateFlow<MyPostsTabState>
    val warningsFlow: StateFlow<((context: Context) -> String?)?>

    fun refreshMessages()
}
