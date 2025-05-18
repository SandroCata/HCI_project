package com.example.budgify.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.screen.CategoriesScreen
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.screen.CreditsDebitsScreen
import com.example.budgify.screen.Homepage
import com.example.budgify.screen.ObjectivesManagementScreen
import com.example.budgify.screen.ObjectivesScreen
import com.example.budgify.screen.Settings
import com.example.budgify.screen.TransactionsScreen
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.ThemePreferenceManager

@Composable
fun NavGraph(
    viewModel: FinanceViewModel,
    // Rimuovi il valore di default per navController per assicurarti che sia sempre passato
    // se MainActivity lo gestisce, o mantienilo se questo NavGraph può essere usato altrove
    // con un controller di default. Per coerenza con il passaggio da MainActivity:
    // navController: NavHostController,
    themePreferenceManager: ThemePreferenceManager,
    onThemeChange: (AppTheme) -> Unit,
    startDestination: String, // Riceve la start destination da MainActivity
    onForgotPinClicked: () -> Unit, // Callback per PIN dimenticato
    // Aggiungiamo un NavController opzionale con valore di default se MainActivity non lo passa ancora
    navController: NavHostController = rememberNavController()
) {
    // La logica per determinare initialPinSet e startDestination è ora in MainActivity
    // Quindi NavHost usa direttamente il parametro startDestination.

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ScreenRoutes.AccessPin.route) {
            PinEntryScreen(
                onPinCorrect = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(ScreenRoutes.AccessPin.route) { inclusive = true }
                    }
                },
                onForgotPin = onForgotPinClicked // Passa il callback qui
            )
        }

        composable(ScreenRoutes.Categories.route) { CategoriesScreen(navController, viewModel) }
        composable(ScreenRoutes.Home.route) { Homepage(navController, viewModel) }
        composable(ScreenRoutes.Objectives.route) { ObjectivesScreen(navController, viewModel) }
        composable(ScreenRoutes.ObjectivesManagement.route) { ObjectivesManagementScreen(navController, viewModel) }
        composable(ScreenRoutes.Settings.route) {
            Settings(
                // Assicurati che la tua Settings screen sia Settings e non SettingsScreen se hai cambiato nome
                navController = navController,
                viewModel = viewModel,
                //themePreferenceManager = themePreferenceManager, // Passa themePreferenceManager
                onThemeChange = onThemeChange, // Passa il callback
                // Dovresti aggiungere anche callbacks per onPinSuccessfullySet e onPinCleared
                // se vuoi che Settings possa comunicare questi cambiamenti a MainActivity
                // per un aggiornamento UI immediato senza riavvio. Esempio:
                // onPinSettingsChanged = { requiresPinEntry -> /* callback a MainActivity */ }
            )
        }
        composable(ScreenRoutes.Transactions.route) { TransactionsScreen(navController, viewModel) }
        composable(ScreenRoutes.CredDeb.route) { CreditsDebitsScreen(navController, viewModel) }
    }
}

// Helper function to read PIN (può rimanere qui o essere spostata in un file utility se usata altrove)
fun getSavedPinFromContext(context: Context): String? {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "AppSettings", // Nome consistente con MainActivity
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.getString("access_pin", null)
    } catch (e: Exception) {
        Log.e("NavGraph", "Error reading PIN", e)
        null
    }
}

@Composable
fun PinEntryScreen(
    onPinCorrect: () -> Unit,
    onForgotPin: () -> Unit // Nuovo callback
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enter Access PIN", style = MaterialTheme.typography.headlineSmall)

            TextField(
                value = enteredPin,
                onValueChange = {
                    if (it.length <= 6) { // Esempio: limita la lunghezza del PIN se necessario
                        enteredPin = it
                    }
                    errorMessage = null
                },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val savedPin = getSavedPinFromContext(context)
                    if (enteredPin == savedPin && savedPin != null) {
                        onPinCorrect()
                    } else {
                        errorMessage = "Incorrect PIN"
                        enteredPin = "" // Opzionale: cancella il campo dopo un tentativo errato
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enteredPin.isNotBlank() // Disabilita il pulsante se il PIN è vuoto
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(8.dp)) // Aggiungi uno spacer

            TextButton(onClick = onForgotPin) { // Pulsante per PIN Dimenticato
                Text("Forgot PIN?")
            }
        }
    }
}