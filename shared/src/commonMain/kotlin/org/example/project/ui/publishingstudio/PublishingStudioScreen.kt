package org.example.project.ui.publishingstudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.resolveVideoPath
import org.example.project.ui.components.PlayPauseReplayButton
import org.example.project.ui.components.StudioBottomNavigationRow
import org.example.project.ui.components.VideoPlayer

@Composable
fun PublishingStudioScreen(
    viewModel: PublishingStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val activeScript by viewModel.activeScript.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val seekRequest by viewModel.seekRequest.collectAsState()
    val currentTime by viewModel.currentTimeMs.collectAsState()
    val videoDuration by viewModel.videoDuration.collectAsState()
    val isPlaybackCompleted by viewModel.isPlaybackCompleted.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Publishing Studio",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary, // Gold for Noir theme
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (activeScript?.videoPath == null && activeScript?.publishedVideoPath == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No video to publish.", color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
            }
        } else {
            // Video Player
            Box(
                modifier = Modifier
                    .weight(0.6f) // Take more space for video
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                val videoPath = activeScript!!.publishedVideoPath ?: activeScript!!.videoPath!!
                val resolvedPath = resolveVideoPath(videoPath)
                VideoPlayer(
                    modifier = Modifier.fillMaxSize(),
                    videoPath = resolvedPath,
                    isPlaying = isPlaying,
                    seekRequest = seekRequest,
                    onTimeUpdate = { viewModel.updateCurrentTime(it) },
                    onCompletion = { 
                        viewModel.onVideoCompletion() 
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback and publishing controls
            Column(
                modifier = Modifier
                    .weight(0.4f) // Remaining space
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Time: ${currentTime / 1000}s / ${videoDuration / 1000}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                PlayPauseReplayButton(
                    isPlaying = isPlaying,
                    isPlaybackCompleted = isPlaybackCompleted,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.weight(1f))

                StudioBottomNavigationRow(
                    onBack = onNavigateBack,
                    onArchive = { viewModel.markAsPublished { onNavigateHome() } },
                    actionText = "Export ↑",
                    onAction = { viewModel.shareCurrentVideo() }
                )
            }
        }
    }
}
