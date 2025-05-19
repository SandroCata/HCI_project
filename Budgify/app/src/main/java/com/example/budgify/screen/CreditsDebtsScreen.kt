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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.text.format

@Composable
fun CreditsDebitsScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.CredDeb.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val totalCredits by viewModel.totalCreditLoans.collectAsStateWithLifecycle()
    val totalDebits by viewModel.totalDebtLoans.collectAsStateWithLifecycle()
    val lastThreeLoans by viewModel.lastThreeLoans.collectAsStateWithLifecycle()

    // Stati per la gestione dei dialoghi e del prestito selezionato
    var showActionChoiceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showEditLoanDialog by remember { mutableStateOf(false) }
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }

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
                    title = "Total Credits",
                    amount = totalCredits,
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = Color(0xFF4CAF50)
                ) { navController.navigate(ScreenRoutes.credDebManagementRouteWithArg(LoanType.CREDIT)) }
                ClickableAmountArea(
                    title = "Total Debts",
                    amount = totalDebits,
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
                text = "Recent Loans",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (lastThreeLoans.isEmpty()) {
                Text("No recent loans recorded.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lastThreeLoans, key = { loan -> loan.id }) { loan ->
                        LoanRow(
                            loan = loan,
                            onLongPress = { currentLoan ->
                                selectedLoan = currentLoan
                                showActionChoiceDialog = true
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
            LoanActionChoiceDialog(
                loan = loan,
                onDismiss = {
                    showActionChoiceDialog = false
                    selectedLoan = null
                },
                onEditClick = {
                    showActionChoiceDialog = false // Chiudi questo dialogo
                    showEditLoanDialog = true      // Apri il dialogo di modifica (selectedLoan è già impostato)
                },
                onDeleteClick = {
                    showActionChoiceDialog = false // Chiudi questo dialogo
                    showDeleteConfirmationDialog = true // Apri il dialogo di conferma (selectedLoan è già impostato)
                }
            )
        }

        if (showDeleteConfirmationDialog) {
            ConfirmLoanDeleteDialog(
                loan = loan,
                onDismiss = {
                    showDeleteConfirmationDialog = false
                    // Non resettare selectedLoan qui, potrebbe servire se l'utente annulla
                    // e poi vuole modificare. Si resetterà quando il dialogo principale si chiude.
                },
                onConfirmDelete = {
                    viewModel.deleteLoan(loan)
                    scope.launch { snackbarHostState.showSnackbar("'${loan.desc}' deleted") }
                    showDeleteConfirmationDialog = false
                    selectedLoan = null // Resetta dopo l'eliminazione
                }
            )
        }

        if (showEditLoanDialog) {
            EditLoanDialog(
                loanToEdit = loan,
                viewModel = viewModel,
                onDismiss = {
                    showEditLoanDialog = false
                    selectedLoan = null // Resetta se l'utente annulla la modifica
                },
                onLoanUpdated = {
                    scope.launch { snackbarHostState.showSnackbar("Loan updated successfully") }
                    showEditLoanDialog = false
                    selectedLoan = null // Resetta dopo l'aggiornamento
                }
            )
        }
    }
}

// --- COMPOSABLE PER I DIALOGHI SPECIFICI ---

@Composable
fun LoanActionChoiceDialog(
    loan: Loan,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Action for '${loan.desc}'",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XButton(onDismiss)
            }
        },
        text = { Text("What would you like to do with this loan?") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onDeleteClick) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onEditClick) {
                    Text("Edit")
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun ConfirmLoanDeleteDialog(
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
            }
        },
        text = { Text("Are you sure you want to delete '${loan.desc}'?") },
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


// --- ALTRI COMPOSABLE (ClickableAmountArea, LoanRow, EditLoanDialog) ---
// Mantieni le definizioni di ClickableAmountArea, LoanRow, e EditLoanDialog come le hai.
// Mi assicurerò che EditLoanDialog sia coerente.

@Composable
fun ClickableAmountArea(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    Column(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp) // Considera di rendere queste dimensioni più adattive se necessario
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = currencyFormat.format(amount), // Usa il formattatore di valuta
            style = MaterialTheme.typography.bodyLarge, // Rimosso fontSize per coerenza con il tema
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanRow(
    loan: Loan,
    onLongPress: (Loan) -> Unit
) {
    val amountColor = if (loan.type == LoanType.CREDIT) Color(0xFF4CAF50) else Color(0xFFF44336) // Considera i colori del tema
    val sign = if (loan.type == LoanType.CREDIT) "+" else "-"
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }


    Card(
        modifier = Modifier
            .fillMaxWidth().padding(bottom = 2.dp)
            .combinedClickable(
                onClick = {
                    // Azione per il click normale (opzionale)
                },
                onLongClick = {
                    onLongPress(loan)
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { // Per allineare le date su una riga se possibile
                    Text(
                        text = loan.startDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    loan.endDate?.let {
                        Text(
                            text = " - ${it.format(dateFormatter)}", // Più compatto
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            // modifier = Modifier.padding(start = 4.dp) // Non più necessario se "-" è incluso
                        )
                    }
                }
            }
            Text(
                text = "$sign${currencyFormat.format(loan.amount)}", // Usa il formattatore
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loanToEdit: Loan,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onLoanUpdated: () -> Unit
) {
    var description by remember { mutableStateOf(loanToEdit.desc) }
    var amountString by remember { mutableStateOf(loanToEdit.amount.toString().replace(",", ".")) } // Normalizza per Double
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(loanToEdit.startDate) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(loanToEdit.endDate) }
    var selectedType by remember { mutableStateOf(loanToEdit.type) }
    val loanTypes = remember { LoanType.entries.toList() } // Usa .entries per Enum

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var datePickerTargetIsStart by remember { mutableStateOf(true) } // true per StartDate, false per EndDate

    var errorMessages by remember { mutableStateOf<List<String>>(emptyList()) } // Lista per messaggi di errore
    var showValidationErrorDialog by remember { mutableStateOf(false) }

    fun validateFields(): Boolean {
        val errors = mutableListOf<String>()
        if (description.isBlank()) {
            errors.add("Description cannot be empty.")
        }
        val amountDouble = amountString.toDoubleOrNull()
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
        Surface( // Usa Surface per lo sfondo e il corner radius del Dialog standard
            shape = MaterialTheme.shapes.large, // O shapes.medium
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier.padding(vertical = 24.dp) // Padding per evitare che tocchi i bordi
        ) {
            Column(
                modifier = Modifier
                    // .clip(RoundedCornerShape(16.dp)) // Non più necessario se Surface ha shape
                    // .background(MaterialTheme.colorScheme.surface) // Gestito da Surface
                    .padding(24.dp) // Padding interno standard per dialoghi
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Loan",
                        style = MaterialTheme.typography.headlineSmall, // Stile più appropriato per titoli di dialogo
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
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessages.any { it.contains("amount", ignoreCase = true) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Start Date
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
                    isError = errorMessages.any { it.contains("start date", ignoreCase = true) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // End Date
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
                    isError = errorMessages.any { it.contains("End date", ignoreCase = true) }
                )
                Spacer(modifier = Modifier.height(16.dp)) // Aumentato spacer

                // Loan Type
                Text("Type:", style = MaterialTheme.typography.titleSmall) // Aggiunto stile
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Spazio tra i radio button
                ) {
                    loanTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedType = type }
                                .padding(vertical = 4.dp) // Padding per cliccabilità
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(type.name) // Capitalizza la prima lettera
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp)) // Aumentato spacer

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (validateFields()) {
                                val updatedLoan = loanToEdit.copy(
                                    desc = description.trim(),
                                    amount = amountString.toDoubleOrNull() ?: 0.0, // Assicurati che la conversione sia sicura
                                    type = selectedType,
                                    startDate = selectedStartDate!!, // La validazione assicura che non sia null
                                    endDate = selectedEndDate
                                )
                                viewModel.updateLoan(updatedLoan)
                                onLoanUpdated()
                            } else {
                                showValidationErrorDialog = true // Mostra il dialogo con tutti gli errori
                            }
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    // DatePickerDialog
    if (showDatePickerDialog) {
        val initialDateForPicker = if (datePickerTargetIsStart) {
            selectedStartDate ?: LocalDate.now()
        } else {
            selectedEndDate ?: selectedStartDate ?: LocalDate.now()
        }
        val initialDateMillis = initialDateForPicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        // val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } } // Non necessario se si usa sempre la data selezionata

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
                                // Se endDate è precedente alla nuova startDate, cancellala o aggiustala
                                if (selectedEndDate != null && selectedEndDate!!.isBefore(newSelectedDate)) {
                                    selectedEndDate = null // O newSelectedDate, a seconda della logica desiderata
                                }
                            } else {
                                selectedEndDate = newSelectedDate
                            }
                        }
                    },
                    // enabled = confirmEnabled.value // Sempre abilitato se una data è selezionata
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Dialogo di errore di validazione
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