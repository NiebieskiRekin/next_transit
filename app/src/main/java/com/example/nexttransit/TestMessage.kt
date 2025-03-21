package com.example.nexttransit

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Button({
        scope.launch {
            val localToken = Firebase.messaging.token.await()
            clipboardManager.setText(AnnotatedString(localToken))
        }
    }, content = { Text("Copy firebase token to clipboard") })
}