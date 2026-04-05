package org.example.project.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun StudioBottomNavigationRow(
    onBack: () -> Unit,
    onArchive: (() -> Unit)? = null,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing slightly
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.width(64.dp).height(48.dp).semantics { contentDescription = "Go Back Button" },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(bottom = 2.dp)) {
                    Text("←", fontSize = 24.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            if (onArchive != null) {
                OutlinedButton(
                    onClick = onArchive,
                    modifier = Modifier.width(64.dp).height(48.dp).semantics { contentDescription = "Archive Button" },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(bottom = 2.dp)) {
                        Text("↓", fontSize = 24.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            OutlinedButton(
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier.weight(1f).height(48.dp).semantics { contentDescription = actionText },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, if (actionEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = actionText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

