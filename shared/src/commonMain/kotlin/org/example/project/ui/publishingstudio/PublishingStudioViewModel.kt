package org.example.project.ui.publishingstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao
import org.example.project.domain.getVideoDuration
import org.example.project.domain.shareVideo

class PublishingStudioViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {

    private val _activeScript = MutableStateFlow<Script?>(null)
    val activeScript: StateFlow<Script?> = _activeScript.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _seekRequest = MutableStateFlow<Long?>(null)
    val seekRequest: StateFlow<Long?> = _seekRequest.asStateFlow()

    private val _currentTimeMs = MutableStateFlow<Long>(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _videoDuration = MutableStateFlow<Long>(0L)
    val videoDuration: StateFlow<Long> = _videoDuration.asStateFlow()
    
    private val _isPlaybackCompleted = MutableStateFlow(false)
    val isPlaybackCompleted: StateFlow<Boolean> = _isPlaybackCompleted.asStateFlow()

    fun updateCurrentTime(timeMs: Long) {
        _currentTimeMs.value = timeMs
    }

    fun togglePlayPause() {
        if (_isPlaybackCompleted.value) {
            _isPlaybackCompleted.value = false
            seekTo(0L)
            _isPlaying.value = true
        } else {
            _isPlaying.value = !_isPlaying.value
        }
    }
    
    fun onVideoCompletion() {
        _isPlaying.value = false
        _isPlaybackCompleted.value = true
    }

    fun seekTo(positionMs: Long) {
        _seekRequest.value = positionMs
        _seekRequest.value = null // reset immediately so we can trigger same seek again
        _isPlaybackCompleted.value = false
    }

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                _activeScript.value = script
                script?.videoPath?.let { path ->
                    try {
                        _videoDuration.value = getVideoDuration(path)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun shareCurrentVideo() {
        val path = _activeScript.value?.videoPath
        if (path != null) {
            shareVideo(path)
        }
    }

    fun markAsPublished(onCompleted: () -> Unit) {
        val currentScript = _activeScript.value
        if (currentScript != null) {
            viewModelScope.launch {
                val archivedScript = currentScript.copy(
                    isActive = false, 
                    scriptState = "ARCHIVES"
                )
                scriptDao.update(archivedScript)
                _activeScript.value = null
                onCompleted()
            }
        }
    }
}