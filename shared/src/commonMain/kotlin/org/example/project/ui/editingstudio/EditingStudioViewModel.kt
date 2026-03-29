package org.example.project.ui.editingstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao

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
    
    private val _skippedSegments = MutableStateFlow<List<Pair<Long, Long>>>(emptyList())
    val skippedSegments: StateFlow<List<Pair<Long, Long>>> = _skippedSegments.asStateFlow()

    private val _currentTime = MutableStateFlow<Long>(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    fun updateCurrentTime(timeMs: Long) {
        _currentTime.value = timeMs
    }

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                _activeScript.value = script
                _videoPath.value = script?.videoPath
                script?.videoPath?.let { path ->
                    try {
                        val duration = org.example.project.domain.getVideoDuration(path)
                        _videoDuration.value = duration
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekTo(positionMs: Long) {
        _seekRequest.value = positionMs
        _seekRequest.value = null // reset immediately so we can trigger same seek again
    }

    fun markSectionForRemoval() {
        val current = _currentTime.value
        val start = maxOf(0L, current - 500)
        val end = minOf(_videoDuration.value, current + 500)
        val newSegment = start to end
        
        val currentSegments = _skippedSegments.value.toMutableList()
        currentSegments.add(newSegment)
        
        // Very basic mock simplification of merging intervals
        _skippedSegments.value = currentSegments.sortedBy { it.first }
    }

    fun saveModifiedVideo(onSaved: () -> Unit) {
        val currentScript = _activeScript.value
        val path = _videoPath.value
        if (currentScript != null && path != null) {
            viewModelScope.launch {
                // In Phase 5, we actually trigger the trimVideo API here
                // For now, assume it always works.
                try {
                    val unskippedSegments = calculateUnskippedSegments()
                    val outPath = path.replace(".mp4", "_trimmed.mp4")
                    val success = org.example.project.domain.trimVideo(path, unskippedSegments, outPath)
                    
                    val updatedScript = currentScript.copy(
                        scriptState = "PUBLISHING_STUDIO",
                        videoPath = if (success) outPath else path
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
    
    private fun calculateUnskippedSegments(): List<Pair<Long, Long>> {
        val totalDuration = _videoDuration.value
        if (totalDuration == 0L || _skippedSegments.value.isEmpty()) {
            return listOf(0L to totalDuration)
        }
        
        val unskipped = mutableListOf<Pair<Long, Long>>()
        var currentStart = 0L
        for (skip in _skippedSegments.value) {
            if (currentStart < skip.first) {
                unskipped.add(currentStart to skip.first)
            }
            currentStart = skip.second
        }
        if (currentStart < totalDuration) {
            unskipped.add(currentStart to totalDuration)
        }
        return unskipped
    }

    fun restoreOriginalVideo() {
        _skippedSegments.value = emptyList()
        seekTo(0)
        _isPlaying.value = false
    }
}