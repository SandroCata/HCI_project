package com.example.budgify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.budgify.userpreferences.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Gray,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black

)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Gray,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB00020),
    onError = Color.White
)

// Example: Ocean Blue Theme (Light)
private val OceanBlueColorScheme = lightColorScheme(
    primary = Color(0xFF00609D), // Example: Deep Blue
    secondary = Color(0xFF4DB6AC), // Example: Teal
    tertiary = Color(0xFF81D4FA), // Example: Light Sky Blue
    background = Color(0xFFE0F7FA), // Example: Very Light Cyan
    surface = Color(0xFFFFFFFF),    // Example: White
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF001F2A), // Example: Dark Blue-Gray for text
    onSurface = Color(0xFF001F2A),   // Example: Dark Blue-Gray for text on surface
    error = Color(0xFFB00020),
    onError = Color.White
    // Define all necessary colors
)

// Example: Forest Green Theme (Dark)
private val ForestGreenColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A), // Example: Medium Green
    secondary = Color(0xFFA5D6A7), // Example: Light Green
    tertiary = Color(0xFF81C784), // Example: Another shade of Green
    background = Color(0xFF1B261B), // Example: Very Dark Green
    surface = Color(0xFF2E3B2E),    // Example: Dark Greenish Gray
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE8F5E9), // Example: Very Light Green for text
    onSurface = Color(0xFFE8F5E9),   // Example: Very Light Green for text on surface
    error = Color(0xFFCF6679),
    onError = Color.Black
    // Define all necessary colors
)

// Example: Sunset Orange Theme (Light)
private val SunsetOrangeColorScheme = lightColorScheme(
    primary = Color(0xFFFF7043), // Example: Bright Orange
    secondary = Color(0xFFFFAB91), // Example: Light Orange/Peach
    tertiary = Color(0xFFFFCC80), // Example: Lighter Peach
    background = Color(0xFFFFF3E0), // Example: Very Light Orange/Cream
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF4E342E), // Example: Dark Brown for text
    onSurface = Color(0xFF4E342E),
    error = Color(0xFFB00020),
    onError = Color.White
    // Define all necessary colors
)

@Composable
fun BudgifyTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit
) {
    // Determina quale ColorScheme usare in base al parametro appTheme
    val colorScheme = when (appTheme) {
        AppTheme.DARK -> DarkColorScheme
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.OCEAN_BLUE -> OceanBlueColorScheme
        AppTheme.FOREST_GREEN -> ForestGreenColorScheme
        AppTheme.SUNSET_ORANGE -> SunsetOrangeColorScheme
    }
    // Se volessi ancora considerare il tema di sistema come fallback, potresti fare così:
    // val darkTheme = appTheme == AppTheme.DARK || (appTheme == AppTheme.SYSTEM && isSystemInDarkTheme())
    // val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}