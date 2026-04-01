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
                color = Color(0xFFFFD700), // Gold for Noir theme
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!scriptHasFocus) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = promptContent,
                        onValueChange = { viewModel.updatePromptContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .onFocusChanged { promptHasFocus = it.isFocused },
                        label = { Text("Prompt Idea", color = Color.LightGray) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFFFFD700),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Target Duration: ${targetDuration}s",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Slider(
                        value = targetDuration.toFloat(),
                        onValueChange = { viewModel.updateTargetDuration(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 54,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFFFFD700).copy(alpha = 0.7f),
                            inactiveTrackColor = Color.DarkGray
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
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                            contentColor = Color(0xFFFFD700),
                            disabledContentColor = Color.Gray,
                            disabledContainerColor = Color.DarkGray.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, if (!isGenerating && promptContent.isNotBlank()) Color(0xFFFFD700) else Color.Gray)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFFFD700)
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
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .onFocusChanged { scriptHasFocus = it.isFocused },
                label = { Text("Generated Script Content", color = Color.LightGray) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color(0xFFFFD700),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!promptHasFocus) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text("←", fontSize = 20.sp)
                }

                if (activeScript != null) {
                    OutlinedButton(
                        onClick = { showArchiveDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text("↓", fontSize = 20.sp)
                    }
                }
                
                OutlinedButton(
                    onClick = { 
                        viewModel.saveScript {
                            onNavigateToRecording()
                        }
                    },
                    enabled = generatedScript.isNotBlank() && !isGenerating,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                        contentColor = Color(0xFFFFD700),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, if(generatedScript.isNotBlank() && !isGenerating) Color(0xFFFFD700) else Color.Gray)
                ) {
                    Text("Record →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showArchiveDialog) {
            AlertDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = { Text("Archive Script?") },
                text = { Text("Are you sure you want to archive this script?") },
                confirmButton = {
                    TextButton(onClick = {
                        showArchiveDialog = false
                        viewModel.archiveScript()
                        onNavigateBack()
                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel") }
                },
                containerColor = Color(0xFF222222),
                titleContentColor = Color(0xFFFFD700),
                textContentColor = Color.White
            )
        }
    }
}