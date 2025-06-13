package com.example.budgify.userpreferences
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

enum class AppTheme(val displayName: String, val unlockLevel: Int) {
    LIGHT("Light", 1),              // Default, unlocked at level 1
    DARK("Dark", 1),                // Default, unlocked at level 1
    OCEAN_BLUE("Ocean Blue", 2),    // Unlocks at Level 5
    FOREST_GREEN("Forest Green", 3), // Unlocks at Level 10
    SUNSET_ORANGE("Sunset Orange", 4) // Unlocks at Level 15
    // Add more themes and their unlock levels if you wish
}

class ThemePreferenceManager(context: Context) {
    private val sharedPreferences: EncryptedSharedPreferences by lazy<EncryptedSharedPreferences> {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Crea l'istanza e assegnala a una variabile tipizzata esplicitamente
            val encryptedPrefs: EncryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "ThemeSettings", // File name for theme preferences
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
            encryptedPrefs // Restituisci la variabile tipizzata

        } catch (e: Exception) {
            Log.e("ThemePreferenceManager", "Error creating EncryptedSharedPreferences for theme", e)
            throw RuntimeException("Could not initialize EncryptedSharedPreferences for theme", e) as Nothing
        }
    }

    fun getSavedTheme(): AppTheme {
        val themeString = sharedPreferences.getString("app_theme", AppTheme.LIGHT.name)
        return try {
            AppTheme.valueOf(themeString ?: AppTheme.LIGHT.name)
        } catch (e: IllegalArgumentException) {
            Log.e("ThemePreferenceManager", "Invalid saved theme value: $themeString", e)
            AppTheme.LIGHT // Default to light theme in case of invalid saved value
        }
    }

    fun saveTheme(theme: AppTheme) {
        with(sharedPreferences.edit()) {
            putString("app_theme", theme.name)
            apply()
        }
    }
}

@Composable
fun rememberThemePreferenceManager(): ThemePreferenceManager {
    val context = LocalContext.current
    return remember { ThemePreferenceManager(context) }
}