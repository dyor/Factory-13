package org.example.project.domain

/**
 * Provides a platform-agnostic way to trigger the native share sheet or 
 * open a specific app (like YouTube) for video publishing.
 */
expect fun shareVideo(videoPath: String)