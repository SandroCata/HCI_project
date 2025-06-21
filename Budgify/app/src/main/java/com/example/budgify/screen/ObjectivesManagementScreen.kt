package com.example.budgify.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ObjectiveSectionType {
    ACTIVE,
    EXPIRED
}

@Composable
fun ObjectivesManagementScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.ObjectivesManagement.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(ObjectivesManagementSection.Active) }
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val listState = rememberLazyListState()
    val showButton by remember {
        derivedStateOf {
            // Mostra il pulsante se non stiamo scrollando o se siamo in cima/fondo con pochi elementi
            if (listState.layoutInfo.totalItemsCount <= listState.layoutInfo.visibleItemsInfo.size) {
                true // Sempre visibile se tutti gli elementi sono visibili
            } else {
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtBottom = lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
                isAtTop || isAtBottom || !listState.isScrollInProgress
            }
        }
    }

    LaunchedEffect(key1 = Unit) { // Use Unit as key if viewModel.snackbarMessages is stable
        viewModel.snackbarMessages.collectLatest { message ->
            Log.d("ObjectivesScreen_Snackbar", "Collected global message from VM: $message")
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short // Or .Long, or make it configurable
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = {
            BottomBar(
                navController,
                viewModel,
                showSnackbar = showSnackbar
            )
        },
        containerColor = Color.Transparent // O il colore di sfondo desiderato per lo Scaffold
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TabRow(selectedTabIndex = selectedSection.ordinal) {
                    ObjectivesManagementSection.entries.forEach { section ->
                        Tab(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            text = { Text(section.title) }
                        )
                    }
                }

                val explanatoryText = when (selectedSection) {
                    ObjectivesManagementSection.Active -> "Here you can find all currently active goals.\nTry to complete them before they expire!"
                    ObjectivesManagementSection.Expired -> "Here you can find all reached and/or expired goals.\nYou can still reach expired goals."
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = explanatoryText,
                            style = MaterialTheme.typography.bodyMedium, // Puoi scegliere lo stile che preferisci
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp), // Aggiungi padding per spaziatura
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Text(
                    text = "Hold on a goal to manage it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                    // Rimuovi padding orizzontale qui se lo applichi in ObjectivesSection
                ) {
                    when (selectedSection) {
                        ObjectivesManagementSection.Active -> {
                            ObjectivesSection(ObjectiveSectionType.ACTIVE, listState, viewModel, showSnackbar)
                        }

                        ObjectivesManagementSection.Expired -> {
                            ObjectivesSection(ObjectiveSectionType.EXPIRED, listState, viewModel, showSnackbar)
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showButton, // La visibilità è ora gestita dal LazyListState
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        navController.navigate(ScreenRoutes.Objectives.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Fallo largo quanto il contenitore per centrarlo meglio con padding
                        .padding(horizontal = 16.dp) // Padding orizzontale
                        .padding(bottom = 16.dp) // Padding dal fondo
                ) {
                    Text("Back to Stats Overview")
                }
            }
        }
    }
}

enum class ObjectivesManagementSection(val title: String) {
    Active("Active Goals"),
    Expired("Inactive Goals")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObjectiveItem(
    obj: Objective,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    var showActionChoiceDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showAccountSelectionForCompletionDialog by remember { mutableStateOf(false) }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }
    var insufficientBalanceAccountInfo by remember { mutableStateOf<Pair<String, Double>?>(null) }

    // Calcola se è scaduto e non completato
    val isExpiredNotCompleted = remember(obj) {
        obj.endDate.isBefore(LocalDate.now()) && !obj.completed
    }

    val backgroundColor = when {
        isExpiredNotCompleted -> Color.Gray.copy(alpha = 0.7f) // Grigio per scaduti non completati
        obj.type == ObjectiveType.INCOME -> Color(0xFF4CAF50).copy(alpha = if (obj.completed) 0.5f else 0.8f)
        obj.type == ObjectiveType.EXPENSE -> Color(0xFFF44336).copy(alpha = if (obj.completed) 0.5f else 0.8f)
        else -> MaterialTheme.colorScheme.surfaceVariant // Fallback
    }

    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (obj.completed || isExpiredNotCompleted) 0.7f else 1f)

    val iconImage = when {
        obj.completed -> Icons.Filled.CheckCircleOutline
        isExpiredNotCompleted -> Icons.Filled.TimerOff // Icona per scaduto
        else -> Icons.Filled.EmojiEvents // Icona di default per obiettivi attivi
    }

    val statusTextUnderIcon = when {
        obj.completed -> "Reached"
        isExpiredNotCompleted -> "Expired"
        else -> "Active" // Nessun testo se attivo e non scaduto
    }

    val formattedAmount = String.format(java.util.Locale.US, "%.2f", obj.amount)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    if (obj.completed) {
                        showSnackbar("This goal has already been reached.")
                    } else if (isExpiredNotCompleted) {
                        showSnackbar("This goal has expired. Hold to interact.")
                    } else {
                        showSnackbar("Hold to edit or delete the goal.")
                    }
                },
                onLongClick = {
                    showActionChoiceDialog = true
                }
            )
            .padding(vertical = 8.dp, horizontal = 16.dp) // Adattato padding
    ) {
        // Colonna principale per le informazioni dell'obiettivo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 88.dp) // Spazio per icona e testo di stato a destra
        ) {
            Text(
                text = obj.desc,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Amount: $formattedAmount €",
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Start Date: ${obj.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = contentColor,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Due Date: ${obj.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = contentColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isExpiredNotCompleted) MaterialTheme.colorScheme.error else contentColor
                )
            )
        }

        // Colonna per Icona e Testo di Stato allineata a destra
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = iconImage,
                contentDescription = statusTextUnderIcon ?: "Objective Status",
                modifier = Modifier
                    .size(if (statusTextUnderIcon != null) 60.dp else 80.dp) // Icona più piccola se c'è testo
                    .alpha(if (statusTextUnderIcon != null) 0.7f else 0.5f),
                tint = contentColor.copy(alpha = 0.8f)
            )
            statusTextUnderIcon?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isExpiredNotCompleted) MaterialTheme.colorScheme.error.copy(alpha = contentColor.alpha) else contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Dialogo di Scelta Azione
    if (showActionChoiceDialog) {
        ObjectiveActionChoiceDialog(
            objective = obj,
            onDismiss = { showActionChoiceDialog = false },
            onEditClick = {
                showEditDialog = true
                showActionChoiceDialog = false
            },
            onDeleteClick = {
                Log.d("XP_DEBUG", "onCompleteClick in ObjectiveItem. Setting showAccountSelectionForCompletionDialog = true.")
                showDeleteConfirmationDialog = true
                showActionChoiceDialog = false
            },
            onCompleteClick = {
                showAccountSelectionForCompletionDialog = true
                // selectedAccountIdForCompletion = null // Reset if re-opening
                showActionChoiceDialog = false
            }
        )
    }

    if (showAccountSelectionForCompletionDialog) {
        val accounts by viewModel.allAccounts.collectAsStateWithLifecycle() // Already have allAccounts
        val hasAccounts by viewModel.hasAccounts.collectAsStateWithLifecycle(initialValue = false) // Use your hasAccounts Flow
        //val scope = rememberCoroutineScope()

        if (!hasAccounts && accounts.isEmpty()) { // Check based on your hasAccounts or if list is empty after initial load
            AlertDialog(
                onDismissRequest = { showAccountSelectionForCompletionDialog = false },
                title = { Text("No Accounts Found") },
                text = { Text("You need to create an account first before being able to reach a goal and create a transaction.") },
                confirmButton = {
                    TextButton(onClick = { showAccountSelectionForCompletionDialog = false }) {
                        Text("OK")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showAccountSelectionForCompletionDialog = false },
                title = { Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Account",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    XButton(onDismiss = { showAccountSelectionForCompletionDialog = false })
                } },
                text = {
                    if (accounts.isEmpty()) {
                        Text("Loading accounts...")
                    } else {
                        Column {
                            Text(
                                "Choose an account to create a transaction for this goal",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(accounts, key = { it.id }) { account ->
                                    ListItem(
                                        headlineContent = { Text(account.title) },
                                        supportingContent = { Text("Balance: ${account.amount} €") },
                                        modifier = Modifier.clickable {
                                            Log.d("XP_DEBUG", "Account selected: ${account.title} for goal: ${obj.desc}. Calling completeObjectiveAndCreateTransaction.")
                                            if (obj.type == ObjectiveType.EXPENSE && obj.amount > account.amount) {
                                                insufficientBalanceAccountInfo = Pair(account.title, account.amount)
                                                showInsufficientBalanceDialog = true
                                                showAccountSelectionForCompletionDialog = false
                                            } else {
                                                viewModel.completeObjectiveAndCreateTransaction(
                                                    objective = obj,
                                                    accountId = account.id,
                                                    // categoryId = null
                                                )
                                                // You can still show a generic local snackbar if desired:
                                                showSnackbar("Goal '${obj.desc}' reached. Transaction created for '${account.title}'.")
                                                showAccountSelectionForCompletionDialog = false
                                            }
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        )
                                    )
                                    // Divider() // ListItem often looks good without explicit dividers, but you can add if preferred
                                }
                            }
                        }
                    }
                },
                dismissButton = null,
                confirmButton = {} // Confirm button is handled by item clicks
            )
        }
    }

    if (showInsufficientBalanceDialog && insufficientBalanceAccountInfo != null) {
        val (accTitle, accBalance) = insufficientBalanceAccountInfo!!
        AlertDialog(
            onDismissRequest = {
                showInsufficientBalanceDialog = false
                insufficientBalanceAccountInfo = null
            },
            title = { Text("Insufficient Balance") },
            text = {
                Text(
                    "Account '${accTitle}' has insufficient balance.\n\n" +
                            "Required: ${obj.amount} €\n" +
                            "Available: $accBalance €\n\n" +
                            "Please select another account or add funds."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showInsufficientBalanceDialog = false
                    insufficientBalanceAccountInfo = null
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Dialogo di Modifica Obiettivo
    if (showEditDialog) {
        EditObjectiveDialog(
            objective = obj,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            // onDeleteClick in EditObjectiveDialog ora chiama il dialogo di conferma
            onDeleteRequest = {
                showEditDialog = false // Chiudi il dialogo di modifica
                showDeleteConfirmationDialog = true // Mostra il dialogo di conferma eliminazione
            }
        )
    }

    // Dialogo di Conferma Eliminazione
    if (showDeleteConfirmationDialog) {
        DeleteObjectiveConfirmationDialog( // Rinominato per coerenza
            objective = obj,
            onDismiss = { showDeleteConfirmationDialog = false },
            onConfirmDelete = {
                viewModel.deleteObjective(obj) // Logica di eliminazione
                showDeleteConfirmationDialog = false
                showSnackbar("Goal '${obj.desc}' deleted")
            }
        )
    }
}

@Composable
fun ObjectiveActionChoiceDialog(
    objective: Objective,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit
) {

    val hasExpired = objective.endDate.isBefore(LocalDate.now())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "'${objective.desc}'",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XButton(onDismiss)
        } },
        text = { Text("What would you like to do with this goal?")
        },
        confirmButton = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!objective.completed && !hasExpired)
                        TextButton(onClick = onEditClick) {
                            Text("Edit")
                        }
                    TextButton(onClick = onDeleteClick) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                // "Complete" button: Only if not completed and not expired
                if (!objective.completed) {
                    TextButton(
                        onClick = {
                            Log.d("XP_DEBUG", "Mark as Reached clicked in ActionChoiceDialog for: ${objective.desc}")
                            onCompleteClick()
                            // onDismiss() // Dismiss after action is initiated
                        }, // onDismiss is handled by the actions themselves opening new dialogs or by XButton
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Reached")
                    }
                }
            }
        },
        dismissButton = null
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditObjectiveDialog(
    objective: Objective,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onDeleteRequest: () -> Unit // Modificato da onDeleteClick a onDeleteRequest
) {
    var description by remember { mutableStateOf(objective.desc) }
    var descriptionError by remember { mutableStateOf<String?>(null) } // Nuovo per errore descrizione
    var amount by remember { mutableStateOf(objective.amount.toString().replace('.',',')) }
    var amountError by remember { mutableStateOf<String?>(null) } // Nuovo per errore amount
    var selectedStartDate by remember { mutableStateOf(objective.startDate) }
    var selectedEndDate by remember { mutableStateOf(objective.endDate) }
    var selectedType by remember { mutableStateOf(objective.type) }
    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
    val objectiveTypes = ObjectiveType.entries.toList()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var dateErrorMessage by remember { mutableStateOf<String?>(null) } // Per errore date


    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Goal", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { newValue ->
                    var cleanedValue = newValue.replace("\n", "").replace("\t", "") // Pulisci
                    cleanedValue = cleanedValue.replace(Regex("\\s+"), " ") // Rimuovi spazi extra
                    if (cleanedValue.length <= 50) { // Limite di esempio, puoi cambiarlo
                        description = cleanedValue
                        descriptionError = null
                    } else {
                        description = cleanedValue.take(50)
                        descriptionError = "Max 30 characters."
                    }
                },
                label = { Text("Description (max 30)") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionError != null || (description.isBlank() && (descriptionError !=null || amountError != null || dateErrorMessage != null) /* Mostra errore solo se c'è un errore generale */),
                /*supportingText = {
                    if (descriptionError != null) {
                        Text(descriptionError!!, color = MaterialTheme.colorScheme.error)
                    }
                }*/
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = amount,
                onValueChange = { newValue ->
                    amount = newValue
                    // Validazione durante la digitazione per l'importo
                    val amountToCheck = newValue.replace(',', '.')
                    if (amountToCheck.isNotBlank() && (amountToCheck.toDoubleOrNull() == null || amountToCheck.toDoubleOrNull()!! <= 0)) {
                        amountError = "Invalid positive amount."
                    } else {
                        amountError = null
                    }
                },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = amountError != null,
                /*supportingText = {
                    if (amountError != null) {
                        Text(amountError!!, color = MaterialTheme.colorScheme.error)
                    }
                }*/
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = selectedStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Start Date",
                        modifier = Modifier.clickable { showStartDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && selectedStartDate.isAfter(selectedEndDate) // Aggiunto controllo data
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = selectedEndDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("End Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select End Date",
                        modifier = Modifier.clickable { showEndDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && selectedEndDate.isBefore(selectedStartDate) // Aggiunto controllo data
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    objectiveTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type })
                            Text(type.name.replaceFirstChar { it.titlecase() })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        // Resetta tutti gli errori specifici
                        descriptionError = null
                        amountError = null
                        dateErrorMessage = null

                        val finalDescription = description.trim()
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()

                        var isValid = true

                        if (finalDescription.isBlank()) {
                            descriptionError = "Description cannot be empty."
                            isValid = false
                        } else if (finalDescription.length > 50) { // Ricontrolla al submit
                            descriptionError = "Max 30 characters."
                            isValid = false
                        }

                        if (amountDouble == null || amountDouble <= 0) {
                            amountError = "Please enter a valid positive amount."
                            isValid = false
                        }
                        if (selectedStartDate.isAfter(selectedEndDate)) {
                            dateErrorMessage = "Start date cannot be after end date."
                            isValid = false
                        }
                        // Aggiunta opzionale: controllo se la data di fine è nel passato
                        // if (selectedEndDate.isBefore(LocalDate.now())) {
                        //     dateErrorMessage = (dateErrorMessage ?: "") + "\nEnd date cannot be in the past."
                        //     isValid = false
                        // }


                        if (!isValid) {
                            return@Button
                        }

                        val updatedObjective = objective.copy(
                            desc = finalDescription,
                            amount = amountDouble!!, // Sicuro per il controllo precedente
                            startDate = selectedStartDate,
                            endDate = selectedEndDate,
                            type = selectedType
                        )
                        coroutineScope.launch {
                            viewModel.updateObjective(updatedObjective)
                        }
                        onDismiss()
                    }) {
                    Text("Save changes")
                }
            }
        }
    }

    // DatePickerDialog per la data di inizio
    if (showStartDatePickerDialog) {
        val initialStartDateMillis = remember(objective.startDate) {
            objective.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialStartDateMillis)
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showStartDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedStartDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePickerDialog = false
                    },
                    enabled = confirmEnabled
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // DatePickerDialog per la data di fine
    if (showEndDatePickerDialog) {
        val initialEndDateMillis = remember(objective.endDate) {
            objective.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEndDateMillis)
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showEndDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedEndDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePickerDialog = false
                    },
                    enabled = confirmEnabled
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun DeleteObjectiveConfirmationDialog( // Rinominato per coerenza
    objective: Objective,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit // Aggiunto callback per conferma
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the goal \"${objective.desc}\"?") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmDelete() // Chiama il callback fornito
                    // La logica di eliminazione e lo snackbar sono ora gestiti dal chiamante (ObjectiveItem)
                }
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ObjectivesSection(
    type: ObjectiveSectionType,
    listState: LazyListState,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    val objectives by viewModel.allObjectives.collectAsStateWithLifecycle()
    val filteredObjectives = remember(objectives, type) {
        val now = LocalDate.now()
        when (type) {
            // Gli obiettivi attivi hanno data di fine uguale o successiva a oggi E non sono completati (se hai un flag `isCompleted`)
            ObjectiveSectionType.ACTIVE -> objectives.filter { !it.endDate.isBefore(now) && !it.completed }
            // Gli obiettivi scaduti hanno data di fine precedente a oggi OPPURE sono completati
            ObjectiveSectionType.EXPIRED -> objectives.filter { it.endDate.isBefore(now) || it.completed  }
        }
    }
    val typeText = if (type.name.lowercase()== "active") "active" else "inactive"

    if (filteredObjectives.isEmpty()){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No $typeText goals found.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp, // Padding laterale
                top = 16.dp,
                end = 16.dp,   // Padding laterale
                bottom = if (listState.layoutInfo.totalItemsCount > 0) 72.dp else 16.dp // Più spazio sotto se c'è il bottone "Back"
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredObjectives, key = { it.id }) { objective -> // Aggiunta key per performance
                ObjectiveItem(objective, viewModel, showSnackbar)
            }
        }
    }
}