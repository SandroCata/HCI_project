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

@Composable
fun BudgifyTheme(
    appTheme: AppTheme, // Aggiungi il parametro del tema
    // Rimuovi il parametro darkTheme se non vuoi usare il tema di sistema di default
    content: @Composable () -> Unit
) {
    // Determina quale ColorScheme usare in base al parametro appTheme
    val colorScheme = when (appTheme) {
        AppTheme.DARK -> DarkColorScheme
        AppTheme.LIGHT -> LightColorScheme
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