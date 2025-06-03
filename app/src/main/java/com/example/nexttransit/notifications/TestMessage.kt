package com.example.nexttransit.notifications

import android.content.ClipData
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CopyFirebaseToken() {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Button({
        scope.launch {
            val localToken = Firebase.messaging.token.await()
            var clipData: ClipData = ClipData.newPlainText("firebase token", localToken)
            clipboardManager.setClipEntry(ClipEntry(clipData))
        }
    }, content = { Text("Copy firebase token to clipboard") })
}