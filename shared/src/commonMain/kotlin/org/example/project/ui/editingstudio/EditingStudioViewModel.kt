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

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                _activeScript.value = script
                _videoPath.value = script?.videoPath
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
        // Simple mock behavior for Phase 3
        // In Phase 5 this will be precisely trimmed
    }

    fun saveModifiedVideo(onSaved: () -> Unit) {
        val currentScript = _activeScript.value
        if (currentScript != null) {
            viewModelScope.launch {
                val updatedScript = currentScript.copy(scriptState = "PUBLISHING_STUDIO")
                scriptDao.update(updatedScript)
                _activeScript.value = updatedScript
                onSaved()
            }
        }
    }

    fun restoreOriginalVideo() {
        // Phase 3 mock behavior
        seekTo(0)
        _isPlaying.value = false
    }
}