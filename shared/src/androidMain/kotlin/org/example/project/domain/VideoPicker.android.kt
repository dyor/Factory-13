package org.example.project.domain

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun VideoPicker(
    show: Boolean,
    onVideoPicked: (String?) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Copy to temp file to get absolute path
                val tempFile = File(context.cacheDir, "picked_video_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onVideoPicked(tempFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
                onVideoPicked(null)
            }
        } else {
            onVideoPicked(null)
        }
    }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch("video/*")
        }
    }
}
