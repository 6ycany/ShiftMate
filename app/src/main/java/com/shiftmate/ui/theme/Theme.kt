package com.shiftmate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Yellow-green / Pale-red palette ──────────────────────────────
val AppGreen        = Color(0xFF7CB342)   // Primary – Yellow-green 600
val AppGreenDark    = Color(0xFF558B2F)   // Dark – Yellow-green 800
val AppGreenLight   = Color(0xFFDCEDC8)  // Container – Light green
val AppRed          = Color(0xFFEF9A9A)  // Secondary – Pale red
val AppRedDark      = Color(0xFFC62828)  // Error
val AppRedContainer = Color(0xFFFFCDD2)  // Secondary container

private val LightColors = lightColorScheme(
    primary              = AppGreen,
    onPrimary            = Color.White,
    primaryContainer     = AppGreenLight,
    onPrimaryContainer   = AppGreenDark,
    secondary            = Color(0xFFE57373),
    onSecondary          = Color.White,
    secondaryContainer   = AppRedContainer,
    onSecondaryContainer = AppRedDark,
    error                = AppRedDark,
    background           = Color(0xFFF8FFF4),
    surface              = Color(0xFFFFFFFF),
    surfaceVariant       = Color(0xFFF0F7E8)
)

@Composable
fun ShiftMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
