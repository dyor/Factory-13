package org.example.project.ui.publishingstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao
import org.example.project.domain.shareVideo

class PublishingStudioViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {

    private val _activeScript = MutableStateFlow<Script?>(null)
    val activeScript: StateFlow<Script?> = _activeScript.asStateFlow()

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                _activeScript.value = script
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