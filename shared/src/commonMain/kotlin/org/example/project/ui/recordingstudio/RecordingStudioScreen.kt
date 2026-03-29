package org.example.project.ui.recordingstudio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kashif.cameraK.compose.CameraKScreen
import com.kashif.cameraK.compose.rememberCameraKState
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.state.CameraConfiguration
import com.kashif.cameraK.state.CameraKEvent
import com.kashif.cameraK.video.VideoCaptureResult
import com.kashif.videorecorderplugin.rememberVideoRecorderPlugin

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
    val visibleLines by viewModel.visibleLines.collectAsState()

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

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Half: Teleprompter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (activeScript == null) {
                Text("No active script found. Go back and generate one.", color = MaterialTheme.colorScheme.error)
            } else if (isCountingDown) {
                Text(
                    text = countdown.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    visibleLines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
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
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFinished) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { viewModel.reset() }) {
                            Text("Re-Record")
                        }
                        Button(onClick = onNavigateForward) {
                            Text("Proceed to Editing")
                        }
                    }
                } else if (!isRecording && !isCountingDown) {
                    Button(
                        onClick = { viewModel.startRecordingProcess() },
                        enabled = activeScript != null
                    ) {
                        Text("Start Recording")
                    }
                } else if (isRecording) {
                    Button(onClick = { viewModel.stopRecording() }) {
                        Text("Stop Early")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
    }
}