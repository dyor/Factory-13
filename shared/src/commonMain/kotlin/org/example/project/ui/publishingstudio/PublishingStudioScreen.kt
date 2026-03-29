package org.example.project.ui.publishingstudio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PublishingStudioScreen(
    viewModel: PublishingStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
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
            text = "Publishing Studio",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (activeScript?.videoPath == null) {
            Text("No video to publish.", color = MaterialTheme.colorScheme.error)
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ready to publish:", style = MaterialTheme.typography.titleMedium)
                    Text("\"${activeScript?.title}\"", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { viewModel.shareCurrentVideo() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Export / Share")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.markAsPublished { onNavigateHome() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark as Completed & Go Home")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Editing")
        }
    }
}