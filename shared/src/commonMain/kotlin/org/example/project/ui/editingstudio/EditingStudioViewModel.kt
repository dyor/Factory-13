package org.example.project.ui.editingstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.CaptionInfo
import org.example.project.domain.ScriptDao
import org.example.project.domain.getVideoDuration
import org.example.project.domain.resolveVideoPath
import org.example.project.domain.trimVideo

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
data class TimelineSubBlock(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val isSkipped: Boolean = false
)

@Serializable
data class TimelineBlock(
    val secondIndex: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val subBlocks: List<TimelineSubBlock>,
    val isBuffer: Boolean = false
) {
    val isFullySkipped: Boolean
        get() = subBlocks.all { it.isSkipped }
        
    val isPartiallySkipped: Boolean
        get() = subBlocks.any { it.isSkipped } && !isFullySkipped
}

class EditingStudioViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {

    private val _activeScript = MutableStateFlow<Script?>(null)
    val activeScript: StateFlow<Script?> = _activeScript.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _seekRequest = MutableStateFlow<Long?>(null)
    val seekRequest: StateFlow<Long?> = _seekRequest.asStateFlow()
    
    private val _videoPath = MutableStateFlow<String?>(null)
    val videoPath: StateFlow<String?> = _videoPath.asStateFlow()

    private val _videoDuration = MutableStateFlow<Long>(0L)
    val videoDuration: StateFlow<Long> = _videoDuration.asStateFlow()
    
    private val _timelineBlocks = MutableStateFlow<List<TimelineBlock>>(emptyList())
    val timelineBlocks: StateFlow<List<TimelineBlock>> = _timelineBlocks.asStateFlow()

    private val _currentTimeMs = MutableStateFlow<Long>(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _isPlaybackCompleted = MutableStateFlow(false)
    val isPlaybackCompleted: StateFlow<Boolean> = _isPlaybackCompleted.asStateFlow()

    private val _isPreviewingWithoutSkipped = MutableStateFlow(false)
    val isPreviewingWithoutSkipped: StateFlow<Boolean> = _isPreviewingWithoutSkipped.asStateFlow()

    fun updateCurrentTime(timeMs: Long) {
        _currentTimeMs.value = timeMs
        
        if (_isPreviewingWithoutSkipped.value && _isPlaying.value) {
            val blocks = _timelineBlocks.value
            val unskipped = getUnskippedSegments(blocks)
            val isInsideSkipped = unskipped.none { timeMs >= it.first && timeMs < it.second }
            if (isInsideSkipped) {
                val nextUnskipped = unskipped.firstOrNull { it.first >= timeMs }
                if (nextUnskipped != null) {
                    seekTo(nextUnskipped.first)
                } else {
                    _isPlaying.value = false
                    _isPreviewingWithoutSkipped.value = false
                    _isPlaybackCompleted.value = true
                }
            }
        }
    }

    fun onVideoCompletion() {
        _isPlaying.value = false
        _isPlaybackCompleted.value = true
        _isPreviewingWithoutSkipped.value = false
    }

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                // Update script state if it isn't already EDITING_STUDIO or further along
                if (script != null && script.scriptState != "EDITING_STUDIO" && script.scriptState != "PUBLISHING_STUDIO") {
                    val updatedScript = script.copy(scriptState = "EDITING_STUDIO")
                    scriptDao.update(updatedScript)
                    // The flow will emit again with the updated script
                    return@collect
                }

                _activeScript.value = script
                _videoPath.value = script?.videoPath
                script?.videoPath?.let { path ->
                    try {
                        val resolvedPath = resolveVideoPath(path)
                        val duration = getVideoDuration(resolvedPath)
                        _videoDuration.value = duration
                        
                        if (!script.skippedSegmentsJson.isNullOrBlank()) {
                            try {
                                _timelineBlocks.value = Json.decodeFromString(script.skippedSegmentsJson)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                generateTimeline(duration, script.content)
                            }
                        } else {
                            generateTimeline(duration, script.content)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun generateTimeline(durationMs: Long, scriptContent: String? = null) {
        val blocks = mutableListOf<TimelineBlock>()
        val totalSeconds = kotlin.math.ceil(durationMs / 1000.0).toInt()
        
        // Parse the script to identify buffer seconds
        val bufferSeconds = mutableSetOf<Int>()
        if (scriptContent != null) {
            val segments = parseScriptForBuffers(scriptContent)
            for (seg in segments) {
                if (seg.isBuffer) {
                    for (s in seg.startTimeSec until seg.endTimeSec) {
                        bufferSeconds.add(s)
                    }
                }
            }
        }
        
        for (sec in 0 until totalSeconds) {
            val secStart = sec * 1000L
            val secEnd = minOf(secStart + 1000L, durationMs)
            
            val subBlocks = mutableListOf<TimelineSubBlock>()
            for (tenth in 0 until 10) {
                val subStart = secStart + (tenth * 100L)
                val subEnd = minOf(subStart + 100L, secEnd)
                if (subStart < secEnd) {
                    subBlocks.add(TimelineSubBlock(subStart, subEnd))
                }
            }
            blocks.add(TimelineBlock(sec, secStart, secEnd, subBlocks, isBuffer = bufferSeconds.contains(sec)))
        }
        _timelineBlocks.value = blocks
    }

    // Helper to parse script structure exactly as RecordingStudioViewModel does to find buffer blocks
    private data class ParsedSegment(val startTimeSec: Int, val endTimeSec: Int, val text: String, val isBuffer: Boolean)
    
    private fun parseScriptForBuffers(content: String): List<ParsedSegment> {
        val regex = Regex("""^(\d+)s-(\d+)s:?\s*(.*)$""")
        val lines = content.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val parsedSegments = mutableListOf<ParsedSegment>()
        
        var runningTime = 0
        
        for ((index, line) in lines.withIndex()) {
            val match = regex.find(line)
            val text: String
            val duration: Int
            
            if (match != null) {
                val parsedStart = match.groupValues[1].toInt()
                val parsedEnd = match.groupValues[2].toInt()
                duration = maxOf(1, parsedEnd - parsedStart)
                text = match.groupValues[3]
            } else {
                duration = 5
                text = line
            }
            
            val segStart = runningTime
            val segEnd = runningTime + duration
            parsedSegments.add(ParsedSegment(segStart, segEnd, text, isBuffer = false))
            runningTime = segEnd
            
            // Add a 2-second buffer between segments
            if (index < lines.lastIndex) {
                parsedSegments.add(ParsedSegment(runningTime, runningTime + 2, text, isBuffer = true))
                runningTime += 2
            }
        }
        
        if (parsedSegments.isNotEmpty()) {
            val lastTime = parsedSegments.last().endTimeSec
            parsedSegments.add(ParsedSegment(lastTime, lastTime + 5, "...and cut!", isBuffer = true))
        }
        
        return parsedSegments
    }

    fun togglePlayPause() {
        if (_isPlaybackCompleted.value) {
            _isPlaybackCompleted.value = false
            seekTo(0L)
            _isPlaying.value = true
        } else {
            _isPlaying.value = !_isPlaying.value
        }
        if (!_isPlaying.value) {
            _isPreviewingWithoutSkipped.value = false
        }
    }

    fun replay() {
        _isPlaybackCompleted.value = false
        seekTo(0L)
        _isPlaying.value = true
    }

    fun pauseVideo() {
        _isPlaying.value = false
        _isPreviewingWithoutSkipped.value = false
    }

    fun startPreviewWithoutSkipped() {
        val unskipped = getUnskippedSegments(_timelineBlocks.value)
        if (unskipped.isNotEmpty()) {
            _isPreviewingWithoutSkipped.value = true
            seekTo(unskipped.first().first)
            _isPlaying.value = true
        }
    }

    fun seekTo(positionMs: Long) {
        _seekRequest.value = positionMs
        _currentTimeMs.value = positionMs // Optimistically update time to force UI refresh immediately
        _isPlaybackCompleted.value = false
    }
    
    fun clearSeekRequest() {
        _seekRequest.value = null
    }

    fun toggleSubBlockSkip(secondIndex: Int, subBlockIndex: Int) {
        val currentBlocks = _timelineBlocks.value.toMutableList()
        val block = currentBlocks.getOrNull(secondIndex) ?: return
        
        val subBlocks = block.subBlocks.toMutableList()
        val subBlock = subBlocks.getOrNull(subBlockIndex) ?: return
        
        subBlocks[subBlockIndex] = subBlock.copy(isSkipped = !subBlock.isSkipped)
        currentBlocks[secondIndex] = block.copy(subBlocks = subBlocks)
        
        _timelineBlocks.value = currentBlocks
        
        // Ensure playback stops and we seek to the exact start of this 0.1s block
        _isPlaying.value = false
        _isPreviewingWithoutSkipped.value = false
        seekTo(subBlock.startTimeMs)
    }

    fun skipAllSubBlocks(secondIndex: Int) {
        val currentBlocks = _timelineBlocks.value.toMutableList()
        val block = currentBlocks.getOrNull(secondIndex) ?: return
        val skippedSubBlocks = block.subBlocks.map { it.copy(isSkipped = true) }
        currentBlocks[secondIndex] = block.copy(subBlocks = skippedSubBlocks)
        _timelineBlocks.value = currentBlocks
        seekTo(block.startTimeMs)
        _isPlaying.value = false
        _isPreviewingWithoutSkipped.value = false
    }

    fun unskipAllSubBlocks(secondIndex: Int) {
        val currentBlocks = _timelineBlocks.value.toMutableList()
        val block = currentBlocks.getOrNull(secondIndex) ?: return
        val unskippedSubBlocks = block.subBlocks.map { it.copy(isSkipped = false) }
        currentBlocks[secondIndex] = block.copy(subBlocks = unskippedSubBlocks)
        _timelineBlocks.value = currentBlocks
        seekTo(block.startTimeMs)
        _isPlaying.value = false
        _isPreviewingWithoutSkipped.value = false
    }

    private val _captionPosition = MutableStateFlow(org.example.project.domain.CaptionPosition.BOTTOM)
    val captionPosition: StateFlow<org.example.project.domain.CaptionPosition> = _captionPosition.asStateFlow()

    fun updateCaptionPosition(position: org.example.project.domain.CaptionPosition) {
        _captionPosition.value = position
    }

    fun archiveScript() {
        val currentScript = _activeScript.value
        if (currentScript != null) {
            viewModelScope.launch {
                val updatedScript = currentScript.copy(isActive = false)
                scriptDao.update(updatedScript)
                _activeScript.value = null
            }
        }
    }

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun onVideoImported(filePath: String) {
        val currentScript = _activeScript.value
        if (currentScript != null) {
            viewModelScope.launch {
                _isProcessing.value = true
                val duration = getVideoDuration(resolveVideoPath(filePath))
                val updatedScript = currentScript.copy(
                    videoPath = filePath,
                    skippedSegmentsJson = ""
                )
                scriptDao.update(updatedScript)
                _activeScript.value = updatedScript
                _videoPath.value = filePath
                _videoDuration.value = duration
                generateTimeline(duration, updatedScript.content)
                _isProcessing.value = false
            }
        }
    }

    fun saveModifiedVideo(onSaved: () -> Unit) {
        val currentScript = _activeScript.value
        val path = _videoPath.value
        if (currentScript != null && path != null) {
            viewModelScope.launch {
                _isProcessing.value = true
                try {
                    val unskippedSegments = getUnskippedSegments(_timelineBlocks.value)
                    
                    // Create a unique output path so we never overwrite the original raw recording
                    val epoch = Clock.System.now().toEpochMilliseconds()
                    val outPath = path.substringBeforeLast(".mp4") + "_trimmed_${epoch}.mp4"

                    val captions = if (_captionPosition.value != org.example.project.domain.CaptionPosition.NONE) {
                        parseScriptForBuffers(currentScript.content)
                            .filter { !it.isBuffer }
                            .map { CaptionInfo((it.startTimeSec * 1000).toLong(), (it.endTimeSec * 1000).toLong(), it.text) }
                    } else {
                        emptyList()
                    }

                    val success = trimVideo(
                        inputPath = path, 
                        unskippedSegments = unskippedSegments, 
                        outputPath = outPath, 
                        captions = captions, 
                        captionPosition = _captionPosition.value
                    )
                    
                    val updatedScript = currentScript.copy(
                        scriptState = "PUBLISHING_STUDIO",
                        publishedVideoPath = if (success) outPath else path, // Store the output in publishedVideoPath
                        // DO NOT wipe the skippedSegmentsJson here, otherwise we can't revert after publishing if we go back!
                        skippedSegmentsJson = Json.encodeToString(_timelineBlocks.value)
                    )
                    scriptDao.update(updatedScript)
                    _activeScript.value = updatedScript
                    _isProcessing.value = false
                    onSaved()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isProcessing.value = false
                }
            }
        }
    }
    
    private fun getUnskippedSegments(blocks: List<TimelineBlock>): List<Pair<Long, Long>> {
        val unskipped = mutableListOf<Pair<Long, Long>>()
        var currentStart: Long? = null
        
        for (block in blocks) {
            for (subBlock in block.subBlocks) {
                if (!subBlock.isSkipped) {
                    if (currentStart == null) {
                        currentStart = subBlock.startTimeMs
                    }
                } else {
                    if (currentStart != null) {
                        unskipped.add(currentStart to subBlock.startTimeMs)
                        currentStart = null
                    }
                }
            }
        }
        
        if (currentStart != null) {
            unskipped.add(currentStart to blocks.last().endTimeMs)
        }
        
        return unskipped
    }

    val hasModifications: StateFlow<Boolean> = _timelineBlocks.map { blocks ->
        blocks.any { block -> block.subBlocks.any { it.isSkipped } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun restoreOriginalVideo() {
        val currentBlocks = _timelineBlocks.value.map { block ->
            block.copy(subBlocks = block.subBlocks.map { it.copy(isSkipped = false) })
        }
        _timelineBlocks.value = currentBlocks
        _isPreviewingWithoutSkipped.value = false
        seekTo(0)
        _isPlaying.value = false
    }
}