package com.example.smarthome.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006495),
    secondary = Color(0xFF50606F),
    tertiary = Color(0xFF65587B),
    background = Color(0xFFF8F9FF),
    surface = Color(0xFFF8F9FF),
    error = Color(0xFFBA1A1A)
)

@Composable
fun SmartHomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}