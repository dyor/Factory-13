package org.example.project.ui.writersroom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.Screen

@Composable
fun WritersRoomScreen(
    viewModel: WritersRoomViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecording: () -> Unit
) {
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedScript by viewModel.generatedScript.collectAsState()
    val targetDuration by viewModel.targetDuration.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Writer's Room",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Target Duration: ${targetDuration}s")
                Slider(
                    value = targetDuration.toFloat(),
                    onValueChange = { viewModel.updateTargetDuration(it.toInt()) },
                    valueRange = 5f..60f,
                    steps = 54
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.generateScript() },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Generate Script")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = generatedScript,
            onValueChange = { viewModel.updateScriptContent(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("Script Content") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onNavigateBack) {
                Text("Back to Home")
            }
            
            Button(
                onClick = { 
                    viewModel.saveScript {
                        onNavigateToRecording()
                    }
                },
                enabled = generatedScript.isNotBlank() && !isGenerating
            ) {
                Text("Save & Record")
            }
        }
    }
}