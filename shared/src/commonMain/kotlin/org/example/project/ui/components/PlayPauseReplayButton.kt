package org.example.project.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayPauseReplayButton(
    isPlaying: Boolean,
    isPlaybackCompleted: Boolean,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playButtonText = when {
        isPlaybackCompleted -> "⎌"
        isPlaying -> "■"
        else -> "▶"
    }

    OutlinedButton(
        onClick = onTogglePlayPause,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(playButtonText, maxLines = 1, fontSize = 20.sp)
    }
}
