package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.compose_multiplatform
import kotlinproject.shared.generated.resources.film_noir

import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.compose.runtime.mutableStateListOf
import org.example.project.ui.Screen
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer

import org.example.project.permissions.AppPermissionsHandler

import org.example.project.ui.writersroom.WritersRoomScreen
import org.example.project.ui.writersroom.WritersRoomViewModel
import org.example.project.di.AppContainer

import org.example.project.ui.recordingstudio.RecordingStudioScreen
import org.example.project.ui.recordingstudio.RecordingStudioViewModel

import org.example.project.ui.editingstudio.EditingStudioScreen
import org.example.project.ui.editingstudio.EditingStudioViewModel

import org.example.project.ui.home.HomeScreen
import org.example.project.ui.home.HomeViewModel

import org.example.project.ui.publishingstudio.PublishingStudioScreen
import org.example.project.ui.publishingstudio.PublishingStudioViewModel

import org.example.project.ui.archives.ArchivesScreen
import org.example.project.ui.archives.ArchivesViewModel

import org.example.project.ui.theme.MovieTheme

@Composable
@Preview
fun App() {
    MovieTheme {
        AppPermissionsHandler {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.film_noir),
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                val backStack = rememberSerializable(serializer = SnapshotStateListSerializer()) {
                    mutableStateListOf<Screen>(Screen.Home)
                }
                val onBack = { if (backStack.size > 1) backStack.removeLast() }
    
                val onNavigateHome = { 
                    backStack.clear()
                    backStack.add(Screen.Home)
                }
    
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .safeContentPadding()
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = onBack,
                        entryProvider = entryProvider {
                            entry<Screen.Home> {
                                val homeViewModel = remember {
                                    HomeViewModel(scriptDao = AppContainer.scriptDao)
                                }
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onNavigateToWritersRoom = { backStack.add(Screen.WritersRoom) },
                                    onNavigateToRecordingStudio = { backStack.add(Screen.RecordingStudio) },
                                    onNavigateToEditingStudio = { backStack.add(Screen.EditingStudio) },
                                    onNavigateToPublishingStudio = { backStack.add(Screen.PublishingStudio) },
                                    onNavigateToArchives = { backStack.add(Screen.Archives) }
                                )
                            }
                            entry<Screen.WritersRoom> {
                                val viewModel = remember { 
                                    WritersRoomViewModel(
                                        scriptDao = AppContainer.scriptDao,
                                        geminiClient = AppContainer.geminiClient
                                    ) 
                                }
                                WritersRoomScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { onNavigateHome() },
                                    onNavigateToRecording = { backStack.add(Screen.RecordingStudio) }
                                )
                            }
                            entry<Screen.RecordingStudio> {
                                val recordingViewModel = remember {
                                    RecordingStudioViewModel(scriptDao = AppContainer.scriptDao)
                                }
                                RecordingStudioScreen(
                                    viewModel = recordingViewModel,
                                    onNavigateBack = { onNavigateHome() },
                                    onNavigateForward = { backStack.add(Screen.EditingStudio) }
                                )
                            }
                            entry<Screen.EditingStudio> {
                                val editingViewModel = remember {
                                    EditingStudioViewModel(scriptDao = AppContainer.scriptDao)
                                }
                                EditingStudioScreen(
                                    viewModel = editingViewModel,
                                    onNavigateBack = { onNavigateHome() },
                                    onNavigateForward = { backStack.add(Screen.PublishingStudio) }
                                )
                            }
                            entry<Screen.PublishingStudio> {
                                val publishingViewModel = remember {
                                    PublishingStudioViewModel(scriptDao = AppContainer.scriptDao)
                                }
                                PublishingStudioScreen(
                                    viewModel = publishingViewModel,
                                    onNavigateBack = { onNavigateHome() },
                                    onNavigateHome = { onNavigateHome() }
                                )
                            }
                            entry<Screen.Archives> {
                                val archivesViewModel = remember {
                                    ArchivesViewModel(scriptDao = AppContainer.scriptDao)
                                }
                                ArchivesScreen(
                                    viewModel = archivesViewModel,
                                    onNavigateBack = { onNavigateHome() },
                                    onNavigateToStudio = { state ->
                                        backStack.clear()
                                        backStack.add(Screen.Home)
                                        when (state) {
                                            "WRITERS_ROOM" -> backStack.add(Screen.WritersRoom)
                                            "RECORDING_STUDIO" -> backStack.add(Screen.RecordingStudio)
                                            "EDITING_STUDIO" -> backStack.add(Screen.EditingStudio)
                                            "PUBLISHING_STUDIO" -> backStack.add(Screen.PublishingStudio)
                                        }
                                    }
                                )
                            }
                        },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    )
                }
            }
        }
    }
}