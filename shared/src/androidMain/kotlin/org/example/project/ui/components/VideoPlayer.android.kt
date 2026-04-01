package org.example.project.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    videoPath: String,
    isPlaying: Boolean,
    seekRequest: Long?,
    onTimeUpdate: (Long) -> Unit,
    onCompletion: () -> Unit
) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }

    LaunchedEffect(videoPath) {
        if (videoPath.isNotBlank()) {
            videoView.setVideoURI(Uri.parse(videoPath))
            videoView.setOnPreparedListener { mp ->
                mp.setVolume(1f, 1f) // Ensure audio is playing
                if (isPlaying) videoView.start()
            }
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            videoView.start()
        } else {
            videoView.pause()
        }
    }

    LaunchedEffect(seekRequest) {
        seekRequest?.let {
            videoView.seekTo(it.toInt())
            delay(200) // Debounce async seek callbacks
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            if (videoView.isPlaying) {
                onTimeUpdate(videoView.currentPosition.toLong())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoView.stopPlayback()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { 
            videoView.apply {
                setOnCompletionListener { onCompletion() }
            }
        },
        update = { 
            // Empty update block to prevent recomposition loops
        }
    )
}