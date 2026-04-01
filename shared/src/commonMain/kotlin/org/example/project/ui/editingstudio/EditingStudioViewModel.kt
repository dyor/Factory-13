package org.example.project.ui.editingstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao
import org.example.project.domain.getVideoDuration
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
    val subBlocks: List<TimelineSubBlock>
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
                _activeScript.value = script
                _videoPath.value = script?.videoPath
                script?.videoPath?.let { path ->
                    try {
                        val duration = getVideoDuration(path)
                        _videoDuration.value = duration
                        
                        if (!script.skippedSegmentsJson.isNullOrBlank()) {
                            try {
                                _timelineBlocks.value = Json.decodeFromString(script.skippedSegmentsJson)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                generateTimeline(duration)
                            }
                        } else {
                            generateTimeline(duration)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun generateTimeline(durationMs: Long) {
        val blocks = mutableListOf<TimelineBlock>()
        val totalSeconds = kotlin.math.ceil(durationMs / 1000.0).toInt()
        
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
            blocks.add(TimelineBlock(sec, secStart, secEnd, subBlocks))
        }
        _timelineBlocks.value = blocks
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

    fun saveModifiedVideo(onSaved: () -> Unit) {
        val currentScript = _activeScript.value
        val path = _videoPath.value
        if (currentScript != null && path != null) {
            viewModelScope.launch {
                try {
                    val unskippedSegments = getUnskippedSegments(_timelineBlocks.value)
                    
                    // Create a unique output path so we never overwrite the original raw recording
                    val epoch = Clock.System.now().toEpochMilliseconds()
                    val outPath = path.substringBeforeLast(".mp4") + "_trimmed_${epoch}.mp4"

                    val success = trimVideo(path, unskippedSegments, outPath)
                    
                    val updatedScript = currentScript.copy(
                        scriptState = "PUBLISHING_STUDIO",
                        videoPath = if (success) outPath else path,
                        skippedSegmentsJson = Json.encodeToString(_timelineBlocks.value)
                    )
                    scriptDao.update(updatedScript)
                    _activeScript.value = updatedScript
                    onSaved()
                } catch (e: Exception) {
                    e.printStackTrace()
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