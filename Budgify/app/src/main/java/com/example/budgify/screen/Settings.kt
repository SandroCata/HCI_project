package com.example.budgify.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.navigation.getSavedPinFromContext
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.rememberThemePreferenceManager
import kotlinx.coroutines.launch

// Enum per rappresentare le opzioni delle impostazioni selezionate
enum class SettingsOptionType {
    NONE, PIN, THEME, ABOUT
}

@Composable
fun Settings(navController: NavController, viewModel: FinanceViewModel, onThemeChange: (AppTheme) -> Unit) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Settings.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Stato per tenere traccia dell'opzione selezionata
    var selectedOption by remember { mutableStateOf(SettingsOptionType.NONE) }

    Scaffold(
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(
            navController,
            viewModel,
            showSnackbar = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sezione delle opzioni di impostazione
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp), // Aggiunto padding sotto le opzioni
                verticalArrangement = Arrangement.Top
            ) {
                SettingsOption(
                    icon = Icons.Default.Lock,
                    title = "Set an access PIN",
                    onClick = { selectedOption = SettingsOptionType.PIN }
                )
                Spacer(modifier = Modifier.height(8.dp)) // Spazio ridotto tra le opzioni
                SettingsOption(
                    icon = Icons.Default.NightsStay,
                    title = "Dark/Light Mode",
                    onClick = { selectedOption = SettingsOptionType.THEME }
                )
                Spacer(modifier = Modifier.height(8.dp)) // Spazio ridotto tra le opzioni
                SettingsOption(
                    icon = Icons.Default.Info,
                    title = "About the app",
                    onClick = { selectedOption = SettingsOptionType.ABOUT }
                )
            }

            // Sezione dei dettagli/azioni dell'opzione selezionata
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Occupa lo spazio rimanente
                    .padding(top = 16.dp), // Aggiunto padding sopra la sezione dei dettagli
                contentAlignment = Alignment.Center // Allinea il contenuto al centro
            ) {
                when (selectedOption) {
                    SettingsOptionType.NONE -> {
                        // Mostra un testo di benvenuto o istruzioni iniziali
                        Text("Select an option for more details", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    SettingsOptionType.PIN -> {
                        PinSettingsContent()
                    }
                    SettingsOptionType.THEME -> {
                        ThemeSettingsContent(onThemeChange)
                    }
                    SettingsOptionType.ABOUT -> {
                        AboutSettingsContent()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Normal)
    }
    Divider()
}

@Composable
fun PinSettingsContent() {
    // Stati per i campi di input del PIN
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Stato per mostrare messaggi di errore
    // Stato per mostrare messaggi di successo specifici per la rimozione del PIN
    var removeSuccessMessage by remember { mutableStateOf<String?>(null) }
    // Stato per mostrare messaggi di successo per il salvataggio del PIN
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Obtain the context to access SharedPreferences
    val context = LocalContext.current

    // Stato mutabile per tracciare se un PIN è attualmente impostato
    var isPinSet by remember { mutableStateOf(getSavedPinFromContext(context) != null) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth() // Riempie la larghezza disponibile
    ) {
        Text("PIN Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)

        // Controlla se mostrare il pulsante di rimozione o il messaggio di successo della rimozione
        if (isPinSet && removeSuccessMessage == null) { // Mostra il pulsante solo se il PIN è impostato E non c'è un messaggio di successo di rimozione
            Button(
                onClick = {
                    try {
                        val masterKey = MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()

                        val sharedPreferences = EncryptedSharedPreferences.create(
                            context,
                            "AppSettings", // File name for SharedPreferences
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )

                        // Remove the PIN key
                        with(sharedPreferences.edit()) {
                            remove("access_pin")
                            apply()
                        }

                        // Imposta il messaggio di successo per la rimozione
                        removeSuccessMessage = "PIN removed successfully"
                        saveSuccessMessage = null // Cancella il messaggio di successo per il salvataggio
                        errorMessage = null // Clear error message
                        newPin = "" // Clear input fields
                        confirmPin = ""
                        // Aggiorna lo stato isPinSet (opzionale qui se il messaggio prende il suo posto, ma utile per coerenza)
                        isPinSet = false


                    } catch (e: Exception) {
                        Log.e("PinSettingsContent", "Error removing PIN", e)
                        errorMessage = "Error removing PIN: ${e.localizedMessage}"
                        removeSuccessMessage = null // Cancella il messaggio di successo per la rimozione
                        saveSuccessMessage = null // Cancella il messaggio di successo per il salvataggio
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), // Use error color for "Remove"
                modifier = Modifier.fillMaxWidth() // Riempie la larghezza
            ) {
                Text("Remove secure access")
            }
        } else if (!isPinSet && removeSuccessMessage != null) { // Mostra il messaggio di successo della rimozione se il PIN NON è impostato E c'è un messaggio
            // Mostra il messaggio di successo specifico per la rimozione al posto del pulsante
            Text(
                text = removeSuccessMessage!!, // Usiamo !! perché sappiamo che non è nullo qui
                color = MaterialTheme.colorScheme.primary, // Colore per i successi
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth() // Riempi la larghezza per allineamento
            )
        }

        if (isPinSet) {
            Text("Change your access PIN:", fontWeight = FontWeight.Bold)
        } else {
            Text("Set an access PIN:", fontWeight = FontWeight.Bold)
        }


        // Campo di input per il nuovo PIN
        TextField(
            value = newPin,
            onValueChange = {
                newPin = it
                // Clear messages when user starts typing
                errorMessage = null
                removeSuccessMessage = null // Cancella messaggi di successo all'input
                saveSuccessMessage = null
            },
            label = { Text("New PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(), // Nasconde il testo
            modifier = Modifier.fillMaxWidth() // Riempie la larghezza
        )

        // Campo di input per confermare il PIN
        TextField(
            value = confirmPin,
            onValueChange = {
                confirmPin = it
                // Clear messages when user starts typing
                errorMessage = null
                removeSuccessMessage = null // Cancella messaggi di successo all'input
                saveSuccessMessage = null
            },
            label = { Text("Confirm New PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(), // Nasconde il testo
            modifier = Modifier.fillMaxWidth() // Riempie la larghezza
        )

        // Mostra messaggio di errore se presente
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Mostra messaggio di successo per il salvataggio se presente
        saveSuccessMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary, // Colore per i successi
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                // Aggiungi la validazione per la lunghezza minima del PIN
                if (newPin.isBlank() || confirmPin.isBlank()) {
                    errorMessage = "PIN cannot be empty"
                    saveSuccessMessage = null
                    removeSuccessMessage = null
                } else if (newPin.length < 4) {
                    errorMessage = "PIN must be at least 4 digits long"
                    saveSuccessMessage = null
                    removeSuccessMessage = null
                } else if (newPin != confirmPin) {
                    errorMessage = "PINs do not match"
                    saveSuccessMessage = null
                    removeSuccessMessage = null
                } else {
                    try {
                        val masterKey = MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()

                        val sharedPreferences = EncryptedSharedPreferences.create(
                            context,
                            "AppSettings", // File name for SharedPreferences
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )

                        with(sharedPreferences.edit()) {
                            putString("access_pin", newPin)
                            apply()
                        }

                        saveSuccessMessage = "PIN saved successfully"
                        errorMessage = null // Clear error message
                        removeSuccessMessage = null // Clear success message
                        newPin = ""
                        confirmPin = ""
                        // Aggiorna lo stato isPinSet per mostrare il pulsante di rimozione
                        isPinSet = true


                    } catch (e: Exception) {
                        Log.e("PinSettingsContent", "Error saving PIN", e)
                        errorMessage = "Error saving PIN: ${e.localizedMessage}"
                        saveSuccessMessage = null // Clear success message
                        removeSuccessMessage = null // Clear success message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth() // Riempie la larghezza
        ) {
            Text("Save PIN")
        }
    }
}


// Composbale per i dettagli delle impostazioni del tema
@Composable
fun ThemeSettingsContent(onThemeChange: (AppTheme) -> Unit) {
    val themePreferenceManager = rememberThemePreferenceManager()

    // Leggi lo stato del tema corrente per inizializzare la UI di questa Composable
    var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth() // Per centrare i pulsanti
    ) {
        Text("Choose the theme:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp) // Aggiungi padding
        ) {
            // Pulsante per il Tema Chiaro
            Button(
                onClick = {
                    // * NON salvare la preferenza direttamente qui. *
                    // La MainActivity salverà la preferenza quando riceve il callback.

                    onThemeChange(AppTheme.LIGHT) // *** CHIAMA IL CALLBACK! ***

                    // Aggiorna lo stato locale per l'UI di questa schermata (es. per disabilitare il pulsante)
                    currentTheme = AppTheme.LIGHT
                },
                enabled = currentTheme != AppTheme.LIGHT // Disabilita se è già selezionato
            ) {
                Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Light Mode")
                Spacer(Modifier.width(4.dp))
                Text("Light Theme")
            }

            // Pulsante per il Tema Scuro
            Button(
                onClick = {
                    // * NON salvare la preferenza direttamente qui. *
                    // La MainActivity salverà la preferenza quando riceve il callback.

                    onThemeChange(AppTheme.DARK) // *** CHIAMA IL CALLBACK! ***

                    // Aggiorna lo stato locale per l'UI di questa schermata
                    currentTheme = AppTheme.DARK
                },
                enabled = currentTheme != AppTheme.DARK // Disabilita se è già selezionato
            ) {
                Icon(imageVector = Icons.Default.NightsStay, contentDescription = "Dark Mode")
                Spacer(Modifier.width(4.dp))
                Text("Dark Theme")
            }
        }
    }
}

// Composbale per i dettagli delle informazioni sull'app
@Composable
fun AboutSettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Version: 1.0.0")
        Text("Developers: A. Catalano, A. Rocchi, O. Iacobelli")
        Text("Budgify is an app that deals with managing your finances.")
    }
}