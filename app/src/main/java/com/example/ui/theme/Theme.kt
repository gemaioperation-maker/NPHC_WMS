package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberTeal,
    secondary = SafetyGold,
    tertiary = CopilotPurple,
    background = CyberSlateBg,
    surface = CyberSlateSurface,
    onPrimary = Color(0xFF381E72), // Rich dark purple contrast on light purple primary
    onSecondary = Color(0xFF332D41), // Deep contrast on light lavender-slate
    onTertiary = Color(0xFFE6E1E5), // Light high contrast on deep royal purple
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = LaserRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force industrial dark-slate aesthetic by default for optimal warehouse scanning ergonomic contrast
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
