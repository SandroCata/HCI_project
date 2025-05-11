package com.example.budgify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceRepository
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.navigation.NavGraph
import com.example.budgify.ui.theme.BudgifyTheme
import com.example.budgify.userpreferences.ThemePreferenceManager


//Flag per andare direttamente alla home oppure passare per lo sblocco con pin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for a saved PIN on app startup (mantieni la tua logica PIN esistente)
        val savedPin = getSavedPin()

        // Inizializza ThemePreferenceManager
        val themePreferenceManager = ThemePreferenceManager(this)

        setContent {
            // Leggi la preferenza del tema all'avvio e osservala per i cambiamenti
            var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }

            // Manteniamo la tua logica per il ViewModel
            val application = application as FinanceApplication
            val database = application.database
            val repository = FinanceRepository(
                database.transactionDao(),
                database.accountDao(),
                database.goalDao()
            )
            val financeViewModel: FinanceViewModel =
                viewModel(factory = FinanceViewModel.FinanceViewModelFactory(repository))

            // Avvolgi il tuo NavGraph con la funzione del tema
            BudgifyTheme(appTheme = currentTheme) { // Passa il tema corrente qui
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Il tuo NavGraph è il contenuto che riceverà il tema
                    NavGraph(
                        viewModel = financeViewModel,
                        // Potresti aver bisogno di passare il gestore delle preferenze o un callback
                        // se la schermata delle impostazioni si trova all'interno del NavGraph
                        onThemeChange = { newTheme -> // Esempio: passa un callback
                            themePreferenceManager.saveTheme(newTheme) // Salva la nuova preferenza
                            currentTheme = newTheme // Aggiorna lo stato qui per innescare la ricomposizione
                        }, // Esempio: passa il gestore
                        themePreferenceManager = themePreferenceManager
                    )
                }
            }
        }
    }

    private fun getSavedPin(): String? {
        return try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                this,
                "AppSettings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            sharedPreferences.getString("access_pin", null)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading PIN", e)
            null
        }
    }
}