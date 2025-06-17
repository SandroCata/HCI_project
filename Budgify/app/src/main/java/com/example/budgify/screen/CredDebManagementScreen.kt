package com.example.budgify.screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.text.lowercase
import androidx.compose.material3.ListItem // For account selection
import androidx.compose.material3.ListItemDefaults // For account selection
import kotlinx.coroutines.flow.collectLatest // For global snackbar messages if you add them
import android.util.Log // For debugging
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow

// Enum for the sections in LoanManagementScreen, aligning with LoanType
enum class LoanSectionType(val title: String, val loanType: LoanType) {
    CREDITS("Credits", LoanType.CREDIT),
    DEBTS("Debts", LoanType.DEBT);
     // Semicolon needed if you add functions/companion object

    companion object {
        fun fromLoanType(loanType: LoanType?): LoanSectionType {
            return entries.find { it.loanType == loanType } ?: DEBTS // Default to DEBTS if null or not found
        }
    }
}


@Composable
fun CredDebManagementScreen(navController: NavController, viewModel: FinanceViewModel, initialSelectedLoanType: LoanType? = null) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.CredDebManagement.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedSection by remember {
        mutableStateOf(LoanSectionType.fromLoanType(initialSelectedLoanType))
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Assicurati sia presente
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = {
            BottomBar(
                navController,
                viewModel,
                showSnackbar = showSnackbar
            )
        },
        containerColor = Color.Transparent
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
                    LoanSectionType.entries.forEach { sectionType ->
                        Tab(
                            selected = selectedSection == sectionType,
                            onClick = { selectedSection = sectionType },
                            text = { Text(sectionType.title) }
                        )
                    }
                }

                // --- TESTO ESPLICATIVO AGGIUNTO QUI ---
                val explanatoryText = when (selectedSection) {
                    LoanSectionType.CREDITS -> "Here you can track all the money you lent.\nDon't forget to reclaim it ;)"
                    LoanSectionType.DEBTS -> "Here you can find all the money you borrowed.\nRemember to return it as soon as possible ;)"
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
                // --- FINE TESTO ESPLICATIVO ---

                Text(
                    text = "Hold on a loan to manage it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    LoansSection(
                        loanType = selectedSection.loanType,
                        listState = listState,
                        viewModel = viewModel,
                        showSnackbar = showSnackbar
                    )
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
                        navController.navigate(ScreenRoutes.CredDeb.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Fallo largo quanto il contenitore per centrarlo meglio con padding
                        .padding(horizontal = 16.dp) // Padding orizzontale
                        .padding(bottom = 16.dp) // Padding dal fondo
                ) {
                    Text("Back to Loans Overview")
                }
            }
        }
    }
}


@Composable
fun LoansSection(
    loanType: LoanType,
    listState: LazyListState,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val filteredLoans = remember(allLoans, loanType) {
        allLoans.filter { it.type == loanType }
    }

    if (filteredLoans.isEmpty()) {
        // ... (testo "No loans found")
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredLoans, key = { it.id }) { loan ->
                // Passiamo snackbarHostState e scope a LoanItem se necessario,
                // o manteniamo la funzione showSnackbar come stiamo facendo.
                LoanItem(
                    loan = loan,
                    viewModel = viewModel,
                    showSnackbar = showSnackbar
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanItem(
    loan: Loan,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    var showActionChoiceDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showAccountSelectionForCompletionDialog by remember { mutableStateOf(false) }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }
    var insufficientBalanceAccountInfo by remember { mutableStateOf<Pair<String, Double>?>(null) }

    //val scope = rememberCoroutineScope()

    val backgroundColor = when (loan.type) {
        LoanType.CREDIT -> Color(0xFF4CAF50).copy(alpha = if (loan.completed) 0.5f else 0.8f)
        LoanType.DEBT -> Color(0xFFF44336).copy(alpha = if (loan.completed) 0.5f else 0.8f)
    }
    //val contentColor = Color.White.copy(alpha = if (loan.completed) 0.7f else 1f)
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (loan.completed) 0.7f else 1f)

    val image = if (loan.completed) Icons.Filled.CheckCircleOutline else Icons.Filled.RequestQuote

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    if (loan.completed) {
                        showSnackbar("This loan is already ${if (loan.type == LoanType.DEBT) "repaid" else "collected"}.")
                    } else {
                        showSnackbar("Hold to interact with the ${loan.type.name.lowercase()}.")
                    }
                },
                onLongClick = {
                    showActionChoiceDialog = true // MOSTRA SEMPRE IL DIALOGO DELLE AZIONI
                }
            )
    ) {
        Icon(
            imageVector = image, // Trophy icon
            contentDescription = "Completed Loan",
            modifier = Modifier
                .align(Alignment.CenterEnd) // Align to the center-end of the Box
                .size(80.dp) // Adjust size as needed
                .padding(end = 16.dp) // Some padding from the edge
                .alpha(0.5f), // Set transparency (0.0f is fully transparent, 1.0f is fully opaque)
            tint = contentColor.copy(alpha = 0.7f) // Optional: tint to match content color with more alpha
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .clip(RoundedCornerShape(8.dp))
//                .background(backgroundColor)
//                .combinedClickable(
//                    onClick = {
//                        if (loan.completed) {
//                            showSnackbar("This loan is already ${if (loan.type == LoanType.DEBT) "repaid" else "collected"}.")
//                        } else {
//                            showSnackbar("Hold to interact with the ${loan.type.name.lowercase()}.")
//                        }
//                    },
//                    onLongClick = {
//                        showActionChoiceDialog = true // MOSTRA SEMPRE IL DIALOGO DELLE AZIONI
//                    }
//                )
                .padding(16.dp),
        ) {
            // ... (contenuto del Column di LoanItem come prima: Text per desc, amount, date, status)
            Text(
                text = loan.desc,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
            Text(text = "Amount: ${loan.amount}€", color = contentColor)
            Text(
                text = "Added: ${loan.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = contentColor
            )
            loan.endDate?.let {
                Text(
                    text = "Due Date: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    color = contentColor
                )
            }
            if (loan.completed) {
                Text(
                    "Status: ${if (loan.type == LoanType.DEBT) "Repaid" else "Collected"}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    //color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }

    if (showActionChoiceDialog) {
        // Usa il LoanActionChoiceDialog definito più avanti (o importato)
        // che ora gestirà la logica per loan.completed
        LoanActionChoiceDialog(
            loan = loan, // Passa il loan con il suo stato 'completed'
            onDismiss = { showActionChoiceDialog = false },
            onEditClick = { // Questo verrà chiamato solo se il pulsante "Edit" è visibile e cliccabile
                showEditDialog = true
                showActionChoiceDialog = false
            },
            onDeleteClick = { // Sempre disponibile
                showDeleteConfirmationDialog = true
                showActionChoiceDialog = false
            },
            onCompleteClick = { // Questo verrà chiamato solo se il pulsante "Complete" è visibile
                Log.d("LoanItem", "Mark as Paid/Collected clicked for: ${loan.desc}")
                showAccountSelectionForCompletionDialog = true
                showActionChoiceDialog = false
            }
        )
    }


    if (showAccountSelectionForCompletionDialog) {
        val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
        val hasAccounts by viewModel.hasAccounts.collectAsStateWithLifecycle(initialValue = false)

        if (!hasAccounts && accounts.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showAccountSelectionForCompletionDialog = false },
                title = { Text("No Accounts Found") },
                text = { Text("You need to create an account first.") },
                confirmButton = {
                    TextButton(onClick = { showAccountSelectionForCompletionDialog = false }) { Text("OK") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showAccountSelectionForCompletionDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Account selection",
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        XButton(onDismiss = { showAccountSelectionForCompletionDialog = false })
                    }
                },
                text = {
                    if (accounts.isEmpty()) {
                        Text("Loading accounts...")
                    } else {
                        Column {
                            Text(
                                "Choose an account for this ${loan.type.name.lowercase()}.",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(accounts, key = { it.id }) { account ->
                                    ListItem(
                                        headlineContent = { Text(account.title) },
                                        supportingContent = { Text("Balance: ${account.amount} €") },
                                        modifier = Modifier.clickable {
                                            if (loan.type == LoanType.DEBT && loan.amount > account.amount) {
                                                insufficientBalanceAccountInfo = Pair(account.title, account.amount)
                                                showInsufficientBalanceDialog = true
                                                showAccountSelectionForCompletionDialog = false
                                            } else {
                                                viewModel.completeLoanAndCreateTransaction(
                                                    loan = loan,
                                                    accountId = account.id,
                                                    //categoryId = null
                                                )
                                                showSnackbar("${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' marked complete. Transaction for '${account.title}'.")
                                                showAccountSelectionForCompletionDialog = false
                                            }
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
                },
                dismissButton = null, //  TextButton(onClick = { showAccountSelectionForCompletionDialog = false }) { Text("Cancel") }
                confirmButton = {}
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
                            "Required: ${loan.amount} €\n" +
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

    // Il dialogo Edit si aprirà solo se loan.completed è false (gestito da LoanActionChoiceDialog)
    if (showEditDialog) {
        EditLoanDialog(
            loan = loan, // loan.completed sarà false qui
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            showSnackbar = showSnackbar,
            onDeleteRequest = {
                showEditDialog = false
                showDeleteConfirmationDialog = true
            }
        )
    }

        // Il dialogo Delete si può aprire per qualsiasi loan
    if (showDeleteConfirmationDialog) {
        ConfirmLoanDeleteDialog(
            loan = loan, // Può essere completed o non completed
            onDismiss = { showDeleteConfirmationDialog = false },
            onConfirmDelete = {
                viewModel.deleteLoan(loan)
                showDeleteConfirmationDialog = false
                showSnackbar("${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' deleted")
            }
        )
    }
}

// EditLoanDialog rimane invariato: riceverà un loan con loan.completed = false
// a causa della logica in LoanActionChoiceDialog.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loan: Loan, // Questo loan NON sarà completed quando il dialogo è mostrato
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    // ... (contenuto di EditLoanDialog come prima, sapendo che loan.completed è false)
    var description by remember { mutableStateOf(loan.desc) }
    var amount by remember { mutableStateOf(loan.amount.toString().replace('.',',')) }
    var selectedStartDate by remember { mutableStateOf(loan.startDate) }
    var selectedEndDate by remember { mutableStateOf(loan.endDate) }
    var selectedLoanType by remember { mutableStateOf(loan.type) }
    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
    val loanTypes = LoanType.entries.toList()
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
                Text("Edit ${loan.type.name.lowercase()}", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank() && errorMessage != null && errorMessage!!.contains("Description",ignoreCase = true)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = amount,
                onValueChange = { amount = it.replace(",",".").replace(Regex("[^\\d.]"), "").let { num ->
                    val parts = num.split('.')
                    if (parts.size > 1) {
                        parts[0] + "." + parts.subList(1, parts.size).joinToString("").take(2)
                    } else {
                        num
                    }
                }.replace('.',',') },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = amount.replace(',','.').toDoubleOrNull() == null && errorMessage != null && errorMessage!!.contains("Amount",ignoreCase = true)
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
                isError = errorMessage != null && selectedEndDate != null && selectedStartDate.isAfter(selectedEndDate!!)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = selectedEndDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Not set (Optional)",
                onValueChange = {},
                label = { Text("End Date (Optional)") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select End Date",
                        modifier = Modifier.clickable { showEndDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && selectedEndDate != null && selectedStartDate.isAfter(selectedEndDate!!)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    loanTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedLoanType = type }
                        ) {
                            RadioButton(
                                selected = selectedLoanType == type,
                                onClick = { selectedLoanType = type })
                            Text(type.name.replaceFirstChar { it.titlecase() })
                        }
                    }
                }
            }
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
                        errorMessage = null
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()
                        if (description.isBlank()) {
                            errorMessage = "Description cannot be empty."
                            return@Button
                        }
                        if (amountDouble == null || amountDouble <= 0) {
                            errorMessage = "Please enter a valid positive amount."
                            return@Button
                        }
                        if (selectedEndDate != null && selectedStartDate.isAfter(selectedEndDate!!)) {
                            errorMessage = "Start date cannot be after end date."
                            return@Button
                        }
                        val updatedLoan = loan.copy(
                            desc = description,
                            amount = amountDouble,
                            startDate = selectedStartDate,
                            endDate = selectedEndDate,
                            type = selectedLoanType,
                            completed = loan.completed // Preserva lo stato completed
                        )
                        coroutineScope.launch {
                            viewModel.updateLoan(updatedLoan)
                        }
                        showSnackbar("${updatedLoan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${updatedLoan.desc}' updated")
                        onDismiss()
                    }) {
                    Text("Save Changes")
                }
            }
        }
    }
    if (showStartDatePickerDialog) {
        val currentStartDateMillis = remember(selectedStartDate) {
            selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentStartDateMillis)
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
            dismissButton = { TextButton(onClick = { showStartDatePickerDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndDatePickerDialog) {
        val currentEndDateMillis = remember(selectedEndDate) {
            selectedEndDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentEndDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedEndDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePickerDialog = false
                    },
                    enabled = true
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = true) }
    }
}