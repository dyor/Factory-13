package org.example.project.ui.archives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.example.project.domain.Script
import org.example.project.domain.ScriptDao

class ArchivesViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {

    private val _archivedScripts = MutableStateFlow<List<Script>>(emptyList())
    val archivedScripts: StateFlow<List<Script>> = _archivedScripts.asStateFlow()

    init {
        viewModelScope.launch {
            scriptDao.getAllScripts().collectLatest { scripts ->
                _archivedScripts.value = scripts.filter { !it.isActive }
            }
        }
    }

    fun makeScriptActive(script: Script, onActivated: (String) -> Unit) {
        viewModelScope.launch {
            scriptDao.setActiveScript(script.id)
            onActivated(script.scriptState)
        }
    }
}