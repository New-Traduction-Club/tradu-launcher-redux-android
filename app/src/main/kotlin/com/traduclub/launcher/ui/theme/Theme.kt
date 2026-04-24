package com.traduclub.launcher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// custom dark color palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF1C1B1F),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFFB2F4EC),
    tertiary = Color(0xFFFF7597),
    onTertiary = Color(0xFF1C1B1F),
    background = Color(0xFF0E0E12),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF15151A),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF1E1E24),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF1C1B1F),
    outline = Color(0xFF938F99)
)

@Composable
fun TraduLauncherTheme(
    content: @Composable () -> Unit
) {
    // use dynamic colors on android 12+ if available, otherwise use our custom palette
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
