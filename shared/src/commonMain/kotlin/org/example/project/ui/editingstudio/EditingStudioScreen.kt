package org.example.project.ui.editingstudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.resolveVideoPath
import org.example.project.ui.components.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
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
    val currentTime by viewModel.currentTimeMs.collectAsState()
    val videoDuration by viewModel.videoDuration.collectAsState()
    val timelineBlocks by viewModel.timelineBlocks.collectAsState()
    val isPreviewingWithoutSkipped by viewModel.isPreviewingWithoutSkipped.collectAsState()
    val isPlaybackCompleted by viewModel.isPlaybackCompleted.collectAsState()

    var showFineTuneModal by remember { mutableStateOf<TimelineBlock?>(null) }
    var selectedBlockIndex by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(showFineTuneModal) {
        if (showFineTuneModal != null) {
            viewModel.pauseVideo()
        }
    }

    LaunchedEffect(timelineBlocks) {
        if (showFineTuneModal != null) {
            // Re-find the block to ensure the modal displays updated sub-block states
            showFineTuneModal = timelineBlocks.firstOrNull { it.secondIndex == showFineTuneModal!!.secondIndex }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editing Studio",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFFD700), // Gold for Noir theme
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (videoPath == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No video found. Please go back to Recording Studio.",
                    color = Color.Red
                )
            }
        } else {
            // Video Player
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
            ) {
                val resolvedPath = resolveVideoPath(videoPath!!)
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

                // Temporal red overlay if the current time is inside a skipped segment
                val isCurrentlySkipped = timelineBlocks.any { block ->
                    block.subBlocks.any { subBlock ->
                        subBlock.isSkipped && currentTime >= subBlock.startTimeMs && currentTime < subBlock.endTimeMs
                    }
                }
                
                if (isCurrentlySkipped) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls & Mock Trimming Controls
            Column(
                modifier = Modifier
                    .weight(0.5f)
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

                // Timeline Visualization
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(timelineBlocks) { block ->
                        val isCurrent = (currentTime / 1000).toInt() == block.secondIndex
                        val isSelected = selectedBlockIndex == block.secondIndex
                        
                        // We only want ONE button to be highlighted.
                        // If something is explicitly selected (modal open), only that one gets highlighted.
                        // Otherwise, the currently playing second gets highlighted.
                        val isActiveOrSelected = if (selectedBlockIndex != null) {
                            isSelected
                        } else {
                            isCurrent
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .fillMaxHeight()
                                .background(
                                    color = when {
                                        block.isFullySkipped -> Color.Red.copy(alpha = 0.6f)
                                        block.isPartiallySkipped -> Color(0xFFFFA500).copy(alpha = 0.6f) // Orange for partial
                                        else -> Color.Gray.copy(alpha = 0.6f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = if (isActiveOrSelected) 2.dp else 0.dp,
                                    color = if (isActiveOrSelected) Color(0xFFFFD700) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    selectedBlockIndex = block.secondIndex
                                    viewModel.seekTo(block.startTimeMs)
                                    showFineTuneModal = block
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${block.secondIndex}s",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val playButtonText = when {
                        isPlaybackCompleted -> "Replay"
                        isPlaying -> "Pause"
                        else -> "Play"
                    }

                    OutlinedButton(
                        onClick = { 
                            if (isPlaybackCompleted) viewModel.replay() else viewModel.togglePlayPause() 
                        }, 
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(playButtonText, maxLines = 1)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.restoreOriginalVideo() }, 
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)), // Red
                        border = BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Text("Restore Original", maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    OutlinedButton(
                        onClick = { 
                            viewModel.saveModifiedVideo {
                                onNavigateForward()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                            contentColor = Color(0xFFFFD700)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text("Publish →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Fine-Tune Modal
        if (showFineTuneModal != null) {
            val block = showFineTuneModal!!
            ModalBottomSheet(
                onDismissRequest = { 
                    showFineTuneModal = null
                    selectedBlockIndex = null // Clear selection when modal closes
                },
                sheetState = sheetState,
                containerColor = Color(0xFF222222)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Fine-Tune Second ${block.secondIndex}", 
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap a 0.1s block to toggle removal (red = skipped).", 
                        color = Color.LightGray, 
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { 
                                viewModel.skipAllSubBlocks(block.secondIndex)
                                showFineTuneModal = viewModel.timelineBlocks.value.getOrNull(block.secondIndex)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("Skip All")
                        }
                        OutlinedButton(
                            onClick = { 
                                viewModel.unskipAllSubBlocks(block.secondIndex)
                                showFineTuneModal = viewModel.timelineBlocks.value.getOrNull(block.secondIndex)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Green),
                            border = BorderStroke(1.dp, Color.Green)
                        ) {
                            Text("Skip None")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        block.subBlocks.forEachIndexed { index, subBlock ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .padding(1.dp)
                                    .background(
                                        if (subBlock.isSkipped) Color.Red else Color.Green,
                                        RoundedCornerShape(2.dp)
                                    )
                                    .clickable {
                                        viewModel.toggleSubBlockSkip(block.secondIndex, index)
                                        showFineTuneModal = viewModel.timelineBlocks.value.getOrNull(block.secondIndex)
                                    }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            showFineTuneModal = null
                            selectedBlockIndex = null // Clear selection when Done is clicked
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}