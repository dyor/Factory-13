package org.example.project.domain

import androidx.compose.runtime.Composable

@Composable
expect fun VideoPicker(
    show: Boolean,
    onVideoPicked: (String?) -> Unit
)
