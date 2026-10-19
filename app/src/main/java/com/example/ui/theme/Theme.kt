package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AeroCastColorScheme = darkColorScheme(
    primary = SkyCyanAccent,
    onPrimary = SkyDeepNavy,
    primaryContainer = SkySlateBlue,
    onPrimaryContainer = CloudFrostWhite,
    secondary = SunWarmAmber,
    onSecondary = SkyDeepNavy,
    tertiary = SunSoftGold,
    background = SkyDeepNavy,
    onBackground = CloudFrostWhite,
    surface = SkyIndigo,
    onSurface = CloudFrostWhite,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondaryIce,
    error = ErrorCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AeroCastColorScheme,
        typography = Typography,
        content = content
    )
}
