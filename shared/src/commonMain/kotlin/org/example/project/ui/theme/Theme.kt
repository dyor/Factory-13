package org.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700), // Gold for primary actions/highlights
    onPrimary = Color.Black,
    secondary = Color(0xFFB0BEC5), // Light Slate Gray
    onSecondary = Color.Black,
    tertiary = Color(0xFF607D8B), // Dark Slate Gray
    onTertiary = Color.White,
    background = Color(0xFF121212), // Very dark background
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E), // Slightly lighter dark surface for cards/panels
    onSurface = Color.White,
    error = Color(0xFFCF6679), // Red for errors
    onError = Color.Black,
    surfaceVariant = Color(0xFF3C3C3C), // Used for text field containers, etc.
    onSurfaceVariant = Color.LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB0BEC5), // Placeholder, can be refined
    onPrimary = Color.Black,
    secondary = Color(0xFF607D8B),
    onSecondary = Color.White,
    tertiary = Color(0xFF78909C),
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF0F0F0),
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White,
    surfaceVariant = Color(0xFFD3D3D3),
    onSurfaceVariant = Color.DarkGray
)

@Composable
fun MovieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // We primarily focus on dark theme for Film Noir, light theme is basic
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // We will define this later or use default Material 3
        content = content
    )
}

// TODO: Define custom Typography if needed, otherwise use MaterialTheme.typography defaults.
val Typography = MaterialTheme.typography
