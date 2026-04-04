package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeCameraView(
    modifier: Modifier,
    isRecording: Boolean,
    onVideoRecorded: (String) -> Unit
)
