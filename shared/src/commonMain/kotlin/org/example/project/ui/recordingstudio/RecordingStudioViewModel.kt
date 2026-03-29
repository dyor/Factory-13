package org.example.project.ui.recordingstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao

class RecordingStudioViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {

    private val _activeScript = MutableStateFlow<Script?>(null)
    val activeScript: StateFlow<Script?> = _activeScript.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _countdown = MutableStateFlow(5)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _isCountingDown = MutableStateFlow(false)
    val isCountingDown: StateFlow<Boolean> = _isCountingDown.asStateFlow()

    private val _visibleLines = MutableStateFlow<List<String>>(emptyList())
    val visibleLines: StateFlow<List<String>> = _visibleLines.asStateFlow()

    private var allLines: List<String> = emptyList()
    private var currentLineIndex = 0
    private var teleprompterJob: Job? = null

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                _activeScript.value = script
                if (script != null) {
                    allLines = script.content.split("\n").filter { it.isNotBlank() }
                    updateVisibleLines()
                }
            }
        }
    }

    fun startRecordingProcess() {
        if (_activeScript.value == null) return
        
        viewModelScope.launch {
            _isFinished.value = false
            _isCountingDown.value = true
            _countdown.value = 5
            
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value -= 1
            }
            
            _isCountingDown.value = false
            _isRecording.value = true
            startTeleprompter()
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        _isFinished.value = true
        teleprompterJob?.cancel()
        currentLineIndex = 0
        updateVisibleLines()
    }

    private fun startTeleprompter() {
        teleprompterJob?.cancel()
        
        val targetDuration = _activeScript.value?.targetDuration ?: 15
        val totalLines = allLines.size
        
        if (totalLines == 0) return
        
        val timePerLineMs = (targetDuration * 1000L) / totalLines

        teleprompterJob = viewModelScope.launch {
            while (currentLineIndex < totalLines) {
                updateVisibleLines()
                delay(timePerLineMs)
                currentLineIndex++
            }
            stopRecording()
        }
    }

    private fun updateVisibleLines() {
        val endIndex = minOf(currentLineIndex + 3, allLines.size)
        _visibleLines.value = allLines.subList(currentLineIndex, endIndex)
    }

    fun reset() {
        _isRecording.value = false
        _isFinished.value = false
        _isCountingDown.value = false
        _countdown.value = 5
        currentLineIndex = 0
        teleprompterJob?.cancel()
        updateVisibleLines()
    }
}