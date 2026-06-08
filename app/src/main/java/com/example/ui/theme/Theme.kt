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

private val DarkColorScheme =
  darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    secondary = SophisticatedSecondary,
    onSecondary = SophisticatedOnSecondary,
    tertiary = SophisticatedTertiary,
    onTertiary = SophisticatedOnTertiary,
    background = SophisticatedBg,
    onBackground = SophisticatedOnBg,
    surface = SophisticatedSurface,
    onSurface = SophisticatedOnSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onSurfaceVariant = SophisticatedOnSurfaceVariant,
    outline = SophisticatedOutline
  )

private val LightColorScheme = DarkColorScheme // Always use Sophisticated Dark for this premium app

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Force custom branding instead of dynamic color
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
