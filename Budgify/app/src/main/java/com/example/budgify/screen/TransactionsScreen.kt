package com.example.budgify.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Transactions.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // State to hold the selected date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(
            navController,
            viewModel,
            showSnackbar = showSnackbar
        ) }
    ){
            innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top, // Distribute space between sections
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                // Pass the onDaySelected lambda to update the selectedDate state
                MonthlyCalendar(
                    onDaySelected = { date ->
                        selectedDate = date // Update the selected date state
                    }
                )
            }

                item {
                    // Pass the selected date to TransactionBox
                    TransactionBox(
                        selectedDate = selectedDate,
                        viewModel = viewModel,
                        showSnackbar = showSnackbar
                    )
                }
            }


        }
    }


@Composable
fun MonthlyCalendar(
    onDaySelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }

    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun")

    // Calcola l'offset per allineare il primo giorno del mese
    val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 // 1 per lunedì, 7 per domenica
    val startOffset = if (firstDayOfWeekValue == 0) 6 else firstDayOfWeekValue - 1 // 0-6 per allineamento con Lun-Dom

    val daysOfMonth = (1..daysInMonth).map { currentMonth.atDay(it) }
    val calendarDays = (List(startOffset) { null } + daysOfMonth).map { it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Header del calendario (mese e navigazione)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mese Precedente")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mese Successivo")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nomi dei giorni della settimana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid of the month days - Use a simple Column with Rows instead of LazyVerticalGrid
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Divide the calendarDays list into rows of 7 days
            val weeks = calendarDays.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Add empty boxes for padding at the start if the first week is not full
                    val daysInWeek = week.size
                    val emptySlots = 7 - daysInWeek

                    week.forEach { day ->
                        val isSelected = selectedDate.value == day
                        val isToday = day == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f) // Give each day equal weight in the row
                                .aspectRatio(1f) // Keep the aspect ratio
                                .padding(2.dp)
                                .clickable(enabled = day != null) {
                                    if (day != null) {
                                        selectedDate.value = day
                                        onDaySelected(day)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                ) {
                                    Text(
                                        text = day.dayOfMonth.toString(),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> LocalContentColor.current
                                        },
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add empty boxes to fill the remaining space in the last row
                    repeat(emptySlots) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Assume TransactionItem is a composable that displays a single transaction
@OptIn(ExperimentalFoundationApi::class) // Opt-in for ExperimentalFoundationApi
@Composable
fun TransactionItem1(
    transactionWithDetails: TransactionWithDetails,
    onClick: (MyTransaction) -> Unit,
    onLongClick: (MyTransaction) -> Unit // Add a long click lambda parameter
) {
    val myTransaction = transactionWithDetails.transaction
    val account = transactionWithDetails.account
    val category = transactionWithDetails.category

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable( // Use combinedClickable
                onClick = {
                    onClick(myTransaction)
                },
                onLongClick = {
                    onLongClick(myTransaction) // Call the lambda on long click
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val formattedDescription1 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(myTransaction.description)
                }
                append("  (")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    // Access the description from the related Category object
                    append(category?.desc ?: "Uncategorized") // Use safe call and default if category is null
                }
                append(")")
            }
            Text(text = formattedDescription1, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(3.dp))
            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    // Access the title from the related Account object
                    append(account.title)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    // Date formatting remains the same
                    append(myTransaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(text = formattedDescription2, fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
        }
        Text(
            text = "${if (myTransaction.type == TransactionType.INCOME) "+" else "-"} ${myTransaction.amount}€",
            color = if (myTransaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionBox(
    selectedDate: LocalDate?,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    // Usa collectAsState o collectAsStateWithLifecycle come preferisci
    val allTransactionsWithDetails by viewModel.allTransactionsWithDetails.collectAsState(initial = emptyList())

    val transactionsForSelectedDate = if (selectedDate != null) {
        allTransactionsWithDetails.filter { it.transaction.date == selectedDate }
    } else {
        // Se nessuna data è selezionata, mostra le ultime transazioni o una lista vuota.
        // Modifica questo comportamento se necessario.
        allTransactionsWithDetails.takeLast(5).reversed()
    }

    var transactionToAction by remember { mutableStateOf<MyTransaction?>(null) }
    var showTransactionActionChoiceDialog by remember { mutableStateOf(false) }
    var showEditTransactionDialog by remember { mutableStateOf(false) } // Rinomina per coerenza con Homepage.LastTransactionBox
    var showDeleteTransactionConfirmationDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // .fillMaxHeight() // Valuta se fillMaxHeight è appropriato qui
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = if (selectedDate != null) selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) else "Latest Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                if (transactionsForSelectedDate.isEmpty() && selectedDate != null) {
                    Text("No transactions for this date.", style = MaterialTheme.typography.bodyMedium)
                } else if (transactionsForSelectedDate.isEmpty() && selectedDate == null) {
                    Text("No transactions found.", style = MaterialTheme.typography.bodyMedium)
                } else if (transactionsForSelectedDate.isEmpty()) {
                    // Questo caso potrebbe essere ridondante a causa dei precedenti, ma lo lascio per completezza
                    Text("No transactions available.", style = MaterialTheme.typography.bodyMedium)
                }
                else {
                    transactionsForSelectedDate.forEach { transactionWithDetails ->
                        // Assicurati che TransactionItem1 sia definito o usa TransactionItem da Homepage.kt
                        // Se usi TransactionItem da Homepage.kt, assicurati che accetti gli stessi parametri.
                        // Per questo esempio, assumo che tu voglia usare TransactionItem1 come definito nel tuo codice.
                        TransactionItem1( // O TransactionItem se hai adattato i parametri
                            transactionWithDetails = transactionWithDetails,
                            onClick = { transaction -> // Il tipo di 'transaction' dovrebbe essere MyTransaction
                                showSnackbar("Hold to edit or delete the transaction")
                            },
                            onLongClick = { transaction -> // Il tipo di 'transaction' dovrebbe essere MyTransaction
                                transactionToAction = transaction
                                showTransactionActionChoiceDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Transaction Action Choice Dialog (simile a Homepage.LastTransactionBox)
    // Transaction Action Choice Dialog
    if (showTransactionActionChoiceDialog && transactionToAction != null) {
        AlertDialog(
            onDismissRequest = {
                showTransactionActionChoiceDialog = false
                transactionToAction = null
            },
            title = { Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction: '${transactionToAction?.description}'",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XButton({
                    showTransactionActionChoiceDialog = false
                    transactionToAction = null
                })
            }
                    },
            text = { Text("What would you like to do?") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            showDeleteTransactionConfirmationDialog = true
                            showTransactionActionChoiceDialog = false
                            // transactionToAction remains set for the delete confirmation
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(
                        onClick = {
                            showEditTransactionDialog = true
                            showTransactionActionChoiceDialog = false
                            // transactionToAction remains set for the edit dialog
                        }
                    ) {
                        Text("Edit")
                    }
                }
            },
            dismissButton = null
        )
    }

    // Show the Edit Transaction Dialog (usa EditTransactionDialog2 rinominato o il tuo EditTransactionDialog)
    if (showEditTransactionDialog && transactionToAction != null) {
        EditTransactionDialog2( // O EditTransactionDialog se hai rinominato
            transaction = transactionToAction!!,
            viewModel = viewModel,
            onDismiss = {
                showEditTransactionDialog = false
                transactionToAction = null
            }
        )
    }

    // Delete Transaction Confirmation Dialog (simile a Homepage.LastTransactionBox)
    if (showDeleteTransactionConfirmationDialog && transactionToAction != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteTransactionConfirmationDialog = false
                // Non resettare transactionToAction qui se vuoi permettere all'utente di tornare
                // al dialog di scelta, ma per semplicità lo resettiamo.
                // transactionToAction = null
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this transaction: \"${transactionToAction?.description}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            transactionToAction?.let { viewModel.deleteTransaction(it) }
                            showDeleteTransactionConfirmationDialog = false
                            transactionToAction = null // Pulisci dopo l'eliminazione
                            showSnackbar("Transaction deleted")
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red) // O MaterialTheme.colorScheme.error
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteTransactionConfirmationDialog = false
                        // transactionToAction = null // Opzionalmente pulisci anche qui
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog2(
    transaction: MyTransaction, // The transaction to be edited
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    // Use the existing transaction data as initial state for editing
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString().replace('.', ',')) } // Use comma for display
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(transaction.categoryId) }
    val selectedCategory = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.id == selectedCategoryId }
    }
    var selectedDate by remember { mutableStateOf<LocalDate?>(transaction.date) }
    var selectedType by remember { mutableStateOf<TransactionType>(transaction.type) }
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    var accountExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(transaction.accountId) }
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId }
    }
    val transactionTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }


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
                Text(
                    "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                )
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
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = amount.replace(',', '.').toDoubleOrNull() == null && errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category Dropdown Menu
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedCategory?.desc ?: "Uncategorized",
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    // No specific error state needed here unless selection is mandatory and not made
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Uncategorized", style = TextStyle(fontWeight = FontWeight.Bold)) },
                        onClick = {
                            selectedCategoryId = null
                            // selectedCategory will update via remember
                            categoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.desc) },
                            onClick = {
                                selectedCategoryId = category.id
                                // selectedCategory will update via remember
                                categoryExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Account Dropdown Menu
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedAccount?.title ?: "Select Account",
                    onValueChange = {},
                    label = { Text("Account") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    isError = selectedAccountId == null && errorMessage != null // Account selection is mandatory
                )

                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.title) },
                            onClick = {
                                selectedAccountId = account.id
                                // selectedAccount will update via remember
                                accountExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { showDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = selectedDate == null && errorMessage != null // Date selection is mandatory
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    transactionTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.toString())
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
                horizontalArrangement = Arrangement.Center// Align save button to the end
            ) {
                // REMOVED Delete Button from here
                // Spacer(modifier = Modifier.width(8.dp)) // REMOVED

                Button(
                    // The enabled state logic is now part of the validation check before saving
                    onClick = {
                        errorMessage = null // Reset error message at the start of save attempt
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()

                        if (description.isBlank()) {
                            errorMessage = "Description cannot be empty."
                            return@Button
                        }
                        if (amountDouble == null || amountDouble <= 0) { // Also check if positive
                            errorMessage = "Please enter a valid positive amount."
                            return@Button
                        }
                        if (selectedAccountId == null) {
                            errorMessage = "Please select an account."
                            return@Button
                        }
                        if (selectedDate == null) {
                            errorMessage = "Please select a date."
                            return@Button
                        }

                        // All checks passed, proceed to update
                        val updatedTransaction = transaction.copy(
                            accountId = selectedAccountId!!, // Not null due to check above
                            type = selectedType,
                            date = selectedDate!!, // Not null due to check above
                            description = description,
                            amount = amountDouble, // Not null due to check above
                            categoryId = selectedCategoryId
                        )
                        coroutineScope.launch {
                            viewModel.updateTransaction(updatedTransaction)
                            onDismiss() // Close the dialog after updating
                        }
                    }) {
                    Text("Save Changes")
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val initialDatePickerMillis = remember(transaction.date) { // Ensure this re-calculates if transaction.date changes
            transaction.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDatePickerMillis)

        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                showDatePickerDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}





