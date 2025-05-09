package com.example.budgify.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgify.BottomBar
import com.example.budgify.TopBar
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.routes.ScreenRoutes

// Enum per rappresentare le opzioni delle impostazioni selezionate
enum class SettingsOptionType {
    NONE, PIN, THEME, ABOUT
}

@Composable
fun Settings(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Settings.route) }
    // Stato per tenere traccia dell'opzione selezionata
    var selectedOption by remember { mutableStateOf(SettingsOptionType.NONE) }

    Scaffold(
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
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
                        ThemeSettingsContent()
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

// Composbale per i dettagli delle impostazioni PIN
@Composable
fun PinSettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TODO: Implementare i campi di input e la logica per impostare il PIN
        Text("Type new PIN:")
        Text("Confirm new PIN")
        Button(onClick = { /* TODO: Salva il PIN */ }) {
            Text("Save PIN")
        }
    }
}

// Composbale per i dettagli delle impostazioni del tema
@Composable
fun ThemeSettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Choose the theme:", fontWeight = FontWeight.Bold)
        // TODO: Implementare le opzioni per scegliere il tema (radio buttons, toggles, etc.)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Light Mode")
            Text("Light theme")
            // Esempio di ToggleButton (richiede implementazione)
            // Switch(checked = isDarkMode, onCheckedChange = { /* TODO: Cambia tema */ })
            Icon(imageVector = Icons.Default.NightsStay, contentDescription = "Dark Mode")
            Text("Dark theme")
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
        // TODO: Ottieni la versione dell'app dinamicamente
        Text("Version: 1.0.0")
        Text("Developers: A. Catalano, A. Rocchi, O. Iacobelli")
        Text("Budgify is an app that deals with managing your finances.")
    }
}