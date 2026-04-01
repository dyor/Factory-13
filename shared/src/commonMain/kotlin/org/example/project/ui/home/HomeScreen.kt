package org.example.project.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (activeScript == null) {
            OutlinedButton(
                onClick = onNavigateToWritersRoom,
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Writer's Room", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Active Project", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeScript?.title ?: "", // Corrected string interpolation
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val state = activeScript?.scriptState ?: "WRITERS_ROOM"
                    
                    val primaryButtonColors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    val secondaryButtonColors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    val buttonBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)

                    when (state) {
                        "WRITERS_ROOM" -> {
                            OutlinedButton(
                                onClick = onNavigateToWritersRoom, 
                                modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("Writer's Room →")
                            }
                        }
                        "RECORDING_STUDIO" -> {
                            OutlinedButton(
                                onClick = onNavigateToRecordingStudio, 
                                modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("Record →")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.revertActiveScriptState("WRITERS_ROOM") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = secondaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("←")
                            }
                        }
                        "EDITING_STUDIO" -> {
                            OutlinedButton(
                                onClick = onNavigateToEditingStudio, 
                                modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("Edit →")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.revertActiveScriptState("RECORDING_STUDIO") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = secondaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("←")
                            }
                        }
                        "PUBLISHING_STUDIO" -> {
                            OutlinedButton(
                                onClick = onNavigateToPublishingStudio, 
                                modifier = Modifier.fillMaxWidth(),
                                colors = primaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("Publish →")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.revertActiveScriptState("EDITING_STUDIO") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = secondaryButtonColors,
                                border = buttonBorder
                            ) {
                                Text("←")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = { viewModel.archiveActiveScript() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("↓ Archive Project")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToArchives,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text("Archives", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
