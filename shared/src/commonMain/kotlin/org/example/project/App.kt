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

@Composable
@Preview
fun App() {
    MaterialTheme {
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
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Text("Home Screen Placeholder")
                                    Button(onClick = { backStack.add(Screen.WritersRoom) }) {
                                        Text("Go to Writers Room")
                                    }
                                    Button(onClick = { backStack.add(Screen.RecordingStudio) }) {
                                        Text("Go to Recording Studio")
                                    }
                                    Button(onClick = { backStack.add(Screen.EditingStudio) }) {
                                        Text("Go to Editing Studio")
                                    }
                                    Button(onClick = { backStack.add(Screen.PublishingStudio) }) {
                                        Text("Go to Publishing Studio")
                                    }
                                    Button(onClick = { backStack.add(Screen.Archives) }) {
                                        Text("Go to Archives")
                                    }
                                }
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
                                    onNavigateBack = onBack,
                                    onNavigateToRecording = { backStack.add(Screen.RecordingStudio) }
                                )
                            }
                            entry<Screen.RecordingStudio> {
                                val recordingViewModel = remember {
                                    RecordingStudioViewModel(scriptDao = AppContainer.scriptDao)
                                }
                                RecordingStudioScreen(
                                    viewModel = recordingViewModel,
                                    onNavigateBack = onBack,
                                    onNavigateForward = { backStack.add(Screen.EditingStudio) }
                                )
                            }
                            entry<Screen.EditingStudio> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Text("Editing Studio Placeholder")
                                    Button(onClick = { onBack() }) {
                                        Text("Back to Home")
                                    }
                                }
                            }
                            entry<Screen.PublishingStudio> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Text("Publishing Studio Placeholder")
                                    Button(onClick = { onBack() }) {
                                        Text("Back to Home")
                                    }
                                }
                            }
                            entry<Screen.Archives> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Text("Archives Placeholder")
                                    Button(onClick = { onBack() }) {
                                        Text("Back to Home")
                                    }
                                }
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