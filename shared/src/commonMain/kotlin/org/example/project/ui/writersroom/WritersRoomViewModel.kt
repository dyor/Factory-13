package org.example.project.ui.writersroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao
import org.example.project.domain.gemini.GeminiClient

class WritersRoomViewModel(
    private val scriptDao: ScriptDao,
    private val geminiClient: GeminiClient = GeminiClient()
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedScript = MutableStateFlow("")
    val generatedScript: StateFlow<String> = _generatedScript.asStateFlow()

    private val _targetDuration = MutableStateFlow(15)
    val targetDuration: StateFlow<Int> = _targetDuration.asStateFlow()

    fun generateScript() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val script = geminiClient.generateScript(targetDurationSeconds = _targetDuration.value)
                _generatedScript.value = script
            } catch (e: Exception) {
                _generatedScript.value = "Error generating script: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun updateTargetDuration(duration: Int) {
        _targetDuration.value = duration
    }

    fun updateScriptContent(newContent: String) {
        _generatedScript.value = newContent
    }

    fun saveScript(onSaved: () -> Unit) {
        viewModelScope.launch {
            scriptDao.clearActiveScript()
            val newScript = Script(
                title = "Generated Script",
                content = _generatedScript.value,
                targetDuration = _targetDuration.value,
                isActive = true
            )
            scriptDao.insert(newScript)
            onSaved()
        }
    }
}