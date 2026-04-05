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
import kotlinx.coroutines.delay
import org.example.project.ui.components.NativeCameraView
import org.example.project.ui.components.StudioBottomNavigationRow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingStudioScreen(
    viewModel: RecordingStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onNavigateToWritersRoom: () -> Unit = onNavigateBack // Default to fallback if not provided
) {
    val activeScript by viewModel.activeScript.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val countdown by viewModel.countdown.collectAsState()

    val currentSegmentText by viewModel.currentSegmentText.collectAsState()
    val isCurrentSegmentBuffer by viewModel.isCurrentSegmentBuffer.collectAsState()
    val totalProgress by viewModel.totalProgress.collectAsState()
    val segments by viewModel.segments.collectAsState()

    var showReRecordDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    // Controls visibility
    var showControls by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(8.dp)) // Themed background
                            .padding(16.dp)
                    )
                } else if (isCountingDown) {
                    Text(
                        text = countdown.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface, // Themed White
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(16.dp)) // Themed background
                            .padding(32.dp)
                    )
                } else if (isRecording) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp)) // Themed background
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
                                    color = Color.White, // Themed White
                                    radius = barHeight, // Ball is slightly larger than bar
                                    center = Offset(ballX, barY)
                                )
                                drawCircle(
                                    color = Color.Black, // Themed Black
                                    radius = barHeight * 0.8f,
                                    center = Offset(ballX, barY)
                                )
                            }
                        }

                        // Current Segment Text
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = currentSegmentText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = if (isCurrentSegmentBuffer) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    // Ready to record state
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp)) // Themed background
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Recording Studio",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface // Themed White
                        )
                    }
                }
            }

            // Bottom Half: Camera & Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            if (isRecording || isCountingDown) {
                                showControls = !showControls
                            }
                        })
                    }
            ) {
                NativeCameraView(
                    modifier = Modifier.fillMaxSize(),
                    isRecording = isRecording,
                    onVideoRecorded = { filePath ->
                        viewModel.onVideoRecorded(filePath)
                    }
                )

                // Controls Overlay
                if (!isRecording && !isCountingDown) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp)) // Themed background
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isFinished || (activeScript?.videoPath != null && !isRecording && !isCountingDown)) {
                            OutlinedButton(
                                onClick = { showReRecordDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary), // Themed Gold
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary) // Themed border
                            ) {
                                Text("Re-Record ↺", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            StudioBottomNavigationRow(
                                onBack = onNavigateBack,
                                onArchive = { showArchiveDialog = true },
                                actionText = "Edit →",
                                onAction = onNavigateForward
                            )
                        } else {
                            StudioBottomNavigationRow(
                                onBack = onNavigateBack,
                                onArchive = { showArchiveDialog = true },
                                actionText = "Start",
                                actionEnabled = activeScript != null,
                                onAction = { viewModel.startRecordingProcess() }
                            )
                        }
                    }
                }
            }
        }

        // Recording Controls Modal Bottom Sheet
        if (showControls && (isRecording || isCountingDown)) {
            ModalBottomSheet(
                onDismissRequest = { showControls = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                scrimColor = Color.Transparent, // Transparent so the user still sees the camera and teleprompter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OutlinedButton(
                            onClick = {
                                viewModel.reset() // Takes us back to WRITERS_ROOM state without destroying
                                onNavigateToWritersRoom()
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        ) {
                            Text("✎ Edit Script", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.stopRecording() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        ) {
                            Text("✓ Stop Early", fontSize = 16.sp)
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
                text = { Text("Are you sure you want to discard this take and record again?", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Themed Light Gray
                confirmButton = {
                    TextButton(onClick = {
                        showReRecordDialog = false
                        viewModel.reset()
                    }) { Text("Yes", color = MaterialTheme.colorScheme.primary) } // Themed Gold
                },
                dismissButton = {
                    TextButton(onClick = { showReRecordDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } // Themed Gray
                },
                containerColor = MaterialTheme.colorScheme.surface, // Themed background
                titleContentColor = MaterialTheme.colorScheme.primary, // Themed Gold
                textContentColor = MaterialTheme.colorScheme.onSurface // Themed White
            )
        }

        if (showArchiveDialog) {
            AlertDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = { Text("Archive Video?") },
                text = { Text("Are you sure you want to archive this video and script?", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Themed Light Gray
                confirmButton = {
                    TextButton(onClick = {
                        showArchiveDialog = false
                        viewModel.archiveScript()
                        onNavigateBack()
                    }) { Text("Yes", color = MaterialTheme.colorScheme.primary) } // Themed Gold
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } // Themed Gray
                },
                containerColor = MaterialTheme.colorScheme.surface, // Themed background
                titleContentColor = MaterialTheme.colorScheme.primary, // Themed Gold
                textContentColor = MaterialTheme.colorScheme.onSurface // Themed White
            )
        }
    }
}
