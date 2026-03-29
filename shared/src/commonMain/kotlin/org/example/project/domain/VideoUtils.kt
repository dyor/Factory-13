package org.example.project.domain

/**
 * Expected functions to access native media processing capabilities.
 */
expect suspend fun getVideoDuration(videoPath: String): Long

expect suspend fun trimVideo(
    inputPath: String, 
    unskippedSegments: List<Pair<Long, Long>>, 
    outputPath: String
): Boolean