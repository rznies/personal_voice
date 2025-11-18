package io.livekit.android.example.voiceassistant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ========================================
// StarkJarvis Iron Man Theme
// ========================================

private val StarkDarkColorScheme = darkColorScheme(
    primary = ArcReactorBlue,
    onPrimary = StarkBlack,
    primaryContainer = ArcReactorBlueDark,
    onPrimaryContainer = ArcReactorBlueLight,

    secondary = StarkGold,
    onSecondary = StarkBlack,
    secondaryContainer = StarkGoldDark,
    onSecondaryContainer = StarkGoldLight,

    tertiary = IronRed,
    onTertiary = StarkWhite,
    tertiaryContainer = IronRedDark,
    onTertiaryContainer = IronRedLight,

    background = StarkBlack,
    onBackground = StarkWhite,
    surface = StarkDarkGray,
    onSurface = StarkWhite,
    surfaceVariant = StarkCharcoal,
    onSurfaceVariant = StarkLightGray,

    outline = StarkSlate,
    outlineVariant = GlassEffect,

    error = ErrorRed,
    onError = StarkWhite,
)

// Light theme for daytime operations (though Tony prefers dark mode)
private val StarkLightColorScheme = lightColorScheme(
    primary = ArcReactorBlueDark,
    onPrimary = StarkWhite,
    primaryContainer = ArcReactorBlueLight,
    onPrimaryContainer = StarkBlack,

    secondary = StarkGoldDark,
    onSecondary = StarkWhite,
    secondaryContainer = StarkGoldLight,
    onSecondaryContainer = StarkBlack,

    tertiary = IronRedDark,
    onTertiary = StarkWhite,
    tertiaryContainer = IronRedLight,
    onTertiaryContainer = StarkBlack,

    background = Color(0xFFF5F5F5),
    onBackground = StarkBlack,
    surface = StarkWhite,
    onSurface = StarkBlack,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = StarkMediumGray,

    outline = StarkMediumGray,
    outlineVariant = StarkLightGray,

    error = ErrorRed,
    onError = StarkWhite,
)

// Legacy color schemes for compatibility
private val DarkColorScheme = StarkDarkColorScheme
private val LightColorScheme = StarkLightColorScheme

/**
 * StarkJarvis Theme - Iron Man Edition
 * "Sometimes you gotta run before you can walk" - Tony Stark
 *
 * Features:
 * - Arc Reactor Blue as primary
 * - Stark Gold for accents
 * - Pure black backgrounds for OLED perfection
 * - Always dark mode (like Tony's workshop)
 */
@Composable
fun StarkJarvisTheme(
    darkTheme: Boolean = true, // Always dark mode by default (Tony approved)
    dynamicColor: Boolean = false, // Disable dynamic colors for consistent Iron Man aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StarkDarkColorScheme
        else -> StarkLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pure black status bar for OLED devices
            window.statusBarColor = StarkBlack.toArgb()
            // Light status bar icons on dark background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Legacy theme for compatibility
@Composable
fun LiveKitVoiceAssistantExampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    StarkJarvisTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}