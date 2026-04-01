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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import org.example.project.domain.resolveVideoPath
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
            color = Color(0xFFFFD700), // Gold for Noir theme
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (activeScript?.videoPath == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No video to publish.", color = Color.Red, fontSize = 18.sp)
            }
        } else {
            // Video Player
            Box(
                modifier = Modifier
                    .weight(0.6f) // Take more space for video
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                val videoPath = activeScript!!.videoPath!!
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
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Time: ${currentTime / 1000}s / ${videoDuration / 1000}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                val playButtonText = when {
                    isPlaybackCompleted -> "Replay"
                    isPlaying -> "Pause"
                    else -> "Play"
                }

                OutlinedButton(
                    onClick = { viewModel.togglePlayPause() }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text(playButtonText, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.shareCurrentVideo() },
                        modifier = Modifier.fillMaxWidth().height(48.dp), // Smaller height
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                            contentColor = Color(0xFFFFD700)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text("Export / Share ↑", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack, 
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text("←", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.markAsPublished { onNavigateHome() } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text("↓ Archive", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
            }
        }
    }
}