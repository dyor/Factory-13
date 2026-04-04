package org.example.project.domain

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

actual suspend fun getVideoDuration(videoPath: String): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(videoPath)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        time?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}

actual suspend fun trimVideo(
    inputPath: String,
    unskippedSegments: List<Pair<Long, Long>>,
    outputPath: String,
    captions: List<CaptionInfo>,
    captionPosition: CaptionPosition
): Boolean {
    val extractor = MediaExtractor()
    var muxer: MediaMuxer? = null
    
    return try {
        extractor.setDataSource(inputPath)
        
        // Find Video and Audio Tracks
        var videoTrackIndex = -1
        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("video/")) {
                videoTrackIndex = i
            } else if (mime.startsWith("audio/")) {
                audioTrackIndex = i
            }
        }
        
        if (videoTrackIndex == -1) return false
        
        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        
        // Add Tracks
        val videoFormat = extractor.getTrackFormat(videoTrackIndex)
        val outputVideoTrack = muxer.addTrack(videoFormat)
        
        var outputAudioTrack = -1
        if (audioTrackIndex != -1) {
            val audioFormat = extractor.getTrackFormat(audioTrackIndex)
            outputAudioTrack = muxer.addTrack(audioFormat)
        }
        
        // Transfer orientation metadata if available
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(inputPath)
            val rotationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            if (rotationString != null) {
                muxer.setOrientationHint(rotationString.toInt())
            }
        } catch (_: Exception) {} finally {
            retriever.release()
        }

        muxer.start()
        
        val maxChunkSize = 1024 * 1024
        val buffer = ByteBuffer.allocate(maxChunkSize)
        val bufferInfo = MediaCodec.BufferInfo()
        
        val lastWrittenPts = mutableMapOf<Int, Long>()
        
        for (segment in unskippedSegments) {
            val (startMs, endMs) = segment
            val startUs = startMs * 1000L
            val endUs = endMs * 1000L
            
            // Note: Simplistic slicing. Real trimming should seek to SYNC_FRAME on video
            extractor.selectTrack(videoTrackIndex)
            if (audioTrackIndex != -1) extractor.selectTrack(audioTrackIndex)
            
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            
            while (true) {
                val trackIndex = extractor.sampleTrackIndex
                if (trackIndex < 0) break
                
                val pts = extractor.sampleTime
                if (pts >= endUs) break
                
                if (pts >= startUs) {
                    val outIndex = if (trackIndex == videoTrackIndex) outputVideoTrack else outputAudioTrack
                    if (outIndex >= 0) {
                        val lastPts = lastWrittenPts[outIndex] ?: -1L
                        if (pts > lastPts) { // Enforce monotonic timestamps
                            val size = extractor.readSampleData(buffer, 0)
                            if (size > 0) {
                                bufferInfo.offset = 0
                                bufferInfo.size = size
                                bufferInfo.presentationTimeUs = pts
                                bufferInfo.flags = extractor.sampleFlags
                                muxer.writeSampleData(outIndex, buffer, bufferInfo)
                                lastWrittenPts[outIndex] = pts
                            }
                        }
                    }
                }
                extractor.advance()
            }
        }
        
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        try { extractor.release() } catch (_: Exception) {}
        try { 
            muxer?.stop()
            muxer?.release() 
        } catch (_: Exception) {}
    }
}