package com.example.budgify.screen

import android.util.Log
import androidx.activity.result.launch
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                    Text("Back to Objectives")
                }
            }
        }
    }
}

enum class ObjectivesManagementSection(val title: String) {
    Active("Active Objectives"),
    Expired("Inactive Objectives")
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

    val backgroundColor = when (obj.type) {
        ObjectiveType.INCOME -> Color(0xff0db201)
        ObjectiveType.EXPENSE -> Color(0xffff6f51)
    }

    val isExpired = obj.endDate.isBefore(LocalDate.now())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = { showSnackbar("Hold to edit or delete the objective") },
                onLongClick = {
                    showActionChoiceDialog = true // Mostra il dialogo di scelta azione
                }
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = obj.desc, textAlign = TextAlign.Center)
        Text(
            text = "Start: ${obj.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
            textAlign = TextAlign.Center
        )
        Text(
            text = "End: ${obj.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
            textAlign = TextAlign.Center
        )
        Text(text = "${obj.amount}€", textAlign = TextAlign.Center)

        if (obj.completed) {
            Text(
                "Status: Completed",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f) // Make status visible
            )
        } else if (isExpired) {
            Text(
                "Status: Expired",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f) // Make status visible
            )
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
                text = { Text("You need to create an account first before completing an objective and creating a transaction.") },
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
                                "Choose an account to create a transaction for this objective",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(accounts, key = { it.id }) { account ->
                                    ListItem(
                                        headlineContent = { Text(account.title) },
                                        // supportingContent = { Text("Optional: Account type or balance") },
                                        // leadingContent = {
                                        //    Icon(
                                        //        Icons.Filled.AccountBalanceWallet,
                                        //        contentDescription = "Account",
                                        //    )
                                        // },
                                        modifier = Modifier.clickable {
                                            Log.d("XP_DEBUG", "Account selected: ${account.title} for objective: ${obj.desc}. Calling completeObjectiveAndCreateTransaction.")
                                            viewModel.completeObjectiveAndCreateTransaction(
                                                objective = obj,
                                                accountId = account.id,
                                                categoryId = null
                                            )
                                            // You can still show a generic local snackbar if desired:
                                            showSnackbar("Objective '${obj.desc}' marked complete. Transaction created for '${account.title}'.")
                                            showAccountSelectionForCompletionDialog = false
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
                showSnackbar("Objective '${obj.desc}' deleted")
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
                text = "Objective: '${objective.desc}'",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XButton(onDismiss)
        } },
        text = { Text("What would you like to do?")
        },
        confirmButton = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDeleteClick) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    if (!objective.completed && !hasExpired)
                    TextButton(onClick = onEditClick) {
                        Text("Edit")
                    }
                }
                // "Complete" button: Only if not completed and not expired
                if (!objective.completed) {
                    TextButton(
                        onClick = {
                            Log.d("XP_DEBUG", "Mark as Completed clicked in ActionChoiceDialog for: ${objective.desc}")
                            onCompleteClick()
                            // onDismiss() // Dismiss after action is initiated
                        }, // onDismiss is handled by the actions themselves opening new dialogs or by XButton
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Completed")
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
    var amount by remember { mutableStateOf(objective.amount.toString().replace('.',',')) }
    var selectedStartDate by remember { mutableStateOf(objective.startDate) }
    var selectedEndDate by remember { mutableStateOf(objective.endDate) }
    var selectedType by remember { mutableStateOf(objective.type) }
    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
    val objectiveTypes = ObjectiveType.entries.toList()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()


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
                Text("Edit Objective", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank() && errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = amount.replace(',','.').toDoubleOrNull() == null && errorMessage != null
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
                        errorMessage = null // Reset error
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()

                        if (description.isBlank()) {
                            errorMessage = "Description cannot be empty."
                            return@Button
                        }
                        if (amountDouble == null || amountDouble <= 0) {
                            errorMessage = "Please enter a valid positive amount."
                            return@Button
                        }
                        if (selectedStartDate.isAfter(selectedEndDate)) {
                            errorMessage = "Start date cannot be after end date."
                            return@Button
                        }

                        val updatedObjective = objective.copy(
                            desc = description,
                            amount = amountDouble,
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
        text = { Text("Are you sure you want to delete the objective \"${objective.desc}\"?") },
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

    if (filteredObjectives.isEmpty()){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No ${type.name.lowercase()} objectives found.",
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