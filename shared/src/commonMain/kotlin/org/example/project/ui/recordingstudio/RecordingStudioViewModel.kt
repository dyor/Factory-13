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

data class ScriptSegment(
    val startTimeSec: Int,
    val endTimeSec: Int,
    val text: String,
    val isBuffer: Boolean = false
)

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

    private val _currentSegmentText = MutableStateFlow("")
    val currentSegmentText: StateFlow<String> = _currentSegmentText.asStateFlow()

    private val _isCurrentSegmentBuffer = MutableStateFlow(false)
    val isCurrentSegmentBuffer: StateFlow<Boolean> = _isCurrentSegmentBuffer.asStateFlow()

    private val _totalTimeRemainingSec = MutableStateFlow(0)
    val totalTimeRemainingSec: StateFlow<Int> = _totalTimeRemainingSec.asStateFlow()

    private val _segmentTimeRemainingSec = MutableStateFlow(0)
    val segmentTimeRemainingSec: StateFlow<Int> = _segmentTimeRemainingSec.asStateFlow()

    private val _segmentProgress = MutableStateFlow(0f)
    val segmentProgress: StateFlow<Float> = _segmentProgress.asStateFlow()

    private val _totalProgress = MutableStateFlow(0f)
    val totalProgress: StateFlow<Float> = _totalProgress.asStateFlow()

    private val _segments = MutableStateFlow<List<ScriptSegment>>(emptyList())
    val segments: StateFlow<List<ScriptSegment>> = _segments.asStateFlow()

    private var teleprompterJob: Job? = null

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                _activeScript.value = script
                if (script != null) {
                    val parsed = parseScript(script.content)
                    _segments.value = parsed
                    if (parsed.isNotEmpty()) {
                        _currentSegmentText.value = parsed.first().text
                        _totalTimeRemainingSec.value = parsed.last().endTimeSec
                        val firstSeg = parsed.first()
                        _segmentTimeRemainingSec.value = firstSeg.endTimeSec - firstSeg.startTimeSec
                        _segmentProgress.value = 0f
                        _totalProgress.value = 0f
                    }
                }
            }
        }
    }

    private fun parseScript(content: String): List<ScriptSegment> {
        val regex = Regex("""^(\d+)s-(\d+)s:?\s*(.*)$""")
        val lines = content.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val parsedSegments = mutableListOf<ScriptSegment>()
        
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
            parsedSegments.add(ScriptSegment(segStart, segEnd, text, isBuffer = false))
            runningTime = segEnd
            
            // Add a 2-second buffer between segments using the same text but marked as buffer
            if (index < lines.lastIndex) {
                parsedSegments.add(ScriptSegment(runningTime, runningTime + 2, text, isBuffer = true))
                runningTime += 2
            }
        }
        
        // Add a 5-second buffer segment at the end so the speaker doesn't get cut off
        if (parsedSegments.isNotEmpty()) {
            val lastTime = parsedSegments.last().endTimeSec
            parsedSegments.add(ScriptSegment(lastTime, lastTime + 5, "...and cut!", isBuffer = true))
        }
        
        return parsedSegments
    }

    private var actualElapsedMs = 0L // Keep track of actual recorded time

    fun startRecordingProcess() {
        if (_activeScript.value == null) return
        
        viewModelScope.launch {
            _isFinished.value = false
            _isCountingDown.value = true
            _countdown.value = 5
            actualElapsedMs = 0L
            
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
        if (_isFinished.value) return // Prevent multiple calls
        
        _isRecording.value = false
        _isFinished.value = true
        
        val job = teleprompterJob
        teleprompterJob = null
        job?.cancel()
        
        val actualSecs = (actualElapsedMs + 999) / 1000
        val currentScript = _activeScript.value
        if (currentScript != null && actualSecs > 0) {
             viewModelScope.launch {
                 val updated = currentScript.copy(targetDuration = actualSecs.toInt())
                 scriptDao.update(updated)
                 _activeScript.value = updated
             }
        }
    }

    private fun startTeleprompter() {
        teleprompterJob?.cancel()
        
        val currentSegments = segments.value
        if (currentSegments.isEmpty()) {
            stopRecording()
            return
        }

        val totalDurationMs = currentSegments.last().endTimeSec * 1000L // Buffer is already included in parsed segments

        teleprompterJob = viewModelScope.launch {
            actualElapsedMs = 0L
            val tickMs = 100L

            while (actualElapsedMs <= totalDurationMs) {
                val currentSec = (actualElapsedMs / 1000).toInt()
                
                // Find current segment
                val currentSeg = currentSegments.firstOrNull { currentSec >= it.startTimeSec && currentSec < it.endTimeSec } 
                    ?: currentSegments.lastOrNull { currentSec >= it.endTimeSec }
                    ?: currentSegments.last() // Fallback to last segment if outside defined ranges

                _currentSegmentText.value = currentSeg.text
                _isCurrentSegmentBuffer.value = currentSeg.isBuffer
                
                val totalRemaining = maxOf(0L, (totalDurationMs - actualElapsedMs) / 1000).toInt()
                _totalTimeRemainingSec.value = totalRemaining

                _totalProgress.value = if (totalDurationMs > 0) {
                    (actualElapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    1f
                }

                val segDurationMs = (currentSeg.endTimeSec - currentSeg.startTimeSec) * 1000L
                val segElapsedMs = actualElapsedMs - (currentSeg.startTimeSec * 1000L)
                val segRemainingMs = maxOf(0L, segDurationMs - segElapsedMs)
                
                _segmentTimeRemainingSec.value = (segRemainingMs / 1000).toInt()
                
                _segmentProgress.value = if (segDurationMs > 0) {
                    (segElapsedMs.toFloat() / segDurationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    1f
                }

                delay(tickMs)
                actualElapsedMs += tickMs
            }
            
            // The teleprompter is done. Give the camera plugin and audio buffer a substantial amount of time to flush
            // before we kill the recording state, which stops the plugin. 
            // 500ms may not be enough for some devices under load. We'll use 1.5s to be safe.
            delay(1500)
            stopRecording()
        }
    }

    fun onVideoRecorded(filePath: String) {
        val currentScript = _activeScript.value
        if (currentScript != null) {
            viewModelScope.launch {
                val updatedScript = currentScript.copy(
                    videoPath = filePath,
                    scriptState = "RECORDING_STUDIO",
                    skippedSegmentsJson = ""
                )
                scriptDao.update(updatedScript)
                _activeScript.value = updatedScript
            }
        }
    }

    fun archiveScript() {
        val currentScript = _activeScript.value
        if (currentScript != null) {
            viewModelScope.launch {
                val updatedScript = currentScript.copy(scriptState = "ARCHIVED")
                scriptDao.update(updatedScript)
                _activeScript.value = null
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            val currentScript = _activeScript.value
            if (currentScript != null) {
                val updatedScript = currentScript.copy(videoPath = null, scriptState = "WRITERS_ROOM")
                scriptDao.update(updatedScript)
                _activeScript.value = updatedScript // Update the flow to reflect the change immediately
            }
        }

        _isRecording.value = false
        _isFinished.value = false
        _isCountingDown.value = false
        _countdown.value = 5
        teleprompterJob?.cancel()
        val currentSegments = segments.value
        if (currentSegments.isNotEmpty()) {
            _currentSegmentText.value = currentSegments.first().text
            _totalTimeRemainingSec.value = currentSegments.last().endTimeSec
            val firstSeg = currentSegments.first()
            _segmentTimeRemainingSec.value = firstSeg.endTimeSec - firstSeg.startTimeSec
            _segmentProgress.value = 0f
            _totalProgress.value = 0f
        }
    }
}
