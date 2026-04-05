package org.example.project.domain

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.CMTimeRangeMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.QuartzCore.CABasicAnimation
import platform.QuartzCore.CALayer
import platform.QuartzCore.CATextLayer
import platform.QuartzCore.kCAAlignmentCenter
import platform.UIKit.UIColor
import platform.Foundation.NSNumber
import kotlin.coroutines.resume
import kotlinx.cinterop.readValue

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual val isCaptionsSupported: Boolean = true

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
    outputPath: String,
    captions: List<CaptionInfo>,
    captionPosition: CaptionPosition
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
    
    // Map captions to the new timeline
    val mappedCaptions = mutableListOf<CaptionInfo>()
    var currentCompMs = 0L
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
        
        // Process captions for this segment
        val segDur = segment.second - segment.first
        for (caption in captions) {
            val overlapStart = maxOf(caption.startTimeMs, segment.first)
            val overlapEnd = minOf(caption.endTimeMs, segment.second)
            
            if (overlapStart < overlapEnd) {
                val mappedStart = currentCompMs + (overlapStart - segment.first)
                val mappedEnd = currentCompMs + (overlapEnd - segment.first)
                mappedCaptions.add(CaptionInfo(mappedStart, mappedEnd, caption.text))
            }
        }
        currentCompMs += segDur
        currentTime = platform.CoreMedia.CMTimeAdd(currentTime, duration)
    }

    if (!success || compVideoTrack == null || videoTrack == null) {
        continuation.resume(false)
        return@suspendCancellableCoroutine
    }

    val naturalSize = videoTrack.naturalSize
    val transform = videoTrack.preferredTransform
    val isRotated = transform.useContents { a == 0.0 && d == 0.0 }
    val renderWidth = if (isRotated) naturalSize.useContents { height } else naturalSize.useContents { width }
    val renderHeight = if (isRotated) naturalSize.useContents { width } else naturalSize.useContents { height }

    // Build the video composition
    val videoComposition = AVMutableVideoComposition.videoComposition()
    videoComposition.setRenderSize(CGSizeMake(renderWidth, renderHeight))
    videoComposition.setFrameDuration(CMTimeMake(1, 30))

    val instruction = AVMutableVideoCompositionInstruction.videoCompositionInstruction()
    instruction.setTimeRange(CMTimeRangeMake(platform.CoreMedia.kCMTimeZero.readValue(), composition.duration))

    val layerInstruction = AVMutableVideoCompositionLayerInstruction.videoCompositionLayerInstructionWithAssetTrack(compVideoTrack)
    layerInstruction.setTransform(videoTrack.preferredTransform, atTime = platform.CoreMedia.kCMTimeZero.readValue())
    instruction.setLayerInstructions(listOf(layerInstruction))
    videoComposition.setInstructions(listOf(instruction))

    // Add CoreAnimation layers for captions if any exist
    if (mappedCaptions.isNotEmpty()) {
        val parentLayer = CALayer.layer()
        val videoLayer = CALayer.layer()
        val overlayLayer = CALayer.layer()
        
        parentLayer.frame = CGRectMake(0.0, 0.0, renderWidth, renderHeight)
        videoLayer.frame = parentLayer.bounds
        overlayLayer.frame = parentLayer.bounds
        
        parentLayer.addSublayer(videoLayer)
        parentLayer.addSublayer(overlayLayer)
        
        for (caption in mappedCaptions) {
            val textLayer = CATextLayer.layer()
            textLayer.string = caption.text
            val fontSize = renderHeight * 0.035 // Reduced font size for better fit
            textLayer.fontSize = fontSize
            textLayer.alignmentMode = kCAAlignmentCenter
            textLayer.foregroundColor = UIColor.whiteColor.CGColor
            textLayer.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.6).CGColor
            
            textLayer.wrapped = true
            textLayer.cornerRadius = 16.0
            textLayer.masksToBounds = true
            
            // To prevent text from being too close to the edges of the box, we can use a container
            val containerLayer = CALayer.layer()
            containerLayer.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.6).CGColor
            containerLayer.cornerRadius = 16.0
            containerLayer.masksToBounds = true

            // Set the background of text layer to transparent so the container handles the background
            textLayer.backgroundColor = UIColor.clearColor.CGColor
            
            val padding = 40.0
            val maxWidth = renderWidth - (padding * 2.0)
            
            // Estimate width and height based on system font proportions.
            val estimatedCharWidth = fontSize * 0.6
            val textLen = caption.text.length
            val estimatedLineWidth = textLen * estimatedCharWidth
            
            // Inner padding for the container
            val horizontalPadding = 32.0
            val verticalPadding = 24.0

            val textMaxWidth = maxWidth - horizontalPadding
            val finalWidth = if (estimatedLineWidth < textMaxWidth) estimatedLineWidth + horizontalPadding else maxWidth
            
            val maxCharsPerLine = (textMaxWidth / estimatedCharWidth)
            val numLines = kotlin.math.ceil(textLen / maxCharsPerLine).toInt().coerceAtLeast(1)
            
            // CATextLayer draws from the top. We calculate the height to match the text height + minimum padding.
            val textHeight = (numLines * fontSize * 1.2)
            val finalHeight = textHeight + verticalPadding
            
            val xOffset = (renderWidth - finalWidth) / 2.0
            
            val yOffset = if (captionPosition == CaptionPosition.TOP) {
                renderHeight * 0.70 // Brought top captions down slightly
            } else {
                renderHeight * 0.05 // Brought bottom captions much lower
            }
            
            containerLayer.frame = CGRectMake(xOffset, yOffset, finalWidth, finalHeight)
            
            // Vertically center the text within the container
            val textYOffset = (finalHeight - textHeight) / 2.0
            textLayer.frame = CGRectMake(horizontalPadding / 2.0, textYOffset, finalWidth - horizontalPadding, textHeight)
            
            containerLayer.addSublayer(textLayer)
            containerLayer.opacity = 0.0f
            
            val anim = CABasicAnimation.animationWithKeyPath("opacity")
            anim.fromValue = NSNumber(1.0)
            anim.toValue = NSNumber(1.0)
            anim.beginTime = platform.AVFoundation.AVCoreAnimationBeginTimeAtZero + (caption.startTimeMs / 1000.0)
            anim.duration = (caption.endTimeMs - caption.startTimeMs) / 1000.0
            anim.removedOnCompletion = true
            
            containerLayer.addAnimation(anim, forKey = "opacityAnim")
            overlayLayer.addSublayer(containerLayer)
        }
        
        videoComposition.setAnimationTool(AVVideoCompositionCoreAnimationTool.videoCompositionCoreAnimationToolWithPostProcessingAsVideoLayer(videoLayer, inLayer = parentLayer))
    }

    val outputUrl = NSURL.fileURLWithPath(outputPath)
    
    // Fall back to Passthrough if we aren't adding captions, 
    // as it avoids the CameraRollValidation issue on iOS when simply trimming.
    val preset = if (mappedCaptions.isNotEmpty()) AVAssetExportPresetHighestQuality else AVAssetExportPresetPassthrough
    val exportSession = AVAssetExportSession(asset = composition, presetName = preset)
    
    exportSession.outputURL = outputUrl
    exportSession.outputFileType = AVFileTypeMPEG4
    if (mappedCaptions.isNotEmpty()) {
        exportSession.videoComposition = videoComposition
    }
    exportSession.shouldOptimizeForNetworkUse = true
    
    exportSession.exportAsynchronouslyWithCompletionHandler {
        val finalSuccess = exportSession.status == AVAssetExportSessionStatusCompleted
        if (!finalSuccess) {
            println("Export failed with error: ${exportSession.error?.localizedDescription}")
        }
        continuation.resume(finalSuccess)
    }
}
