package org.example.project.ui.editingstudio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.resolveVideoPath
import org.example.project.ui.components.VideoPlayer

@Composable
fun EditingStudioScreen(
    viewModel: EditingStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit
) {
    val activeScript by viewModel.activeScript.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val seekRequest by viewModel.seekRequest.collectAsState()
    val videoPath by viewModel.videoPath.collectAsState()

    var currentTime by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editing Studio",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (videoPath == null) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No video found. Please go back to Recording Studio.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // Video Player
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
            ) {
                val resolvedPath = resolveVideoPath(videoPath!!)
                VideoPlayer(
                    modifier = Modifier.fillMaxSize(),
                    videoPath = resolvedPath,
                    isPlaying = isPlaying,
                    seekRequest = seekRequest,
                    onTimeUpdate = { currentTime = it },
                    onCompletion = { 
                        if (isPlaying) {
                            viewModel.togglePlayPause() 
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls & Mock Trimming Controls
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Current Time: ${currentTime / 1000}s", color = MaterialTheme.colorScheme.onSurface)
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { viewModel.togglePlayPause() }) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                    Button(onClick = { viewModel.markSectionForRemoval() }) {
                        Text("Mark for Removal")
                    }
                    Button(onClick = { viewModel.restoreOriginalVideo() }) {
                        Text("Restore")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        viewModel.saveModifiedVideo {
                            onNavigateForward()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save & Proceed to Publishing")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}