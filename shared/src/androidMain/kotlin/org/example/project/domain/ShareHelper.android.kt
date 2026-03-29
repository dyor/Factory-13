package org.example.project.domain

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import org.example.project.di.AppContainer

actual fun shareVideo(videoPath: String) {
    val context = AppContainer.applicationContext as? android.content.Context ?: return
    
    val videoFile = File(videoPath)
    if (!videoFile.exists()) return

    val uri: Uri = try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", 
            videoFile
        )
    } catch (_: IllegalArgumentException) {
        Uri.fromFile(videoFile)
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "Publish Video")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    
    context.startActivity(chooser)
}