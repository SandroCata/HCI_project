package com.example.budgify

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.error
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
import com.example.budgify.screen.Homepage
import com.example.budgify.screen.ObjectivesManagementScreen
import com.example.budgify.screen.ObjectivesScreen
import com.example.budgify.screen.Settings
import com.example.budgify.screen.TransactionsScreen

@Composable
fun NavGraph(
    viewModel: FinanceViewModel,
    navController: NavHostController = rememberNavController()
) {
    // Read the PIN state once when the NavGraph is created
    val context = LocalContext.current
    val initialPinSet = remember { getSavedPinFromContext(context) != null }

    // Determine the start destination based on whether a PIN is set
    val startDestination = if (initialPinSet) {
        ScreenRoutes.AccessPin.route
    } else {
        ScreenRoutes.Home.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ScreenRoutes.AccessPin.route) {
            // Composable for PIN entry screen
            PinEntryScreen(
                onPinCorrect = {
                    // Navigate to the home screen after correct PIN
                    navController.navigate(ScreenRoutes.Home.route) {
                        // Pop up the PIN entry screen so the user can't go back to it
                        popUpTo(ScreenRoutes.AccessPin.route) { inclusive = true }
                    }
                }
                // PinEntryScreen will handle reading the saved PIN for verification
            )
        }

        // Your existing screens
        composable(ScreenRoutes.Categories.route) { CategoriesScreen(navController) }
        composable(ScreenRoutes.Home.route) { Homepage(navController, viewModel) }
        composable(ScreenRoutes.Objectives.route) { ObjectivesScreen(navController, viewModel) }
        composable(ScreenRoutes.ObjectivesManagement.route) { ObjectivesManagementScreen(navController, viewModel) }
        composable(ScreenRoutes.Settings.route) { Settings(navController, viewModel) }
        composable(ScreenRoutes.Transactions.route) { TransactionsScreen(navController, viewModel) }

        /*
        // Keep your commented out composables if you plan to use them later
        composable("manage_objectives_screen") { ManageObjectives(navController) }
        composable("cred_deb_screen") { CredDeb() }
        composable("credits_screen") { Credits() }
        composable("debits_screen") { Debits() }
        composable("categories_screen") { Categories() }
        composable("adding_screen") { Adding() }
         */
    }
}

// Helper function to read PIN within a Composable (consider moving this for better separation)
fun getSavedPinFromContext(context: Context): String? {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "AppSettings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.getString("access_pin", null)
    } catch (e: Exception) {
        Log.e("NavGraph", "Error reading PIN in Composable", e)
        null
    }
}


// Example placeholder Composable for PIN entry (you need to create this)
@Composable
fun PinEntryScreen(onPinCorrect: () -> Unit) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // State for error message
    val context = LocalContext.current

    // Use a Box to center the content
    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .padding(16.dp), // Add some padding
        contentAlignment = Alignment.Center // Center the content within the Box
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally in the Column
            verticalArrangement = Arrangement.spacedBy(16.dp), // Add vertical spacing
            modifier = Modifier.fillMaxWidth() // Make the column fill the width
        ) {
            Text("Enter Access PIN", style = MaterialTheme.typography.headlineSmall)

            TextField(
                value = enteredPin,
                onValueChange = {
                    enteredPin = it
                    errorMessage = null // Clear error when user types
                },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(), // Hide the text
                modifier = Modifier.fillMaxWidth() // Fill the width
            )

            // Show error message if present
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val savedPin = getSavedPinFromContext(context) // Use the helper function
                    if (enteredPin == savedPin && savedPin != null) {
                        onPinCorrect()
                    } else {
                        errorMessage = "Incorrect PIN" // Set error message
                    }
                },
                modifier = Modifier.fillMaxWidth() // Fill the width
            ) {
                Text("Submit")
            }
        }
    }
}