package com.example.budgify.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Importazione corretta per LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Importazione corretta per la delega by
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Importazione corretta
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel // Assumi il percorso corretto
import com.example.budgify.entities.Loan // Assumi il percorso corretto
import com.example.budgify.entities.LoanType // Assumi il percorso corretto
import com.example.budgify.navigation.BottomBar // Assumi il percorso corretto
import com.example.budgify.navigation.TopBar // Assumi il percorso corretto
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes // Assumi il percorso corretto
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import android.util.Log // For debugging

@Composable
fun CreditsDebitsScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.CredDeb.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val totalActiveCredits by viewModel.totalActiveCreditLoans.collectAsStateWithLifecycle()
    val totalActiveDebts by viewModel.totalActiveDebtLoans.collectAsStateWithLifecycle()
    // Filter out paid loans from lastThreeLoans if you only want to show active ones here
    // Or adjust LoanRow to visually indicate paid status
    val lastThreeLoans by viewModel.latestActiveLoans.collectAsStateWithLifecycle()

    // Stati per la gestione dei dialoghi e del prestito selezionato
    var showActionChoiceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showEditLoanDialog by remember { mutableStateOf(false) }
    var showAccountSelectionForCompletionDialog by remember { mutableStateOf(false) } // New state
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) } // <--- NUOVO STATO
    var insufficientBalanceAccountInfo by remember { mutableStateOf<Pair<String, Double>?>(null) } // <--- Per nome conto

    Scaffold(
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableAmountArea(
                    title = "Total Active Credits",
                    amount = totalActiveCredits,
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = Color(0xFF4CAF50)
                ) { navController.navigate(ScreenRoutes.credDebManagementRouteWithArg(LoanType.CREDIT)) }
                ClickableAmountArea(
                    title = "Total Active Debts",
                    amount = totalActiveDebts,
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = Color(0xFFF44336)
                ) { navController.navigate(ScreenRoutes.credDebManagementRouteWithArg(LoanType.DEBT)) }
            }
            Text(
                text = "Tap on one section above for more details",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Recent Active Loans", // Consider renaming if you filter for !isPaid
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val displayLoans = lastThreeLoans.filter { !it.completed } // Optionally filter to show only active loans

            if (displayLoans.isEmpty()) {
                Text(if (lastThreeLoans.any { it.completed }) "No recent active loans. Some loans might be paid." else "No recent loans recorded.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayLoans, key = { loan -> loan.id }) { loan ->
                        LoanRow( // LoanRow will now also need to know about onLongPress
                            loan = loan,
                            onLongPress = { currentLoan ->
                                selectedLoan = currentLoan
                                // Only show actions if not paid.
                                // If LoanRow itself handles click/longClick based on isPaid, this check might be there.
                                if (!currentLoan.completed) {
                                    showActionChoiceDialog = true
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("This loan is already paid/collected.")
                                    }
                                    selectedLoan = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- GESTIONE DIALOGHI ---

    selectedLoan?.let { loan ->
        if (showActionChoiceDialog) {
            // Use the version of LoanActionChoiceDialog defined in THIS file
            LoanActionChoiceDialog(
                loan = loan,
                onDismiss = {
                    showActionChoiceDialog = false
                    selectedLoan = null
                },
                onEditClick = {
                    showActionChoiceDialog = false
                    showEditLoanDialog = true // selectedLoan is already set
                },
                onDeleteClick = {
                    showActionChoiceDialog = false
                    showDeleteConfirmationDialog = true // selectedLoan is already set
                },
                onCompleteClick = { // New callback
                    Log.d("CreditsDebitsScreen", "Complete clicked for ${loan.desc}")
                    showActionChoiceDialog = false
                    showAccountSelectionForCompletionDialog = true // selectedLoan is already set
                }
            )
        }

        if (showDeleteConfirmationDialog) {
            // Use the version of ConfirmLoanDeleteDialog defined in THIS file
            ConfirmLoanDeleteDialog(
                loan = loan,
                onDismiss = {
                    showDeleteConfirmationDialog = false
                    // Keep selectedLoan if user cancels delete, in case they want to edit.
                    // It will be reset when the main action choice dialog sequence ends.
                },
                onConfirmDelete = {
                    viewModel.deleteLoan(loan)
                    scope.launch { snackbarHostState.showSnackbar("'${loan.desc}' deleted") }
                    showDeleteConfirmationDialog = false
                    selectedLoan = null // Reset after successful deletion
                }
            )
        }

        if (showEditLoanDialog) {
            // Use the version of EditLoanDialog defined in THIS file
            EditLoanDialog(
                loanToEdit = loan,
                viewModel = viewModel,
                onDismiss = {
                    showEditLoanDialog = false
                    selectedLoan = null // Reset if user cancels edit
                },
                onLoanUpdated = {
                    scope.launch { snackbarHostState.showSnackbar("Loan '${loan.desc}' updated") }
                    showEditLoanDialog = false
                    selectedLoan = null // Reset after successful update
                }
                // If EditLoanDialog in this file needs onDeleteRequest, add it here
                // and handle showDeleteConfirmationDialog = true
            )
        }

        // Account Selection Dialog for Completing Loan
        if (showAccountSelectionForCompletionDialog) {
            val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
            val hasAccounts by viewModel.hasAccounts.collectAsStateWithLifecycle(initialValue = false)

            if (!hasAccounts && accounts.isEmpty()) {
                AlertDialog(
                    onDismissRequest = {
                        showAccountSelectionForCompletionDialog = false
                        selectedLoan = null // Reset if no account and dialog dismissed
                    },
                    title = { Text("No Accounts Found") },
                    text = { Text("You need to create an account first before completing a loan and creating a transaction.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showAccountSelectionForCompletionDialog = false
                            selectedLoan = null
                        }) {
                            Text("OK")
                        }
                    }
                )
            } else {
                AlertDialog(
                    onDismissRequest = {
                        showAccountSelectionForCompletionDialog = false
                        selectedLoan = null // Reset if dialog dismissed
                    },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Account Selection",
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            XButton(onDismiss = {
                                showAccountSelectionForCompletionDialog = false
                                selectedLoan = null
                            })
                        }
                    },
                    text = {
                        if (accounts.isEmpty()) {
                            Text("Loading accounts...")
                        } else {
                            Column {
                                Text(
                                    "Choose an account to create a transaction for this ${loan.type.name.lowercase()}.",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(accounts, key = { it.id }) { account -> // account deve essere di tipo Account con un campo balance
                                        ListItem(
                                            headlineContent = { Text(account.title) },
                                            supportingContent = {
                                                Text("Balance: ${account.amount} €")
                                            },
                                            modifier = Modifier.clickable {
                                                Log.d("CreditsDebitsScreen", "Attempting to complete loan '${loan.desc}' with account '${account.title}' (Balance: ${account.amount})")

                                                if (loan.type == LoanType.DEBT && loan.amount > account.amount) {
                                                    insufficientBalanceAccountInfo = Pair(account.title, account.amount)
                                                    showInsufficientBalanceDialog = true // <--- MOSTRA IL DIALOGO DI ERRORE
                                                    Log.w("CreditsDebitsScreen", "Insufficient balance in account '${account.title}' for debt '${loan.desc}'. Loan amount: ${loan.amount}, Account balance: ${account.amount}")
                                                } else {
                                                    // Saldo sufficiente o è un CREDITO
                                                    Log.d("CreditsDebitsScreen", "Account '${account.title}' selected for loan '${loan.desc}'. Calling completeLoanPaymentOrCollection.")
                                                    viewModel.completeLoanAndCreateTransaction(
                                                        loan = loan,
                                                        accountId = account.id,
                                                        //categoryId = null
                                                    )
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' marked complete. " +
                                                                    "Transaction created for '${account.title}'."
                                                        )
                                                    }
                                                    showAccountSelectionForCompletionDialog = false
                                                    selectedLoan = null
                                                }
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    dismissButton = null, // Or a cancel button that also resets selectedLoan
                    confirmButton = {} // Confirm button is handled by item clicks
                )
            }
        }
        if (showInsufficientBalanceDialog && selectedLoan != null && insufficientBalanceAccountInfo != null) {
            val (accTitle, accBalance) = insufficientBalanceAccountInfo!!
            AlertDialog(
                onDismissRequest = {
                    showInsufficientBalanceDialog = false
                    insufficientBalanceAccountInfo = null // Resetta le informazioni
                    // Non resettare selectedLoan qui, l'utente è ancora nel processo di selezione account
                },
                title = { Text("Insufficient Balance") },
                text = {
                    Text(
                        "The selected account '${accTitle}' does not have enough balance to repay this debt.\n\n" +
                                "Required: ${selectedLoan!!.amount} €\n" +
                                "Available in '${accTitle}': $accBalance €\n\n" +
                                "Please choose another account or add funds to this one."
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
    }
}


// --- COMPOSABLE PER I DIALOGHI SPECIFICI ---

// PUNTO 1: MODIFICA A LoanActionChoiceDialog
@Composable
fun LoanActionChoiceDialog(
    loan: Loan, // Può essere 'completed' o meno
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Action for '${loan.desc}'",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XButton(onDismiss)
            }
        },
        text = {
            if (loan.completed) {
                Text("This loan is already ${if (loan.type == LoanType.DEBT) "repaid" else "collected"}. You can only delete it.")
            } else {
                Text("What would you like to do with this loan?")
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                // Pulsante Delete SEMPRE disponibile
                TextButton(
                    onClick = {
                        onDeleteClick()
                        // onDismiss() // onDismiss è già gestito da XButton o dal chiamante che chiude showActionChoiceDialog
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }

                // Pulsanti Edit e Complete SOLO se il prestito NON è completato
                if (!loan.completed) {
                    TextButton(onClick = {
                        onEditClick()
                        // onDismiss()
                    }) {
                        Text("Edit")
                    }
                    TextButton(onClick = {
                        onCompleteClick()
                        // onDismiss()
                    }) {
                        Text(if (loan.type == LoanType.DEBT) "Mark as Repaid" else "Mark as Collected")
                    }
                }
            }
        },
        dismissButton = null // Gestito da XButton nel titolo
        /* Oppure, se preferisci un pulsante "Cancel" esplicito in basso:
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
        */
    )
}

@Composable
fun ConfirmLoanDeleteDialog( // This is the version specific to CreditsDebitsScreen
    loan: Loan,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Confirm Deletion", modifier = Modifier.weight(1f))
                // Optionally add XButton here too if desired, though standard is explicit Cancel/Delete buttons
                // XButton(onDismiss)
            }
        },
        text = { Text("Are you sure you want to delete '${loan.desc}'? This action cannot be undone.") }, // Added warning
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
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
fun ClickableAmountArea(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    // ... (your existing ClickableAmountArea implementation - looks good)
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("it", "IT")) } // Example for Euro formatting

    Column(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = currencyFormat.format(amount), // Use the currency formatter
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanRow(
    loan: Loan,
    onLongPress: (Loan) -> Unit // Keep this for interaction
    // onClick: (Loan) -> Unit // Optional: if you want a different action for normal click
) {
    val amountColor = if (loan.type == LoanType.CREDIT) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (loan.type == LoanType.CREDIT) "+" else "-"
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("it", "IT")) }

    val cardAlpha = if (loan.completed) 0.6f else 1f // Visual indication for paid loans

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .combinedClickable(
                onClick = {
                    // Example: Log or show a toast that it's paid if clicked and paid
                    // if (loan.isPaid) { Log.d("LoanRow", "${loan.desc} is already paid.") }
                    // else { onClick(loan) } // Or some other action for unpaid items
                },
                onLongClick = {
                    if (!loan.completed) { // Only allow long press actions if not paid
                        onLongPress(loan)
                    }
                }
            )
            .clip(RoundedCornerShape(12.dp)), // clip before background if using alpha on background
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha) // Apply alpha here
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)) {
                Text(
                    text = loan.desc,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = loan.startDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardAlpha)
                    )
                    loan.endDate?.let {
                        Text(
                            text = " - ${it.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardAlpha),
                        )
                    }
                }
                if (loan.completed) { // Add a status text if paid
                    Text(
                        text = if (loan.type == LoanType.DEBT) "Repaid" else "Collected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = (if (loan.type == LoanType.DEBT) amountColor else amountColor).copy(alpha = cardAlpha + 0.2f) // Slightly more opaque
                    )
                }
            }
            Text(
                text = "$sign${currencyFormat.format(loan.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor.copy(alpha = cardAlpha)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog( // This is the version for CreditsDebitsScreen
    loanToEdit: Loan,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onLoanUpdated: () -> Unit
    // Consider adding onDeleteRequest: () -> Unit if you want delete option from here
) {
    // ... (Your existing EditLoanDialog implementation from CreditsDebitsScreen.kt)
    // It seems mostly fine. Ensure it handles the 'isPaid' status correctly if you ever
    // allow editing paid loans (though typically editing is for active items).
    // For now, assume we only edit !isPaid loans from this screen's flow.

    var description by remember { mutableStateOf(loanToEdit.desc) }
    var amountString by remember { mutableStateOf(loanToEdit.amount.toString().replace('.',',')) } // Use comma for display
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(loanToEdit.startDate) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(loanToEdit.endDate) }
    var selectedType by remember { mutableStateOf(loanToEdit.type) }
    val loanTypes = remember { LoanType.entries.toList() }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var datePickerTargetIsStart by remember { mutableStateOf(true) }

    var errorMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }

    fun validateFields(): Boolean {
        val errors = mutableListOf<String>()
        if (description.isBlank()) {
            errors.add("Description cannot be empty.")
        }
        val amountDouble = amountString.replace(',', '.').toDoubleOrNull() // Convert to dot for Double
        if (amountDouble == null || amountDouble <= 0) {
            errors.add("Please enter a valid positive amount.")
        }
        if (selectedStartDate == null) {
            errors.add("Please select a start date.")
        }
        if (selectedEndDate != null && selectedStartDate != null && selectedEndDate!!.isBefore(selectedStartDate!!)) {
            errors.add("End date cannot be before the start date.")
        }
        errorMessages = errors
        return errors.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Loan",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    XButton(onDismiss)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessages.any { it.contains("Description", ignoreCase = true) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = amountString, // Display with comma
                    onValueChange = {
                        // Allow only numbers and one comma, then replace comma with dot for validation
                        amountString = it.replace(Regex("[^\\d,]"), "").let { num ->
                            val parts = num.split(',')
                            if (parts.size > 1) {
                                parts[0] + "," + parts.subList(1, parts.size).joinToString("")
                            } else {
                                num
                            }
                        }
                    },
                    label = { Text("Amount (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessages.any { it.contains("amount", ignoreCase = true) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = selectedStartDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            datePickerTargetIsStart = true
                            showDatePickerDialog = true
                        }) {
                            Icon(Icons.Default.CalendarToday, "Select Start Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessages.any { it.contains("start date", ignoreCase = true) && selectedStartDate == null } ||
                            (selectedEndDate != null && selectedStartDate != null && selectedEndDate!!.isBefore(selectedStartDate!!))
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = selectedEndDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Not set",
                    onValueChange = {},
                    label = { Text("End Date (Optional)") },
                    readOnly = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedEndDate != null) {
                                IconButton(onClick = { selectedEndDate = null }) {
                                    Icon(Icons.Default.Clear, "Clear End Date")
                                }
                            }
                            IconButton(onClick = {
                                datePickerTargetIsStart = false
                                showDatePickerDialog = true
                            }) {
                                Icon(Icons.Default.CalendarToday, "Select End Date")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessages.any { it.contains("End date", ignoreCase = true) && selectedEndDate != null && selectedStartDate !=null && selectedEndDate!!.isBefore(selectedStartDate!!) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Type:", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    loanTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedType = type }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(type.name.replaceFirstChar { it.titlecase() }) // Title case
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center // Or SpaceBetween if adding Delete
                ) {
                    // Optional: Add Delete button here if desired
                    // TextButton(onClick = { /* Call onDeleteRequest if implemented */ }) { Text("Delete") }
                    Button(
                        onClick = {
                            if (validateFields()) {
                                val updatedLoan = loanToEdit.copy(
                                    desc = description.trim(),
                                    amount = amountString.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                    type = selectedType,
                                    startDate = selectedStartDate!!,
                                    endDate = selectedEndDate,
                                    completed = loanToEdit.completed // Preserve isPaid status
                                )
                                viewModel.updateLoan(updatedLoan)
                                onLoanUpdated() // This will call onDismiss in the calling Composable
                            } else {
                                showValidationErrorDialog = true
                            }
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val initialDateForPicker = if (datePickerTargetIsStart) {
            selectedStartDate ?: LocalDate.now()
        } else {
            selectedEndDate ?: selectedStartDate ?: LocalDate.now()
        }
        val initialDateMillis = initialDateForPicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newSelectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            if (datePickerTargetIsStart) {
                                selectedStartDate = newSelectedDate
                                if (selectedEndDate != null && selectedEndDate!!.isBefore(newSelectedDate)) {
                                    selectedEndDate = null
                                }
                            } else {
                                // For end date, ensure it's not before start date
                                if (selectedStartDate != null && newSelectedDate.isBefore(selectedStartDate!!)) {
                                    // Show error or auto-adjust, for now, just set it
                                    // You might want to show a Toast/Snackbar here
                                    selectedEndDate = newSelectedDate // or selectedStartDate
                                } else {
                                    selectedEndDate = newSelectedDate
                                }
                            }
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = true) } // Added showModeToggle
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            title = { Text("Validation Error") },
            text = {
                Column {
                    errorMessages.forEach { Text("- $it") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showValidationErrorDialog = false }) { Text("OK") }
            }
        )
    }
}