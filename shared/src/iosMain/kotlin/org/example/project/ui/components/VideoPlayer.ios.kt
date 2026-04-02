package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
import platform.AVFAudio.*

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
    val currentOnCompletion by androidx.compose.runtime.rememberUpdatedState(onCompletion)

    LaunchedEffect(videoPath) {
        if (videoPath.isNotBlank()) {
            val url = NSURL.fileURLWithPath(videoPath)
            val playerItem = AVPlayerItem(uRL = url)
            player.replaceCurrentItemWithPlayerItem(playerItem)
            
            // Ensure audio plays even if the physical switch is set to silent
            val audioSession = AVAudioSession.sharedInstance()
            try {
                audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
                audioSession.setActive(true, null)
            } catch (e: Exception) {
                println("Failed to set audio session category: ${e.message}")
            }
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

    LaunchedEffect(isPlaying) {
        while (true) {
            delay(100)
            if (player.rate > 0.0) {
                val time = player.currentTime()
                val seconds = time.useContents { value.toDouble() / timescale.toDouble() }
                onTimeUpdate((seconds * 1000).toLong())
            } else if (isPlaying) {
                val currentItem = player.currentItem
                if (currentItem != null) {
                    val duration = currentItem.duration.useContents { value.toDouble() / timescale.toDouble() }
                    val time = player.currentTime()
                    val seconds = time.useContents { value.toDouble() / timescale.toDouble() }
                    if (duration > 0.0 && seconds >= duration - 0.5) {
                        currentOnCompletion()
                    }
                }
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            player.pause()
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