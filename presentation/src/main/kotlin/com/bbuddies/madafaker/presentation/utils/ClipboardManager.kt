package com.bbuddies.madafaker.presentation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun setText(text: String, label: String = "Firebase Token") {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
    }

    fun getText(): String? {
        return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
    }
}