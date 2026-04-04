package org.example.project.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.tooling.preview.Preview
import org.example.project.domain.Script
import org.example.project.ui.theme.MovieTheme

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

    HomeScreenContent(
        activeScript = activeScript,
        onArchiveActiveScript = { viewModel.archiveActiveScript() },
        onNavigateToWritersRoom = onNavigateToWritersRoom,
        onNavigateToRecordingStudio = onNavigateToRecordingStudio,
        onNavigateToEditingStudio = onNavigateToEditingStudio,
        onNavigateToPublishingStudio = onNavigateToPublishingStudio,
        onNavigateToArchives = onNavigateToArchives
    )
}

@Composable
fun HomeScreenContent(
    activeScript: Script?,
    onArchiveActiveScript: () -> Unit,
    onNavigateToWritersRoom: () -> Unit,
    onNavigateToRecordingStudio: () -> Unit,
    onNavigateToEditingStudio: () -> Unit,
    onNavigateToPublishingStudio: () -> Unit,
    onNavigateToArchives: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "The Factory",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 24.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (activeScript == null) {
            Column(
                modifier = Modifier.weight(1f), 
                verticalArrangement = Arrangement.Center, 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = onNavigateToWritersRoom,
                    modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Writer's Room", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onNavigateToArchives,
                    modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Archives", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = activeScript.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val state = activeScript.scriptState
            val currentLevel = when (state) {
                "WRITERS_ROOM" -> 1
                "RECORDING_STUDIO" -> 2
                "EDITING_STUDIO" -> 3
                "PUBLISHING_STUDIO" -> 4
                else -> 1
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    RoomButton(
                        text = "The Archives", 
                        isActive = false, 
                        isFuture = false, 
                        onClick = onNavigateToArchives, 
                        modifier = Modifier.fillMaxWidth(0.5f).height(64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                val arrowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                val arrowStyle = MaterialTheme.typography.displayMedium.copy(color = arrowColor, fontWeight = FontWeight.Bold)

                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    // Top Row
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RoomButton(
                                text = "Recording\nStudio", 
                                isActive = currentLevel == 2, 
                                isFuture = currentLevel < 2, 
                                onClick = onNavigateToRecordingStudio, 
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                        }
                        Box(modifier = Modifier.weight(0.4f), contentAlignment = Alignment.Center) {
                            if (currentLevel >= 2) {
                                Text("→", style = arrowStyle)
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RoomButton(
                                text = "Editing\nStudio", 
                                isActive = currentLevel == 3, 
                                isFuture = currentLevel < 3, 
                                onClick = onNavigateToEditingStudio, 
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                        }
                    }

                    // Middle Row
                    Row(modifier = Modifier.fillMaxWidth().weight(0.4f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (currentLevel >= 1) {
                                Text("↑", style = arrowStyle)
                            }
                        }
                        Box(modifier = Modifier.weight(0.4f)) // Empty middle
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (currentLevel >= 3) {
                                Text("↓", style = arrowStyle)
                            }
                        }
                    }

                    // Bottom Row
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RoomButton(
                                text = "Writer's\nRoom", 
                                isActive = currentLevel == 1, 
                                isFuture = currentLevel < 1, 
                                onClick = onNavigateToWritersRoom, 
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                        }
                        Box(modifier = Modifier.weight(0.4f)) // Empty middle
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RoomButton(
                                text = "Production\nStudio",
                                isActive = currentLevel == 4, 
                                isFuture = currentLevel < 4, 
                                onClick = onNavigateToPublishingStudio, 
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                        }
                    }
                }
            }
            TextButton(
                onClick = onArchiveActiveScript,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text("↓ Archive Project", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RoomButton(
    text: String,
    isActive: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f)
    val contentColor = if (isFuture) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
    val borderColor = if (isFuture) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary

    OutlinedButton(
        onClick = onClick,
        enabled = !isFuture,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.Black.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier.height(150.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MovieTheme {
        HomeScreenContent(
            activeScript = Script(
                id = 1L,
                title = "The Great Adventure",
                prompt = "A story about a great adventure",
                content = "Once upon a time...",
                targetDuration = 60,
                isActive = true,
                scriptState = "WRITERS_ROOM"
            ),
            onArchiveActiveScript = {},
            onNavigateToWritersRoom = {},
            onNavigateToRecordingStudio = {},
            onNavigateToEditingStudio = {},
            onNavigateToPublishingStudio = {},
            onNavigateToArchives = {}
        )
    }
}
