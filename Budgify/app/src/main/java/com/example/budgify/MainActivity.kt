package com.example.budgify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceRepository
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.navigation.NavGraph
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.ui.theme.BudgifyTheme
import com.example.budgify.userpreferences.ThemePreferenceManager

class MainActivity : ComponentActivity() {

    // Non è più necessario spostare themePreferenceManager qui se non serve in clearSavedPin
    // private lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreferenceManager = ThemePreferenceManager(this)

        setContent {
            var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }
            val navController = rememberNavController() // Crea il NavController qui

            // Stato per determinare se il PIN è necessario.
            // Questo stato verrà aggiornato e causerà la ricomposizione,
            // influenzando la startDestination del NavGraph.
            var requiresPinEntry by remember { mutableStateOf(getSavedPin() != null) }
            var showPinResetConfirmationDialog by remember { mutableStateOf(false) }

            // Determina la startDestination dinamicamente
            val startDestination = if (requiresPinEntry) {
                ScreenRoutes.AccessPin.route // La tua route per la schermata di inserimento PIN
            } else {
                ScreenRoutes.Home.route // La tua route per la schermata principale
            }

            // ViewModel
            val application = application as FinanceApplication
            val database = application.database
            val repository = FinanceRepository(
                database.transactionDao(),
                database.accountDao(),
                database.goalDao(),
                database.categoryDao(),
                database.loanDao()
            )
            val financeViewModel: FinanceViewModel =
                viewModel(factory = FinanceViewModel.FinanceViewModelFactory(repository))

            BudgifyTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Mostra il dialogo di conferma reset PIN se necessario
                    if (showPinResetConfirmationDialog) {
                        PinResetConfirmationDialog(
                            onConfirm = {
                                clearSavedPin()
                                requiresPinEntry = false // Aggiorna lo stato per riflettere che il PIN non è più richiesto
                                showPinResetConfirmationDialog = false
                                // Dopo il reset, naviga alla home e pulisci lo stack di navigazione
                                // relativo alla schermata del PIN.
                                navController.navigate(ScreenRoutes.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onDismiss = {
                                showPinResetConfirmationDialog = false
                            }
                        )
                    }

                    NavGraph(
                        navController = navController, // Passa il NavController
                        viewModel = financeViewModel,
                        themePreferenceManager = themePreferenceManager,
                        onThemeChange = { newTheme ->
                            themePreferenceManager.saveTheme(newTheme)
                            currentTheme = newTheme
                        },
                        startDestination = startDestination, // Passa la startDestination dinamica
                        onForgotPinClicked = {
                            showPinResetConfirmationDialog = true
                        }
                        // Callback per quando un PIN viene impostato/cancellato da Settings
                        // Questo permette a MainActivity di aggiornare 'requiresPinEntry'
                        // e far ricomporre NavGraph con la nuova startDestination se necessario,
                        // o semplicemente di essere consapevole del cambiamento per il prossimo avvio.
                        // onPinExistenceChanged = { pinExists ->
                        //    requiresPinEntry = pinExists
                        // }
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

    private fun clearSavedPin() {
        try {
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
            with(sharedPreferences.edit()) {
                remove("access_pin")
                apply()
            }
            Log.i("MainActivity", "Saved PIN has been cleared.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error clearing PIN", e)
        }
    }
}

@Composable
fun PinResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset PIN?") },
        text = { Text("If you reset the PIN, you will need to set a new one. Any data specifically secured by the old PIN might become inaccessible if not backed up. Are you sure you want to continue?") }, // Testo leggermente modificato
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset PIN", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

