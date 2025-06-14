package com.example.budgify.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.navigation.getSavedPinFromContext
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import com.example.budgify.userpreferences.rememberThemePreferenceManager
import kotlinx.coroutines.launch

const val DEV = false

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
    NONE, PIN, THEME, ABOUT, DEV_RESET
}

@Composable
fun Settings(
    navController: NavController,
    viewModel: FinanceViewModel,
    onThemeChange: (AppTheme) -> Unit
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Settings.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf(SettingsOptionType.NONE) }
    val context = LocalContext.current
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                    title = "Theme",
                    onClick = { selectedOption = SettingsOptionType.THEME }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsOption(
                    icon = Icons.Default.Info,
                    title = "About the app",
                    onClick = { selectedOption = SettingsOptionType.ABOUT }
                )
                Spacer(modifier = Modifier.height(8.dp)) // Spacer before the new option
                // --- ADDED DEV RESET AS A MAIN OPTION ---
                if (DEV) {
                    SettingsOption(
                        icon = Icons.Filled.DeleteForever, // Choose an appropriate icon
                        title = "DEV: Reset Level & Unlocks",
                        onClick = {
                            // Instead of changing the main view, trigger the confirmation dialog
                            // selectedOption = SettingsOptionType.DEV_RESET; // Optional: if you want to highlight it
                            showResetConfirmationDialog = true
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedOption) {
                    SettingsOptionType.NONE -> {
                        Text(
                            "Select an option for more details",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    SettingsOptionType.PIN -> {
                        PinSettingsContent(snackbarHostState = snackbarHostState)
                    }
                    SettingsOptionType.THEME -> {
                        ThemeSettingsContent(
                            viewModel = viewModel, // Pass the viewModel
                            onThemeChange = { newTheme ->
                                val themePreferenceManager = ThemePreferenceManager(context) // Recreate or get instance
                                themePreferenceManager.saveTheme(newTheme)
                                onThemeChange(newTheme)
                            }
                        )
                    }
                    SettingsOptionType.ABOUT -> {
                        AboutSettingsContent()
                    }
                    SettingsOptionType.DEV_RESET -> {}
                }
            }
        }
        if (showResetConfirmationDialog) {
            ResetConfirmationDialog(
                onConfirm = {
                    scope.launch {
                        viewModel.resetUserProgressForTesting()
                        snackbarHostState.showSnackbar("User level, XP, and themes reset!")
                    }
                    showResetConfirmationDialog = false
                    selectedOption = SettingsOptionType.NONE // Reset selection after action
                },
                onDismiss = {
                    showResetConfirmationDialog = false
                    selectedOption = SettingsOptionType.NONE // Reset selection if cancelled
                }
            )
        }
    }
}

@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Reset") },
        text = { Text("Are you sure you want to reset all user level progress, XP, and unlocked themes? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

    // State for password visibility
    var newPinVisible by remember { mutableStateOf(false) }
    var confirmPinVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isPinSet by remember { mutableStateOf(getSavedPinFromContext(context) != null) }
    var savedSecurityQA by remember { mutableStateOf(getSavedSecurityQuestionAnswer(context)) }

    var selectedQuestionIndex by remember { mutableStateOf(savedSecurityQA?.questionIndex ?: 0) }
    var securityAnswerInput by remember { mutableStateOf(savedSecurityQA?.answer ?: "") }
    var securityQuestionDropdownExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val loadedQA = getSavedSecurityQuestionAnswer(context)
        if (loadedQA != null) {
            selectedQuestionIndex = loadedQA.questionIndex
            securityAnswerInput = loadedQA.answer
        } else {
            selectedQuestionIndex = 0
            securityAnswerInput = ""
        }
    }

    val isSecurityQASet = remember { mutableStateOf(savedSecurityQA != null) }
    LaunchedEffect(savedSecurityQA) {
        isSecurityQASet.value = savedSecurityQA != null
        if (savedSecurityQA != null && securityAnswerInput.isEmpty()) {
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
            // ... title text ...
            when {
                isPinSet && isSecurityQASet.value -> "Manage PIN & Security Question"
                isPinSet -> "Set Security Question & Manage PIN"
                isSecurityQASet.value -> "Set PIN & Manage Security Question"
                else -> "Set PIN & Security Question"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

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
            trailingIcon = { // Add trailing icon for visibility toggle
                val image = if (newPinVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (newPinVisible) "Hide PIN" else "Show PIN"
                IconButton(onClick = { newPinVisible = !newPinVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (newPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("PIN", ignoreCase = true) == true
        )

        TextField(
            value = confirmPin,
            onValueChange = {
                confirmPin = it.filter { char -> char.isDigit() }
                errorMessage = null
            },
            label = { Text(if (isPinSet) "Confirm New PIN (if changing)" else "Confirm New PIN") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm PIN") },
            trailingIcon = { // Add trailing icon for visibility toggle
                val image = if (confirmPinVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (confirmPinVisible) "Hide PIN" else "Show PIN"
                IconButton(onClick = { confirmPinVisible = !confirmPinVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            visualTransformation = if (confirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("PINs do not match", ignoreCase = true) == true
        )

        // --- Security Question Section ---
        Text("Security Question for Temporary Access", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            // ... same as before ...
            expanded = securityQuestionDropdownExpanded,
            onExpandedChange = { securityQuestionDropdownExpanded = !securityQuestionDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                // ... same as before ...
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
                // ... same as before ...
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
            // ... same as before ...
            value = securityAnswerInput,
            onValueChange = {
                securityAnswerInput = it
                errorMessage = null
            },
            label = { Text("Your Answer") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Security Answer") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage?.contains("answer", ignoreCase = true) == true
        )

        errorMessage?.let {
            // ... same as before ...
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Row(
            // ... Button row ...
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            if (isPinSet) {
                Button(
                    // ... Remove PIN button ...
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
                // ... Save button ...
                onClick = {
                    errorMessage = null // Reset error
                    var changesMade = false
                    var showSuccessMessage = "Settings updated."

                    // 1. Validate Security Answer (Mandatory if not yet set, or if changed)
                    if (securityAnswerInput.isBlank()) {
                        errorMessage = "Security answer cannot be empty."
                        return@Button
                    }

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
                            isPinSet = true
                        } catch (e: Exception) {
                            Log.e("PinSettingsContent", "Error saving PIN", e)
                            errorMessage = "Error saving PIN."
                            pinSavedSuccessfully = false
                        }
                    }

                    val currentSavedQA = getSavedSecurityQuestionAnswer(context)
                    val qaChanged = currentSavedQA?.questionIndex != selectedQuestionIndex ||
                            currentSavedQA?.answer != securityAnswerInput
                    val isFirstTimeSettingQA = currentSavedQA == null

                    if (qaChanged || isFirstTimeSettingQA) {
                        if (saveSecurityQuestionAnswer(context, selectedQuestionIndex, securityAnswerInput)) {
                            savedSecurityQA = SecurityQuestionAnswer(selectedQuestionIndex, securityAnswerInput)
                            isSecurityQASet.value = true
                            changesMade = true
                        } else {
                            errorMessage = (errorMessage ?: "") + " Error saving security question."
                            qaSavedSuccessfully = false
                        }
                    }

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
                            newPin = ""
                            confirmPin = ""
                        }
                    } else if (errorMessage == null) {
                        scope.launch { snackbarHostState.showSnackbar("No changes were made.") }
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
fun ThemeSettingsContent(
    viewModel: FinanceViewModel,
    onThemeChange: (AppTheme) -> Unit
) {
    val themePreferenceManager = rememberThemePreferenceManager()
    var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }
    val unlockedThemeNames by viewModel.unlockedThemeNames.collectAsStateWithLifecycle()
    val availableThemes = remember(unlockedThemeNames) {
        AppTheme.entries.filter { themeEnum ->
            unlockedThemeNames.contains(themeEnum.name) // Filter by name
        }.sortedBy { it.unlockLevel } // Optional: sort them by unlock level or name
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Choose App Theme",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Display all available themes (unlocked ones)
        if (AppTheme.entries.isEmpty()) { // Simplified check
            Text(
                "No themes defined in the app.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // You can use a LazyColumn or Column depending on how many themes you expect
            Column() {
                val allThemesSorted = remember { AppTheme.entries.sortedBy { it.unlockLevel } }
                allThemesSorted.forEach { theme -> // Iterate through ALL themes to show locked status
                    val isUnlocked = unlockedThemeNames.contains(theme.name)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isUnlocked) { // Only clickable if unlocked
                                if (isUnlocked) {
                                    themePreferenceManager.saveTheme(theme) // Save the theme preference
                                    onThemeChange(theme) // Trigger the actual theme change
                                    currentTheme =
                                        theme // Update local state for UI feedback (e.g., RadioButton)
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentTheme == theme),
                            onClick = if (isUnlocked) {
                                {
                                    themePreferenceManager.saveTheme(theme)
                                    onThemeChange(theme)
                                    currentTheme = theme
                                }
                            } else null, // Disable RadioButton click if not unlocked
                            enabled = isUnlocked // Visually disable RadioButton if not unlocked
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = theme.displayName,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        )
                        //Spacer(Modifier.weight(1f)) // Push lock icon/text to the end
                        if (!isUnlocked) {
                            Text(
                                text = "(Unlocks at Level ${theme.unlockLevel})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            // Optionally, add a Lock Icon
                            // Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Divider() // Optional: add a divider between theme options
                }
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