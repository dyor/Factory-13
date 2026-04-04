package org.example.project.domain

enum class CaptionPosition {
    TOP, BOTTOM, NONE
}

data class CaptionInfo(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

/**
 * Expected functions to access native media processing capabilities.
 */
expect suspend fun getVideoDuration(videoPath: String): Long

expect suspend fun trimVideo(
    inputPath: String, 
    unskippedSegments: List<Pair<Long, Long>>, 
    outputPath: String,
    captions: List<CaptionInfo> = emptyList(),
    captionPosition: CaptionPosition = CaptionPosition.BOTTOM
): Boolean