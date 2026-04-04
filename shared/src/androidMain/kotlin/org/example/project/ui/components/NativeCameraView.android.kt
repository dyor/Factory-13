package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kashif.cameraK.compose.CameraKScreen
import com.kashif.cameraK.compose.rememberCameraKState
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.state.CameraConfiguration
import com.kashif.cameraK.state.CameraKEvent
import com.kashif.cameraK.video.VideoCaptureResult
import com.kashif.videorecorderplugin.rememberVideoRecorderPlugin

@Composable
actual fun NativeCameraView(
    modifier: Modifier,
    isRecording: Boolean,
    onVideoRecorded: (String) -> Unit
) {
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
                        onVideoRecorded(result.filePath)
                    }
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            videoPlugin.startRecording()
        } else if (!isRecording && videoPlugin.isRecording) {
            videoPlugin.stopRecording()
        }
    }

    CameraKScreen(
        modifier = modifier,
        cameraState = cameraState,
        showPreview = true,
        content = { _ -> }
    )
}
