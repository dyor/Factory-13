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
                val prompt = "Write a script for YouTube short that is designed to teach people how to create compelling YouTube shorts. It should take exactly ${_targetDuration.value} seconds to read aloud at a normal pace. ONLY return the text to be spoken and the timestamp range it is spoken in, using the format '0s-5s: Hello...'. Do not include any conversational filler, markdown formatting, explanations, or background info."
                
                val request = org.example.project.domain.gemini.GeminiRequest(
                    contents = listOf(
                        org.example.project.domain.gemini.Content(
                            parts = listOf(org.example.project.domain.gemini.Part(text = prompt))
                        )
                    )
                )

                // We're dynamically constructing the request here since GeminiClient originally hardcoded it
                // Actually, wait, GeminiClient has the hardcoded prompt inside its generateScript method.
                // Let's pass the prompt to GeminiClient.
                val script = geminiClient.generateScript(prompt = prompt)
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