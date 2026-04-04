package org.example.project.ui.writersroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.Screen

@Composable
fun WritersRoomScreen(
    viewModel: WritersRoomViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecording: () -> Unit
) {
    val isGenerating by viewModel.isGenerating.collectAsState()
    val promptContent by viewModel.promptContent.collectAsState()
    val generatedScript by viewModel.generatedScript.collectAsState()
    val targetDuration by viewModel.targetDuration.collectAsState()
    val activeScript by viewModel.activeScript.collectAsState()
    
    val focusManager = LocalFocusManager.current
    var promptHasFocus by remember { mutableStateOf(false) }
    var scriptHasFocus by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    var showArchiveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!promptHasFocus && !scriptHasFocus) {
            Text(
                text = "Writer's Room",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary, // Themed Gold
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(12.dp)) // Themed background
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!scriptHasFocus) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(16.dp)) // Themed background
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = promptContent,
                        onValueChange = { viewModel.updatePromptContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp)) // Themed background
                            .onFocusChanged { promptHasFocus = it.isFocused },
                        label = { Text("Prompt Idea", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Themed Light Gray
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), // Themed White
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Themed Gold
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant, // Themed Dark Gray
                            cursorColor = MaterialTheme.colorScheme.primary, // Themed Gold
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Target Duration: ${targetDuration}s",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface // Themed White
                    )
                    Slider(
                        value = targetDuration.toFloat(),
                        onValueChange = { viewModel.updateTargetDuration(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 10, // Gives 5-second increments: 5, 10, 15, ..., 60
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary, // Themed Gold
                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), // Themed Gold
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant // Themed Dark Gray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { 
                            focusManager.clearFocus()
                            viewModel.generateScript() 
                        },
                        enabled = !isGenerating && promptContent.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), // Themed Gold
                            contentColor = MaterialTheme.colorScheme.primary, // Themed Gold
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant, // Themed Gray
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Themed Dark Gray
                        ),
                        border = BorderStroke(1.dp, if (!isGenerating && promptContent.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) // Themed border
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary // Themed Gold
                            )
                        } else {
                            Text("Generate Script", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!promptHasFocus) {
            OutlinedTextField(
                value = generatedScript,
                onValueChange = { viewModel.updateScriptContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp)) // Themed background
                    .onFocusChanged { scriptHasFocus = it.isFocused },
                label = { Text("Generated Script Content", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Themed Light Gray
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), // Themed White
                colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Themed Gold
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant, // Themed Dark Gray
                            cursorColor = MaterialTheme.colorScheme.primary, // Themed Gold
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!promptHasFocus) {
            org.example.project.ui.components.StudioBottomNavigationRow(
                onBack = onNavigateBack,
                onArchive = if (activeScript != null) { { showArchiveDialog = true } } else null,
                actionText = "Record →",
                actionEnabled = generatedScript.isNotBlank() && !isGenerating,
                onAction = {
                    viewModel.saveScript {
                        onNavigateToRecording()
                    }
                }
            )
        }

        if (showArchiveDialog) {
            AlertDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = { Text("Archive Script?") },
                text = { Text("Are you sure you want to archive this script?", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Themed Light Gray
                confirmButton = {
                    TextButton(onClick = {
                        showArchiveDialog = false
                        viewModel.archiveScript()
                        onNavigateBack()
                    }) { Text("Yes", color = MaterialTheme.colorScheme.primary) } // Themed Gold
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } // Themed Gray
                },
                containerColor = MaterialTheme.colorScheme.surface, // Themed background
                titleContentColor = MaterialTheme.colorScheme.primary, // Themed Gold
                textContentColor = MaterialTheme.colorScheme.onSurface // Themed White
            )
        }
    }
}
