package org.example.project.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToWritersRoom: () -> Unit,
    onNavigateToRecordingStudio: () -> Unit,
    onNavigateToEditingStudio: () -> Unit,
    onNavigateToPublishingStudio: () -> Unit,
    onNavigateToArchives: () -> Unit
) {
    val activeScript by viewModel.activeScript.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "The Factory",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (activeScript == null) {
            Button(
                onClick = onNavigateToWritersRoom,
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
            ) {
                Text("Start New Video")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Active Project:", style = MaterialTheme.typography.titleMedium)
                    Text("\"${activeScript?.title}\"", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    val state = activeScript?.scriptState ?: "WRITERS_ROOM"
                    
                    when (state) {
                        "WRITERS_ROOM" -> {
                            Button(onClick = onNavigateToWritersRoom, modifier = Modifier.fillMaxWidth()) {
                                Text("Continue in Writer's Room")
                            }
                        }
                        "RECORDING_STUDIO" -> {
                            Button(onClick = onNavigateToRecordingStudio, modifier = Modifier.fillMaxWidth()) {
                                Text("Continue in Recording Studio")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.revertActiveScriptState("WRITERS_ROOM") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Revert to Writer's Room")
                            }
                        }
                        "EDITING_STUDIO" -> {
                            Button(onClick = onNavigateToEditingStudio, modifier = Modifier.fillMaxWidth()) {
                                Text("Continue in Editing Studio")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.revertActiveScriptState("RECORDING_STUDIO") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Re-Record Video")
                            }
                        }
                        "PUBLISHING_STUDIO" -> {
                            Button(onClick = onNavigateToPublishingStudio, modifier = Modifier.fillMaxWidth()) {
                                Text("Continue to Publishing")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.revertActiveScriptState("EDITING_STUDIO") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Revert to Editing")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { viewModel.archiveActiveScript() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Archive Project")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToArchives,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text("Archives")
        }
    }
}