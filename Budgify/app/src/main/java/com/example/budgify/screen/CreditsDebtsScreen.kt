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
import androidx.compose.material3.Button
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
import com.example.budgify.routes.ScreenRoutes // Assumi il percorso corretto
import kotlinx.coroutines.launch
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

    // Stato per mostrare il dialogo di conferma eliminazione
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var loanToDelete by remember { mutableStateOf<Loan?>(null) }

    // Nuovi stati per la scelta Modifica/Elimina e per il prestito selezionato
    var showActionChoiceDialog by remember { mutableStateOf(false) }
    var selectedLoanForAction by remember { mutableStateOf<Loan?>(null) }

    // Stato per mostrare il dialogo di modifica (da creare separatamente)
    var showEditLoanDialog by remember { mutableStateOf(false)}

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
                    iconColor = Color(0xFF4CAF50),
                    onClick = {
                        navController.navigate(ScreenRoutes.CredDebManagement.route)
                    }
                )
                ClickableAmountArea(
                    title = "Total Debts",
                    amount = totalDebits,
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = Color(0xFFF44336),
                    onClick = {
                        navController.navigate(ScreenRoutes.CredDebManagement.route)
                    }
                )
            }

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
                            onLongPress = { currentLoan -> // Rinominato per chiarezza
                                selectedLoanForAction = currentLoan // Imposta il prestito per l'azione
                                showActionChoiceDialog = true    // Mostra il dialogo di scelta
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogo di scelta Azione (Modifica o Elimina)
    if (showActionChoiceDialog && selectedLoanForAction != null) {
        AlertDialog(
            onDismissRequest = {
                showActionChoiceDialog = false
                selectedLoanForAction = null
            },
            title = { Text("Choose Action for '${selectedLoanForAction!!.desc}'") },
            text = { Text("What would you like to do with this loan?") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Occupa tutta la larghezza per distribuire i pulsanti
                    horizontalArrangement = Arrangement.SpaceEvenly // O Arrangement.End se li vuoi allineati a destra
                ) {
                    // Pulsante Delete (a sinistra)
                    TextButton(
                        onClick = {
                            loanToDelete = selectedLoanForAction
                            showDeleteConfirmationDialog = true
                            showActionChoiceDialog = false
                            selectedLoanForAction = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }

                    // Pulsante Edit (in mezzo)
                    TextButton(
                        onClick = {
                            showEditLoanDialog = true
                            // showActionChoiceDialog = false; // Non serve chiuderlo qui, lo farà onDismiss o il pulsante Cancel
                            // selectedLoanForAction rimane impostato per il dialogo di modifica
                        }
                    ) {
                        Text("Edit")
                    }

                    // Pulsante Cancel (a destra)
                    TextButton(
                        onClick = {
                            showActionChoiceDialog = false
                            selectedLoanForAction = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = null // Non usiamo un dismissButton separato in questa configurazione
            // L'azione onDismissRequest gestisce la chiusura se si clicca fuori
        )
    }

    // Dialogo di conferma eliminazione (esistente, ma ora attivato dal dialogo di scelta)
    if (showDeleteConfirmationDialog && loanToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                loanToDelete = null
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${loanToDelete!!.desc}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLoan(loanToDelete!!)
                        showDeleteConfirmationDialog = false
                        loanToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        loanToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialogo di Modifica Prestito (DA IMPLEMENTARE)
    if (showEditLoanDialog && selectedLoanForAction != null) {
        // Qui dovrai chiamare un Composable simile ad `AddLoanDialog`,
        // ma pre-compilato con i dati di `selectedLoanForAction`
        // e con una logica per aggiornare il prestito esistente nel ViewModel.
        // Esempio:
        EditLoanDialog( // Supponendo tu crei questo Composable
            loanToEdit = selectedLoanForAction!!,
            viewModel = viewModel,
            onDismiss = {
                showEditLoanDialog = false
                selectedLoanForAction = null // Resetta dopo la modifica o l'annullamento
            },
            onLoanUpdated = {
                // Opzionale: mostra uno snackbar o un messaggio di successo
                scope.launch {
                    snackbarHostState.showSnackbar("Loan updated successfully")
                }
                showEditLoanDialog = false
                selectedLoanForAction = null
            }
        )
    }
}

@Composable
fun ClickableAmountArea(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "%.2f €".format(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class) // Aggiungi questa annotazione
@Composable
fun LoanRow(
    loan: Loan,
    onLongPress: (Loan) -> Unit // Lambda per gestire il long press
) {
    val amountColor = if (loan.type == LoanType.CREDIT) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (loan.type == LoanType.CREDIT) "+" else "-"
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable( // Usa combinedClickable
                onClick = {
                    // Azione per il click normale (se necessaria, altrimenti lasciala vuota)
                    // Ad esempio: navController.navigate("loan_details_screen/${loan.id}")
                },
                onLongClick = { // Gestisci il long click qui
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
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = loan.startDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Mostra endDate solo se non è null
                loan.endDate?.let {
                    Text(
                        text = " | End: ${it.format(dateFormatter)}", // Aggiungi un separatore
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(
                text = "$sign%.2f €".format(loan.amount),
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
    onLoanUpdated: () -> Unit // Callback per quando il prestito è aggiornato
) {
    var description by remember { mutableStateOf(loanToEdit.desc) }
    var amount by remember { mutableStateOf(loanToEdit.amount.toString()) } // Converte double in stringa
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(loanToEdit.startDate) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(loanToEdit.endDate) }
    var selectedType by remember { mutableStateOf(loanToEdit.type) }
    val loanTypes = LoanType.entries.toList()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun triggerError(message: String) {
        errorMessage = message
        showErrorDialog = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Edit Loan", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // TextField per Description (pre-compilato)
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank() && showErrorDialog
            )
            Spacer(modifier = Modifier.height(8.dp))

            // TextField per Amount (pre-compilato)
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0) && showErrorDialog
            )
            Spacer(modifier = Modifier.height(8.dp))

            // TextField per Start Date (pre-compilato)
            TextField(
                value = selectedStartDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Start Date",
                        modifier = Modifier.clickable {
                            datePickerTarget = "START"
                            showDatePickerDialog = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = selectedStartDate == null && showErrorDialog
            )
            Spacer(modifier = Modifier.height(8.dp))

            // TextField per End Date (pre-compilato, gestisce null)
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
                            datePickerTarget = "END"
                            showDatePickerDialog = true
                        }) {
                            Icon(Icons.Default.CalendarToday, "Select End Date")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // RadioButton per Loan Type (pre-compilato)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    loanTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.name)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Pulsanti Salva Modifiche / Annulla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Allinea i pulsanti a destra
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (description.isBlank()) {
                            triggerError("Description cannot be empty.")
                            return@Button
                        }
                        val amountDouble = amount.toDoubleOrNull()
                        if (amountDouble == null || amountDouble <= 0) {
                            triggerError("Please enter a valid amount.")
                            return@Button
                        }
                        if (selectedStartDate == null) {
                            triggerError("Please select a start date.")
                            return@Button
                        }
                        if (selectedEndDate != null && selectedStartDate != null && selectedEndDate!!.isBefore(selectedStartDate!!)) {
                            triggerError("End date cannot be before the start date.")
                            return@Button
                        }

                        val updatedLoan = loanToEdit.copy( // Crea una copia con i valori aggiornati
                            desc = description,
                            amount = amountDouble,
                            type = selectedType,
                            startDate = selectedStartDate!!,
                            endDate = selectedEndDate
                        )
                        viewModel.updateLoan(updatedLoan) // Assicurati di avere questo metodo nel ViewModel
                        onLoanUpdated() // Chiama il callback
                        // onDismiss() // onLoanUpdated dovrebbe gestire la chiusura o farlo il chiamante
                    }
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    // DatePickerDialog (identico a quello di AddLoanDialog)
    if (showDatePickerDialog) {
        val initialDateForPicker = when (datePickerTarget) {
            "START" -> selectedStartDate ?: LocalDate.now()
            "END" -> selectedEndDate ?: selectedStartDate ?: LocalDate.now()
            else -> LocalDate.now()
        }
        val initialDateMillis = initialDateForPicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog( /* ...implementazione identica... */
            onDismissRequest = { showDatePickerDialog = false; datePickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newSelectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            when (datePickerTarget) {
                                "START" -> selectedStartDate = newSelectedDate
                                "END" -> selectedEndDate = newSelectedDate
                            }
                        }
                        datePickerTarget = null
                    },
                    enabled = confirmEnabled.value
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false; datePickerTarget = null }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }


    // Dialogo di errore (identico a quello di AddLoanDialog)
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Validation Error") },
            text = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = { showErrorDialog = false }) { Text("OK") } }
        )
    }
}