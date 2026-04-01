package org.example.project.ui.recordingstudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kashif.cameraK.compose.CameraKScreen
import com.kashif.cameraK.compose.rememberCameraKState
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.state.CameraConfiguration
import com.kashif.cameraK.state.CameraKEvent
import com.kashif.cameraK.video.VideoCaptureResult
import com.kashif.videorecorderplugin.rememberVideoRecorderPlugin

import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.film_noir
import org.jetbrains.compose.resources.painterResource

@Composable
fun RecordingStudioScreen(
    viewModel: RecordingStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit
) {
    val activeScript by viewModel.activeScript.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    
    val currentSegmentText by viewModel.currentSegmentText.collectAsState()
    val totalProgress by viewModel.totalProgress.collectAsState()
    val segments by viewModel.segments.collectAsState()

    var showReRecordDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    val videoPlugin = rememberVideoRecorderPlugin()
    val cameraState by rememberCameraKState(
        config = CameraConfiguration(
            cameraLens = CameraLens.FRONT, 
            directory = Directory.DOCUMENTS
        ),
        setupPlugins = { it.attachPlugin(videoPlugin) }
    )

    LaunchedEffect(videoPlugin) {
        videoPlugin.recordingEvents.collect { event ->
            when (event) {
                is CameraKEvent.RecordingStopped -> {
                    val result = event.result
                    if (result is VideoCaptureResult.Success) {
                        println("Video recorded to: ${result.filePath}")
                        viewModel.onVideoRecorded(result.filePath)
                    }
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(isRecording, isFinished) {
        if (isRecording) {
            videoPlugin.startRecording()
        } else if (isFinished) {
            videoPlugin.stopRecording()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.film_noir),
            contentDescription = "Film Noir Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Half: Teleprompter & Counters
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeScript == null) {
                    Text(
                        text = "No active script found. Go back and generate one.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    )
                } else if (isCountingDown) {
                    Text(
                        text = countdown.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(32.dp)
                    )
                } else if (isRecording) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        // Custom segment progress bar
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barHeight = 12.dp.toPx()
                            val barY = canvasHeight / 2f
                            
                            val segmentColors = listOf(
                                Color(0xFFFF5252), // Red
                                Color(0xFFFFD54F), // Yellow
                                Color(0xFF81C784), // Green
                                Color(0xFF64B5F6), // Blue
                                Color(0xFFBA68C8)  // Purple
                            )
                            
                            if (segments.isNotEmpty()) {
                                val totalVideoTime = segments.last().endTimeSec
                                var currentStartX = 0f
                                
                                // Draw segments
                                segments.forEachIndexed { index, seg ->
                                    val segDuration = seg.endTimeSec - seg.startTimeSec
                                    val segWidth = (segDuration.toFloat() / totalVideoTime.toFloat()) * canvasWidth
                                    val color = segmentColors[index % segmentColors.size]
                                    
                                    drawLine(
                                        color = color,
                                        start = Offset(currentStartX, barY),
                                        end = Offset(currentStartX + segWidth, barY),
                                        strokeWidth = barHeight,
                                        cap = StrokeCap.Round
                                    )
                                    currentStartX += segWidth
                                }
                                
                                // Draw ball
                                val ballX = canvasWidth * totalProgress
                                drawCircle(
                                    color = Color.White,
                                    radius = barHeight, // Ball is slightly larger than bar
                                    center = Offset(ballX, barY)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = barHeight * 0.8f,
                                    center = Offset(ballX, barY)
                                )
                            }
                        }

                        // Current Segment Text
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = currentSegmentText,
                                style = MaterialTheme.typography.titleMedium, // Smaller font for fitting
                                color = Color(0xFFFFD700), // Gold/yellowish for noir contrast
                                textAlign = TextAlign.Center,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                } else {
                    // Ready to record state
                    Text(
                        text = "Recording Studio",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    )
                }
            }

            // Bottom Half: Camera & Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                CameraKScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = false, onClick = {}),
                    cameraState = cameraState,
                    showPreview = true,
                    content = { _ -> }
                )

                // Controls Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isFinished || (activeScript?.videoPath != null && !isRecording && !isCountingDown)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Re-Record button
                            OutlinedButton(
                                onClick = { showReRecordDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text("↺", fontSize = 20.sp)
                            }
                            
                            // Archive button
                            OutlinedButton(
                                onClick = { showArchiveDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text("↓", fontSize = 20.sp)
                            }

                            // Proceed to Editing button
                            OutlinedButton(
                                onClick = onNavigateForward,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFFFD700)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text("Edit →")
                            }
                        }
                    } else if (!isRecording && !isCountingDown) {
                        OutlinedButton(
                            onClick = { viewModel.startRecordingProcess() },
                            enabled = activeScript != null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFFD700),
                                disabledContentColor = Color.Gray
                            ),
                            border = BorderStroke(1.dp, if(activeScript != null) Color(0xFFFFD700) else Color.Gray)
                        ) {
                            Text("Start Recording")
                        }
                    } else if (isRecording) {
                        OutlinedButton(
                            onClick = { viewModel.stopRecording() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xAA8B0000), // Dark Red for recording
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("Stop Early", color = Color.White)
                        }
                    }

                    if (!isRecording && !isCountingDown) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text("←")
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showReRecordDialog) {
            AlertDialog(
                onDismissRequest = { showReRecordDialog = false },
                title = { Text("Re-Record?") },
                text = { Text("Are you sure you want to discard this take and record again?") },
                confirmButton = {
                    TextButton(onClick = {
                        showReRecordDialog = false
                        viewModel.reset()
                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showReRecordDialog = false }) { Text("Cancel") }
                },
                containerColor = Color(0xFF222222),
                titleContentColor = Color(0xFFFFD700),
                textContentColor = Color.White
            )
        }

        if (showArchiveDialog) {
            AlertDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = { Text("Archive Video?") },
                text = { Text("Are you sure you want to archive this video and script?") },
                confirmButton = {
                    TextButton(onClick = {
                        showArchiveDialog = false
                        viewModel.archiveScript()
                        onNavigateBack()
                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel") }
                },
                containerColor = Color(0xFF222222),
                titleContentColor = Color(0xFFFFD700),
                textContentColor = Color.White
            )
        }
    }
}
