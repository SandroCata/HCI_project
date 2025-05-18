package com.example.budgify.screen

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


// --- Security Question Data ---
data class SecurityQuestionAnswer(val questionIndex: Int, val answer: String)

val securityQuestions = listOf(
    "What was the name of your first pet?",
    "What is your mother's maiden name?",
    "What was the name of your elementary school?",
    "In what city were you born?",
    "What is your favorite book?"
)

// --- Helper functions for SharedPreferences ---

// Funzione helper per recuperare la domanda e risposta di sicurezza salvate
fun getSavedSecurityQuestionAnswer(context: android.content.Context): SecurityQuestionAnswer? {
    try {
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
        val questionIndex = sharedPreferences.getInt("security_question_index", -1)
        val answer = sharedPreferences.getString("security_answer", null)

        return if (questionIndex != -1 && answer != null) {
            SecurityQuestionAnswer(questionIndex, answer)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("Settings", "Error retrieving saved security question/answer", e)
        return null
    }
}

// Funzione helper per salvare la domanda e risposta di sicurezza
fun saveSecurityQuestionAnswer(context: android.content.Context, questionIndex: Int, answer: String): Boolean {
    try {
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
        with(sharedPreferences.edit()) {
            putInt("security_question_index", questionIndex)
            putString("security_answer", answer) // Store the answer directly (it will be encrypted by EncryptedSharedPreferences)
            apply()
        }
        return true
    } catch (e: Exception) {
        Log.e("Settings", "Error saving security question/answer", e)
        return false
    }
}


enum class SettingsOptionType {
    NONE, PIN, THEME, ABOUT
}

@Composable
fun Settings(navController: NavController, viewModel: FinanceViewModel, onThemeChange: (AppTheme) -> Unit) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Settings.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf(SettingsOptionType.NONE) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = {
            BottomBar(
                navController,
                viewModel,
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                SettingsOption(
                    icon = Icons.Default.Lock,
                    title = "Access PIN & Security Question", // MODIFIED
                    onClick = { selectedOption = SettingsOptionType.PIN }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsOption(
                    icon = Icons.Default.NightsStay,
                    title = "Dark/Light Mode",
                    onClick = { selectedOption = SettingsOptionType.THEME }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsOption(
                    icon = Icons.Default.Info,
                    title = "About the app",
                    onClick = { selectedOption = SettingsOptionType.ABOUT }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedOption) {
                    SettingsOptionType.NONE -> {
                        Text(
                            "Select an option for more details",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    SettingsOptionType.PIN -> {
                        PinSettingsContent(snackbarHostState = snackbarHostState)
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
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Normal)
    }
    Divider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSettingsContent(snackbarHostState: SnackbarHostState) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState() // Added for scrolling

    var isPinSet by remember { mutableStateOf(getSavedPinFromContext(context) != null) }
    var savedSecurityQA by remember { mutableStateOf(getSavedSecurityQuestionAnswer(context)) }

    // State for security question UI
    var selectedQuestionIndex by remember { mutableStateOf(savedSecurityQA?.questionIndex ?: 0) }
    var securityAnswerInput by remember { mutableStateOf(savedSecurityQA?.answer ?: "") }
    var securityQuestionDropdownExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) { // Load saved security question and answer
        val loadedQA = getSavedSecurityQuestionAnswer(context)
        if (loadedQA != null) {
            selectedQuestionIndex = loadedQA.questionIndex
            securityAnswerInput = loadedQA.answer
        } else {
            // Default to the first question if nothing is saved
            selectedQuestionIndex = 0
            securityAnswerInput = ""
        }
    }

    // Determine if the security question/answer has been set at least once
    val isSecurityQASet = remember { mutableStateOf(savedSecurityQA != null) }
    // If the saved Q&A changes, update isSecurityQASet
    LaunchedEffect(savedSecurityQA) {
        isSecurityQASet.value = savedSecurityQA != null
        if (savedSecurityQA != null && securityAnswerInput.isEmpty()) {
            // Pre-fill if it was emptied and then saved (though less likely with this setup)
            securityAnswerInput = savedSecurityQA!!.answer
            selectedQuestionIndex = savedSecurityQA!!.questionIndex
        }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            when {
                isPinSet && isSecurityQASet.value -> "Manage PIN & Security Question"
                isPinSet -> "Set Security Question & Manage PIN"
                isSecurityQASet.value -> "Set PIN & Manage Security Question"
                else -> "Set PIN & Security Question"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // --- PIN Section ---
        if (isPinSet) {
            Text("Change Access PIN", style = MaterialTheme.typography.titleMedium)
        } else {
            Text("Set New Access PIN", style = MaterialTheme.typography.titleMedium)
        }

        TextField(
            value = newPin,
            onValueChange = {
                newPin = it.filter { char -> char.isDigit() }
                errorMessage = null
            },
            label = { Text("New PIN (min 4 digits)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "New PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("PIN", ignoreCase = true) == true // Make check case-insensitive
        )

        TextField(
            value = confirmPin,
            onValueChange = {
                confirmPin = it.filter { char -> char.isDigit() }
                errorMessage = null
            },
            label = { Text(if (isPinSet) "Confirm New PIN (if changing)" else "Confirm New PIN") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("PINs do not match", ignoreCase = true) == true
        )

        // --- Security Question Section ---
        Text("Security Question for Temporary Access", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = securityQuestionDropdownExpanded,
            onExpandedChange = { securityQuestionDropdownExpanded = !securityQuestionDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = securityQuestions[selectedQuestionIndex],
                onValueChange = {}, // Not editable directly
                readOnly = true,
                label = { Text("Select Security Question") },
                leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "Security Question") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityQuestionDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = securityQuestionDropdownExpanded,
                onDismissRequest = { securityQuestionDropdownExpanded = false }
            ) {
                securityQuestions.forEachIndexed { index, question ->
                    DropdownMenuItem(
                        text = { Text(question) },
                        onClick = {
                            selectedQuestionIndex = index
                            securityQuestionDropdownExpanded = false
                            errorMessage = null
                        }
                    )
                }
            }
        }

        TextField(
            value = securityAnswerInput,
            onValueChange = {
                securityAnswerInput = it
                errorMessage = null
            },
            label = { Text("Your Answer") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Security Answer") }, // Using Lock icon for answer too
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // Or false if answers can be long
            isError = errorMessage?.contains("answer", ignoreCase = true) == true
        )


        // --- Error Message ---
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // --- Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            if (isPinSet) {
                Button(
                    onClick = {
                        try {
                            val masterKey = MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
                            val sharedPreferences = EncryptedSharedPreferences.create(
                                context, "AppSettings", masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            )
                            with(sharedPreferences.edit()) {
                                remove("access_pin")
                                apply()
                            }
                            scope.launch { snackbarHostState.showSnackbar("PIN removed successfully.") }
                            errorMessage = null
                            newPin = ""
                            confirmPin = ""
                            isPinSet = false
                        } catch (e: Exception) {
                            Log.e("PinSettingsContent", "Error removing PIN", e)
                            errorMessage = "Error removing PIN."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Remove PIN")
                }
            }

            Button(
                onClick = {
                    errorMessage = null // Reset error
                    var changesMade = false
                    var showSuccessMessage = "Settings updated."

                    // 1. Validate Security Answer (Mandatory if not yet set, or if changed)
                    if (securityAnswerInput.isBlank()) {
                        errorMessage = "Security answer cannot be empty."
                        return@Button
                    }
                    // Add any other answer validation if needed (e.g., min length)

                    // 2. Validate PIN (if trying to set/change PIN)
                    val isTryingToSetOrChangePin = newPin.isNotEmpty() || confirmPin.isNotEmpty()
                    if (isTryingToSetOrChangePin) {
                        if (newPin.length < 4) {
                            errorMessage = "New PIN must be at least 4 digits long."
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            errorMessage = "PINs do not match."
                            return@Button
                        }
                    }

                    // 3. Save Logic
                    var pinSavedSuccessfully = true
                    var qaSavedSuccessfully = true

                    // Save PIN if provided and valid
                    if (isTryingToSetOrChangePin && newPin.isNotBlank()) {
                        try {
                            val masterKey = MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
                            val sharedPreferences = EncryptedSharedPreferences.create(
                                context, "AppSettings", masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            )
                            with(sharedPreferences.edit()) {
                                putString("access_pin", newPin)
                                apply()
                            }
                            changesMade = true
                            isPinSet = true // Update UI state
                        } catch (e: Exception) {
                            Log.e("PinSettingsContent", "Error saving PIN", e)
                            errorMessage = "Error saving PIN."
                            pinSavedSuccessfully = false
                        }
                    }

                    // Save Security Question & Answer if changed or first time
                    val currentSavedQA = getSavedSecurityQuestionAnswer(context)
                    val qaChanged = currentSavedQA?.questionIndex != selectedQuestionIndex ||
                            currentSavedQA?.answer != securityAnswerInput
                    val isFirstTimeSettingQA = currentSavedQA == null

                    if (qaChanged || isFirstTimeSettingQA) {
                        if (saveSecurityQuestionAnswer(context, selectedQuestionIndex, securityAnswerInput)) {
                            savedSecurityQA = SecurityQuestionAnswer(selectedQuestionIndex, securityAnswerInput) // Update UI state
                            isSecurityQASet.value = true
                            changesMade = true
                        } else {
                            errorMessage = (errorMessage ?: "") + " Error saving security question."
                            qaSavedSuccessfully = false
                        }
                    }

                    // Determine success message
                    if (changesMade) {
                        when {
                            isTryingToSetOrChangePin && newPin.isNotBlank() && (qaChanged || isFirstTimeSettingQA) ->
                                showSuccessMessage = if (pinSavedSuccessfully && qaSavedSuccessfully) "PIN and security question updated." else "Partial update. Check errors."
                            isTryingToSetOrChangePin && newPin.isNotBlank() ->
                                showSuccessMessage = if (pinSavedSuccessfully) "PIN updated." else "Error saving PIN."
                            qaChanged || isFirstTimeSettingQA ->
                                showSuccessMessage = if (qaSavedSuccessfully) "Security question updated." else "Error saving security question."
                        }
                        scope.launch { snackbarHostState.showSnackbar(showSuccessMessage) }
                        if (isTryingToSetOrChangePin && pinSavedSuccessfully) {
                            newPin = "" // Reset PIN fields after successful save
                            confirmPin = ""
                        }
                        // Keep security answer input as is, user might want to see it
                    } else if (errorMessage == null) { // No changes and no errors from previous steps
                        scope.launch { snackbarHostState.showSnackbar("No changes were made.") }
                    }

                    if (!pinSavedSuccessfully || !qaSavedSuccessfully) {
                        // Error message is already set by individual save attempts
                    }

                },
                enabled = securityAnswerInput != (savedSecurityQA?.answer ?: "") ||
                        selectedQuestionIndex != (savedSecurityQA?.questionIndex ?: 0) ||
                        newPin.isNotEmpty() || confirmPin.isNotEmpty() ||
                        (!isSecurityQASet.value && securityAnswerInput.isNotBlank())
            ) {
                Text("Save")
            }
        }
    }
}


@Composable
fun ThemeSettingsContent(onThemeChange: (AppTheme) -> Unit) {
    val themePreferenceManager = rememberThemePreferenceManager()
    var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Choose App Theme", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    onThemeChange(AppTheme.LIGHT)
                    currentTheme = AppTheme.LIGHT
                },
                enabled = currentTheme != AppTheme.LIGHT,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Light Theme")
                Spacer(Modifier.width(4.dp))
                Text("Light")
            }

            Button(
                onClick = {
                    onThemeChange(AppTheme.DARK)
                    currentTheme = AppTheme.DARK
                },
                enabled = currentTheme != AppTheme.DARK,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.NightsStay, contentDescription = "Dark Theme")
                Spacer(Modifier.width(4.dp))
                Text("Dark")
            }
        }
    }
}

@Composable
fun AboutSettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Budgify", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Version: 1.0.0", style = MaterialTheme.typography.bodyLarge)
        Text("Developers: A. Catalano, A. Rocchi, O. Iacobelli", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Your personal finance manager.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}