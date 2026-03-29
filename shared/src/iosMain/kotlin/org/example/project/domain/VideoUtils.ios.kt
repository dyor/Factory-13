package org.example.project.domain

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.CMTimeRangeMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import kotlin.coroutines.resume

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual suspend fun getVideoDuration(videoPath: String): Long {
    val url = NSURL.fileURLWithPath(videoPath)
    val asset = AVURLAsset(uRL = url, options = null)
    val duration = asset.duration
    val durationSeconds = duration.useContents { value.toDouble() / timescale.toDouble() }
    return (durationSeconds * 1000).toLong()
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual suspend fun trimVideo(
    inputPath: String,
    unskippedSegments: List<Pair<Long, Long>>,
    outputPath: String
): Boolean = suspendCancellableCoroutine { continuation ->
    val url = NSURL.fileURLWithPath(inputPath)
    val asset = AVURLAsset(uRL = url, options = null)
    val composition = AVMutableComposition()
    
    val videoTrack = asset.tracksWithMediaType(AVMediaTypeVideo).firstOrNull() as? AVAssetTrack
    val audioTrack = asset.tracksWithMediaType(AVMediaTypeAudio).firstOrNull() as? AVAssetTrack
    
    val compVideoTrack = composition.addMutableTrackWithMediaType(AVMediaTypeVideo, preferredTrackID = 0)
    val compAudioTrack = composition.addMutableTrackWithMediaType(AVMediaTypeAudio, preferredTrackID = 0)

    if (videoTrack != null && compVideoTrack != null) {
        compVideoTrack.preferredTransform = videoTrack.preferredTransform
    }

    var currentTime = CMTimeMake(0, 600)
    
    var success = true
    for (segment in unskippedSegments) {
        val startSeconds = segment.first / 1000.0
        val endSeconds = segment.second / 1000.0
        val durationSeconds = endSeconds - startSeconds
        
        val start = CMTimeMakeWithSeconds(startSeconds, 600)
        val duration = CMTimeMakeWithSeconds(durationSeconds, 600)
        val range = CMTimeRangeMake(start, duration)
        
        val error: kotlinx.cinterop.CPointer<kotlinx.cinterop.ObjCObjectVar<NSError?>>? = null
        if (videoTrack != null && compVideoTrack != null) {
            compVideoTrack.insertTimeRange(range, ofTrack = videoTrack, atTime = currentTime, error = error)
        }
        if (audioTrack != null && compAudioTrack != null) {
            compAudioTrack.insertTimeRange(range, ofTrack = audioTrack, atTime = currentTime, error = error)
        }
        
        if (error != null) {
            success = false
            break
        }
        
        currentTime = platform.CoreMedia.CMTimeAdd(currentTime, duration)
    }

    if (!success) {
        continuation.resume(false)
        return@suspendCancellableCoroutine
    }

    val outputUrl = NSURL.fileURLWithPath(outputPath)
    val exportSession = AVAssetExportSession(asset = composition, presetName = AVAssetExportPresetPassthrough)
    
    exportSession.outputURL = outputUrl
    exportSession.outputFileType = AVFileTypeMPEG4
    exportSession.shouldOptimizeForNetworkUse = true
    
    exportSession.exportAsynchronouslyWithCompletionHandler {
        val finalSuccess = exportSession.status == AVAssetExportSessionStatusCompleted
        continuation.resume(finalSuccess)
    }
}