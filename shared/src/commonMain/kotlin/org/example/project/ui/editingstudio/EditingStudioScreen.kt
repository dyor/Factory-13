package org.example.project.ui.editingstudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.example.project.domain.resolveVideoPath
import org.example.project.domain.CaptionPosition
import org.example.project.domain.VideoPicker
import org.example.project.ui.components.PlayPauseReplayButton
import org.example.project.ui.components.StudioBottomNavigationRow
import org.example.project.ui.components.VideoPlayer

import androidx.compose.ui.tooling.preview.Preview
import org.example.project.ui.theme.MovieTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditingStudioScreen(
    viewModel: EditingStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val seekRequest by viewModel.seekRequest.collectAsState()
    val videoPath by viewModel.videoPath.collectAsState()
    val currentTime by viewModel.currentTimeMs.collectAsState()
    val videoDuration by viewModel.videoDuration.collectAsState()
    val timelineBlocks by viewModel.timelineBlocks.collectAsState()
    val isPlaybackCompleted by viewModel.isPlaybackCompleted.collectAsState()
    val captionPosition by viewModel.captionPosition.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val hasModifications by viewModel.hasModifications.collectAsState()

    EditingStudioScreenContent(
        isPlaying = isPlaying,
        seekRequest = seekRequest,
        videoPath = videoPath,
        currentTime = currentTime,
        videoDuration = videoDuration,
        timelineBlocks = timelineBlocks,
        isPlaybackCompleted = isPlaybackCompleted,
        captionPosition = captionPosition,
        isProcessing = isProcessing,
        hasModifications = hasModifications,
        onNavigateBack = onNavigateBack,
        onNavigateForward = onNavigateForward,
        onPauseVideo = { viewModel.pauseVideo() },
        onVideoImported = { viewModel.onVideoImported(it) },
        onUpdateCurrentTime = { viewModel.updateCurrentTime(it) },
        onVideoCompletion = { viewModel.onVideoCompletion() },
        onSeekTo = { viewModel.seekTo(it) },
        onReplay = { viewModel.replay() },
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onRestoreOriginalVideo = { viewModel.restoreOriginalVideo() },
        onUpdateCaptionPosition = { viewModel.updateCaptionPosition(it) },
        onSaveModifiedVideo = { viewModel.saveModifiedVideo(it) },
        onArchiveScript = { viewModel.archiveScript() },
        onSkipAllSubBlocks = { viewModel.skipAllSubBlocks(it) },
        onUnskipAllSubBlocks = { viewModel.unskipAllSubBlocks(it) },
        onToggleSubBlockSkip = { secondIndex, index -> viewModel.toggleSubBlockSkip(secondIndex, index) },
        getTimelineBlock = { viewModel.timelineBlocks.value.getOrNull(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditingStudioScreenContent(
    isPlaying: Boolean,
    seekRequest: Long?,
    videoPath: String?,
    currentTime: Long,
    videoDuration: Long,
    timelineBlocks: List<TimelineBlock>,
    isPlaybackCompleted: Boolean,
    captionPosition: CaptionPosition,
    isProcessing: Boolean,
    hasModifications: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onPauseVideo: () -> Unit,
    onVideoImported: (String) -> Unit,
    onUpdateCurrentTime: (Long) -> Unit,
    onVideoCompletion: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onReplay: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onRestoreOriginalVideo: () -> Unit,
    onUpdateCaptionPosition: (CaptionPosition) -> Unit,
    onSaveModifiedVideo: (() -> Unit) -> Unit,
    onArchiveScript: () -> Unit,
    onSkipAllSubBlocks: (Int) -> Unit,
    onUnskipAllSubBlocks: (Int) -> Unit,
    onToggleSubBlockSkip: (Int, Int) -> Unit,
    getTimelineBlock: (Int) -> TimelineBlock?
) {
    var showFineTuneModal by remember { mutableStateOf<TimelineBlock?>(null) }
    var selectedBlockIndex by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(showFineTuneModal) {
        if (showFineTuneModal != null) {
            onPauseVideo()
        }
    }

    LaunchedEffect(timelineBlocks) {
        if (showFineTuneModal != null) {
            // Re-find the block to ensure the modal displays updated sub-block states
            showFineTuneModal = timelineBlocks.firstOrNull { it.secondIndex == showFineTuneModal!!.secondIndex }
        }
    }

    VideoPicker(show = showVideoPicker) { path ->
        showVideoPicker = false
        if (path != null) {
            onVideoImported(path)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editing Studio",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary, // Gold for Noir theme
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (videoPath == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No video found.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { showVideoPicker = true }) {
                        Text("Import Video")
                    }
                }
            }
        } else {
            // Video Player
            Box(
                modifier = Modifier
                    .weight(0.4f) // Give less space to the video relative to the controls
                    .fillMaxWidth()
            ) {
                val resolvedPath = resolveVideoPath(videoPath)
                VideoPlayer(
                    modifier = Modifier.fillMaxSize(),
                    videoPath = resolvedPath,
                    isPlaying = isPlaying,
                    seekRequest = seekRequest,
                    onTimeUpdate = { onUpdateCurrentTime(it) },
                    onCompletion = {
                        onVideoCompletion()
                    }
                )

                // Temporal red overlay if the current time is inside a skipped segment
                val isCurrentlySkipped = timelineBlocks.any { block ->
                    block.subBlocks.any { subBlock ->
                        subBlock.isSkipped && currentTime >= subBlock.startTimeMs && currentTime < subBlock.endTimeMs
                    }
                }
                
                if (isCurrentlySkipped) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls & Mock Trimming Controls
            Column(
                modifier = Modifier
                    .weight(0.6f) // Given more weight because it contains a lot of controls
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Text(
                    text = "Current Time: ${currentTime / 1000}s / ${videoDuration / 1000}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Timeline Visualization
                val listState = rememberLazyListState()
                
                // Track if the scroll is from a drag vs animation
                val isDragged by listState.interactionSource.collectIsDraggedAsState()
                
                LaunchedEffect(isDragged) {
                    if (isDragged && isPlaying) {
                        onPauseVideo()
                    }
                }
                
                // Calculate the target block index that should be visible/centered
                val activeBlockIndex = selectedBlockIndex ?: (currentTime / 1000).toInt()
                
                LaunchedEffect(activeBlockIndex) {
                    if (isDragged != true && activeBlockIndex >= 0 && activeBlockIndex < timelineBlocks.size) {
                        // Animate scroll to the target item.
                        // We use a small negative scroll offset to roughly center it in the view
                        listState.animateScrollToItem(activeBlockIndex, scrollOffset = -150)
                    }
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(timelineBlocks) { block ->
                        val isCurrent = (currentTime / 1000).toInt() == block.secondIndex
                        val isSelected = selectedBlockIndex == block.secondIndex
                        
                        // We only want ONE button to be highlighted.
                        // If something is explicitly selected (modal open), only that one gets highlighted.
                        // Otherwise, the currently playing second gets highlighted.
                        val isActiveOrSelected = if (selectedBlockIndex != null) {
                            isSelected
                        } else {
                            isCurrent
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .fillMaxHeight()
                                .background(
                                    color = when {
                                        block.isFullySkipped -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                        block.isPartiallySkipped -> Color(0xFFFFA500).copy(alpha = 0.6f) // Orange for partial
                                        block.isBuffer -> Color.Black.copy(alpha = 0.8f) // Black for buffer seconds
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = if (isActiveOrSelected) 2.dp else 0.dp,
                                    color = if (isActiveOrSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    selectedBlockIndex = block.secondIndex
                                    onSeekTo(block.startTimeMs)
                                    showFineTuneModal = block
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${block.secondIndex}s",
                                color = if (block.isBuffer) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlayPauseReplayButton(
                        isPlaying = isPlaying,
                        isPlaybackCompleted = isPlaybackCompleted,
                        onTogglePlayPause = {
                            if (isPlaybackCompleted) onReplay() else onTogglePlayPause() 
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onRestoreOriginalVideo() }, 
                        modifier = Modifier.weight(1f),
                        enabled = hasModifications,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error, // Red
                            disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        ),
                        border = BorderStroke(1.dp, if (hasModifications) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Text("↺", maxLines = 1, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showVideoPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Import New Video", maxLines = 1)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Captions Dropdown Row
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { showDropdown = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        val text = when (captionPosition) {
                            CaptionPosition.TOP -> "Top Captions"
                            CaptionPosition.BOTTOM -> "Bottom Captions"
                            CaptionPosition.NONE -> "No Captions"
                        }
                        Text("$text ▼", maxLines = 1)
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Top Captions", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                onUpdateCaptionPosition(CaptionPosition.TOP)
                                showDropdown = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bottom Captions", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                onUpdateCaptionPosition(CaptionPosition.BOTTOM)
                                showDropdown = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("No Captions", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                onUpdateCaptionPosition(CaptionPosition.NONE)
                                showDropdown = false 
                            }
                        )
                    }
                }
            }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                StudioBottomNavigationRow(
                    onBack = onNavigateBack,
                    onArchive = { showArchiveDialog = true },
                    actionText = "Publish →",
                    onAction = {
                        onSaveModifiedVideo {
                            onNavigateForward()
                        }
                    }
                )
            }
        }
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archive Video?") },
            text = { Text("Are you sure you want to archive this video and script?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showArchiveDialog = false
                    onArchiveScript()
                    onNavigateBack()
                }) { Text("Yes", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (isProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    // Fine-Tune Modal
    if (showFineTuneModal != null) {
        val block = showFineTuneModal!!
        ModalBottomSheet(
            onDismissRequest = { 
                showFineTuneModal = null
                selectedBlockIndex = null // Clear selection when modal closes
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Fine-Tune Second ${block.secondIndex}", 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap a 0.1s block to toggle removal (red = skipped).", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { 
                            onSkipAllSubBlocks(block.secondIndex)
                            showFineTuneModal = getTimelineBlock(block.secondIndex)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Skip All")
                    }
                    OutlinedButton(
                        onClick = { 
                            onUnskipAllSubBlocks(block.secondIndex)
                            showFineTuneModal = getTimelineBlock(block.secondIndex)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Green),
                        border = BorderStroke(1.dp, Color.Green)
                    ) {
                        Text("Skip None")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    block.subBlocks.forEachIndexed { index, subBlock ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(1.dp)
                                .background(
                                    if (subBlock.isSkipped) MaterialTheme.colorScheme.error else Color.Green,
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable {
                                    onToggleSubBlockSkip(block.secondIndex, index)
                                    showFineTuneModal = getTimelineBlock(block.secondIndex)
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        showFineTuneModal = null
                        selectedBlockIndex = null // Clear selection when Done is clicked
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
fun EditingStudioScreenPreview() {
    val sampleTimelineBlocks = listOf(
        TimelineBlock(
            secondIndex = 0,
            startTimeMs = 0L,
            endTimeMs = 1000L,
            subBlocks = List(10) { i ->
                TimelineSubBlock(startTimeMs = i * 100L, endTimeMs = (i + 1) * 100L, isSkipped = false)
            }
        ),
        TimelineBlock(
            secondIndex = 1,
            startTimeMs = 1000L,
            endTimeMs = 2000L,
            subBlocks = List(10) { i ->
                TimelineSubBlock(startTimeMs = 1000L + i * 100L, endTimeMs = 1000L + (i + 1) * 100L, isSkipped = i > 5)
            }
        ),
        TimelineBlock(
            secondIndex = 2,
            startTimeMs = 2000L,
            endTimeMs = 3000L,
            subBlocks = List(10) { i ->
                TimelineSubBlock(startTimeMs = 2000L + i * 100L, endTimeMs = 2000L + (i + 1) * 100L, isSkipped = true)
            }
        )
    )

    MovieTheme {
        EditingStudioScreenContent(
            isPlaying = true,
            seekRequest = null,
            videoPath = "sample/path/video.mp4",
            currentTime = 1500L,
            videoDuration = 3000L,
            timelineBlocks = sampleTimelineBlocks,
            isPlaybackCompleted = false,
            captionPosition = CaptionPosition.BOTTOM,
            isProcessing = false,
            hasModifications = true,
            onNavigateBack = {},
            onNavigateForward = {},
            onPauseVideo = {},
            onVideoImported = {},
            onUpdateCurrentTime = {},
            onVideoCompletion = {},
            onSeekTo = {},
            onReplay = {},
            onTogglePlayPause = {},
            onRestoreOriginalVideo = {},
            onUpdateCaptionPosition = {},
            onSaveModifiedVideo = {},
            onArchiveScript = {},
            onSkipAllSubBlocks = {},
            onUnskipAllSubBlocks = {},
            onToggleSubBlockSkip = { _, _ -> },
            getTimelineBlock = { sampleTimelineBlocks.getOrNull(it) }
        )
    }
}