package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoPath: String,
    isPlaying: Boolean,
    seekRequest: Long? = null,
    onTimeUpdate: (Long) -> Unit = {},
    onCompletion: () -> Unit = {}
)