package org.example.project.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object WritersRoom : Screen()

    @Serializable
    data object RecordingStudio : Screen()

    @Serializable
    data object EditingStudio : Screen()

    @Serializable
    data object PublishingStudio : Screen()

    @Serializable
    data object Archives : Screen()
}