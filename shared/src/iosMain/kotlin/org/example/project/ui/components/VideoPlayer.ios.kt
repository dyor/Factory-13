package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.useContents
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    videoPath: String,
    isPlaying: Boolean,
    seekRequest: Long?,
    onTimeUpdate: (Long) -> Unit,
    onCompletion: () -> Unit
) {
    val player = remember { AVPlayer() }
    val playerViewController = remember { AVPlayerViewController() }

    LaunchedEffect(videoPath) {
        if (videoPath.isNotBlank()) {
            val url = NSURL.fileURLWithPath(videoPath)
            val playerItem = AVPlayerItem(uRL = url)
            player.replaceCurrentItemWithPlayerItem(playerItem)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }

    LaunchedEffect(seekRequest) {
        seekRequest?.let {
            // Convert ms to seconds
            val time = CMTimeMakeWithSeconds(it / 1000.0, 600)
            player.seekToTime(time)
            delay(200) // Debounce async seek callbacks
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            if (player.rate > 0.0) {
                val time = player.currentTime()
                val seconds = time.useContents { value.toDouble() / timescale.toDouble() }
                onTimeUpdate((seconds * 1000).toLong())
            }
        }
    }

    UIKitView(
        factory = {
            playerViewController.player = player
            playerViewController.showsPlaybackControls = false
            playerViewController.view
        },
        modifier = modifier,
        update = { view ->
            // Empty update block
        }
    )
}