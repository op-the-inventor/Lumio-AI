package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF000000),      // DeepCosmic (Pitch black)
    surface = Color(0xFF121212),         // DarkSurface (Dark gray)
    onBackground = Color(0xFFF5F5F7),    // TextLight (Off-white)
    onSurface = Color(0xFFF5F5F7),       // TextLight
    surfaceVariant = Color(0xFF1C1C1E),   // Secondary dark gray
    onSurfaceVariant = Color(0xFF8E8E93),  // TextMuted
    primaryContainer = Color(0xFF222222),
    onPrimaryContainer = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1C1C1E),         // Charcoal black accent
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF5F5F7),      // Clean light background
    surface = Color(0xFFFFFFFF),         // Clean white panels
    onBackground = Color(0xFF1C1C1E),    // Dark text
    onSurface = Color(0xFF1C1C1E),       // Dark text
    surfaceVariant = Color(0xFFE5E5EA),   // Soft light container
    onSurfaceVariant = Color(0xFF636366),  // Muted gray text
    primaryContainer = Color(0xFFE5E5EA),
    onPrimaryContainer = Color(0xFF1C1C1E)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
