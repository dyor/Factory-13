package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.domain.getDatabaseBuilder
import org.example.project.di.AppContainer

fun MainViewController() = ComposeUIViewController { 
    AppContainer.init(getDatabaseBuilder())
    App() 
}