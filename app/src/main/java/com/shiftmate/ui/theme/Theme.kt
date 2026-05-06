package com.shiftmate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF1976D2)
private val PrimaryContainer = Color(0xFFBBDEFB)
private val Secondary = Color(0xFF43A047)
private val Error = Color(0xFFE53935)

private val LightColors = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    error = Error
)

@Composable
fun ShiftMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
