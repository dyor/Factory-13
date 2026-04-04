package org.example.project.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script")
data class Script(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String = "",
    val content: String,
    val targetDuration: Int? = null,
    val isActive: Boolean = false,
    val scriptState: String = "WRITERS_ROOM",
    val videoPath: String? = null, // The raw/original video
    val publishedVideoPath: String? = null, // The final output of the Editing Studio
    val skippedSegmentsJson: String = ""
)