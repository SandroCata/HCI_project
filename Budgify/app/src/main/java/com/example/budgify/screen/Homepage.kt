package com.example.budgify.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Definisci gli stili del testo
val smallTextStyle = TextStyle(fontSize = 11.8.sp)

// Definisco la lista di schermate
val items = listOf(
    ScreenRoutes.Transactions,
    ScreenRoutes.Objectives,
    ScreenRoutes.Adding,
    ScreenRoutes.CredDeb,
    ScreenRoutes.Categories
)

@Composable
fun Homepage(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Home.route) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController, viewModel) }
    ){
        innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Box per i pie chart e istogramma
                item {
                    GraficiBox()
                }

                // Box per i conti e il saldo totale
                item {
                    ContiBox(viewModel)
                }

                item {
                    LastTransactionBox(
                        viewModel = viewModel
                    )
                }
            }
        }
}

//Composbale per visualizzare le transazioni
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transactionWithDetails: TransactionWithDetails,
    onLongClick: (MyTransaction) -> Unit
) {
    val myTransaction = transactionWithDetails.transaction
    val account = transactionWithDetails.account
    val category = transactionWithDetails.category

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable( // Use combinedClickable
                onClick = {
                    // Handle regular click if needed (e.g., view details)
                    // Log.d("TransactionItem", "Transaction clicked: ${myTransaction.id}")
                },
                onLongClick = {
                    onLongClick(myTransaction) // Call the lambda on long click
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            val formattedDescription1 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(myTransaction.description)
                }
                append("  (")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(category?.desc ?: "Uncategorized")
                }
                append(")")
            }
            Text(
                text = formattedDescription1,
                style = MaterialTheme.typography.bodyMedium,
            )
            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(account.title)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(myTransaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(
                text = formattedDescription2,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "${if (myTransaction.type == TransactionType.INCOME) "+" else "-"} ${myTransaction.amount}€",
            color = if (myTransaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun LastTransactionBox(viewModel: FinanceViewModel) { // Pass the ViewModel
    // Collect the flow of transactions with details
    val transactionsWithDetails by viewModel.allTransactionsWithDetails.collectAsStateWithLifecycle()
    // State to hold the transaction to be edited, and control dialog visibility
    var transactionToEdit by remember { mutableStateOf<MyTransaction?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = "Latest Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // Iterate through the collected list of TransactionWithDetails
                transactionsWithDetails.takeLast(5).reversed().forEach { transactionWithDetails ->
                    // Pass the TransactionWithDetails object to the updated TransactionItem
                    TransactionItem(
                        transactionWithDetails = transactionWithDetails,
                        onLongClick = { transaction ->
                            // Set the transaction to be edited and show the dialog
                            transactionToEdit = transaction
                            showEditDialog = true
                        }
                    )
                }
            }
        }
    }
    // Show the Edit Transaction Dialog if showEditDialog is true and transactionToEdit is not null
    if (showEditDialog && transactionToEdit != null) {
        Log.d("LastTransactionBox", "Showing Edit Transaction Dialog")
        EditTransactionDialog(
            transaction = transactionToEdit!!, // Pass the transaction to the dialog
            viewModel = viewModel,
            onDismiss = {
                showEditDialog = false
                transactionToEdit = null // Clear the transaction when dialog is dismissed
            }
            // You would likely need more parameters for the dialog like lists of accounts and categories
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: MyTransaction, // The transaction to be edited
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    // Use the existing transaction data as initial state for editing
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    var categoryExpanded by remember { mutableStateOf(false) }
    // Initialize selected category ID with the transaction's categoryId
    var selectedCategoryId by remember { mutableStateOf<Int?>(transaction.categoryId) }
    var selectedCategory = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.id == selectedCategoryId }
    }
    // Initialize selected date with the transaction's date
    var selectedDate by remember { mutableStateOf<LocalDate?>(transaction.date) }
    // Initialize selected type with the transaction's type
    var selectedType by remember { mutableStateOf<TransactionType>(transaction.type) }
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    var accountExpanded by remember { mutableStateOf(false) }
    // Initialize selected account ID with the transaction's accountId
    var selectedAccountId by remember { mutableStateOf<Int?>(transaction.accountId) }
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId }
    }
    val transactionTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME)

    // State for showing the DatePickerDialog
    var showDatePickerDialog by remember { mutableStateOf(false) }
    // State to control the visibility of the delete confirmation dialog
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

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
                Text(
                    "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    //modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
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
                    value = selectedCategory?.desc ?: "Select Category", // Display description or placeholder
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("No Category") }, onClick = {
                        selectedCategoryId = null
                        selectedCategory = null
                        categoryExpanded = false
                    },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.desc) },
                            onClick = {
                                selectedCategoryId = category.id // Store the ID
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
                    value = selectedAccount?.title ?: "Select Account", // Display title or placeholder
                    onValueChange = {},
                    label = { Text("Account") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.title) },
                            onClick = {
                                selectedAccountId = account.id // Store the ID
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
                modifier = Modifier.fillMaxWidth()
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
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(
                    onClick = {
                        showDeleteConfirmationDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Optional: Red color for delete
                ) {
                    Text("Delete")
                }

                Spacer(modifier = Modifier.width(8.dp)) // Add spacing

//                Button(onClick = onDismiss) {
//                    Text("Cancel")
//                }
                Button(
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedDate != null && selectedAccountId != null,
                    onClick = {
                    // Log.d("EditTransactionDialog", "Button clicked")
                    // Implement save logic
                    val amountDouble = amount.toDoubleOrNull()
                    // Add validation
                    if (description.isNotBlank() && amountDouble != null && selectedDate != null && selectedAccountId != null) {
                        // Log.d("EditTransactionDialog", "Saving updated transaction")
                        val updatedTransaction = transaction.copy( // Use copy to create a new instance with updated values
                            accountId = selectedAccountId!!,
                            type = selectedType,
                            date = selectedDate!!,
                            description = description,
                            amount = amountDouble,
                            categoryId = selectedCategoryId
                        )
                        coroutineScope.launch {
                            viewModel.updateTransaction(updatedTransaction)
                            onDismiss() // Close the dialog after updating
                        }
                    } else {
                        // Show validation error to the user
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }

    if (showDatePickerDialog) {
        // Initialize DatePickerState with the transaction's date if available
        val initialDateMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

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

    // Delete Confirmation Dialog
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteTransaction(transaction) // Delete the transaction
                            onDismiss() // Dismiss the Edit dialog
                        }
                        showDeleteConfirmationDialog = false // Dismiss the confirmation dialog
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false } // Dismiss the confirmation dialog
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Composbale per visualizzare la box dei conti
@Composable
fun ContiBox(viewModel: FinanceViewModel) {

    // Collect the flow of accounts from the ViewModel
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    // Calcola il saldo totale from the collected accounts
    val totalBalance = accounts.sumOf { it.amount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = "Total Balance: $totalBalance €",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            //Sezione scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Iterate through the collected accounts
                accounts.forEach { account ->
                    AccountItem(account = account, viewModel = viewModel) // Pass ViewModel to AccountItem
                }
                // Aggiungi l'item con il "+"
                AddAccountItem(viewModel = viewModel) // Pass ViewModel to AddAccountItem
            }
        }
    }
}

@Composable
fun AddAccountItem(viewModel: FinanceViewModel) {
    // State to control the visibility of the Add Account dialog
    var showAddAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                showAddAccountDialog = true // Show the dialog on click
            }
            .background(MaterialTheme.colorScheme.onTertiary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add Account",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.surface
        )
    }

    // Show the Add Account dialog if showAddAccountDialog is true
    if (showAddAccountDialog) {
        AddAccountDialog(
            viewModel = viewModel,
            onDismiss = { showAddAccountDialog = false }, // Hide dialog on dismiss
        )
    }
}

//Composbale per visualizzare le sezioni di ogni account
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(account: Account, viewModel: FinanceViewModel) { // Add ViewModel as a parameter
    var isLongPressed by remember { mutableStateOf(false) }
    // State to control the visibility of the delete confirmation dialog
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Obtain a CoroutineScope to launch suspend functions
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier
        .padding(8.dp)
        .width(150.dp)
        .height(65.dp)
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            onClick = {
                if (isLongPressed) {
                    isLongPressed = false // Reset if long pressed and clicked again
                } else {
                    // TODO: Handle regular click logic (e.g., show account details or transactions)
                }
            },
            onLongClick = {
                isLongPressed = true // Set state on long press
            }
        )){
        Column(
            modifier = Modifier
                .width(150.dp)
                .height(65.dp)
                .background(MaterialTheme.colorScheme.onTertiary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = account.title, fontWeight = FontWeight.Bold, color= MaterialTheme.colorScheme.surface)

            Text(text = "${account.amount}€", color= MaterialTheme.colorScheme.surface)
        }

        if (isLongPressed) {
            IconButton(
                onClick = {
                    showDeleteConfirmationDialog = true
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove Account")
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user clicks outside or presses back
                showDeleteConfirmationDialog = false
                isLongPressed = false // Also reset long press state
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the account \"${account.title}\"?\nAll transactions related to this account will also be deleted") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteAccount(account) // Delete the account
                            showDeleteConfirmationDialog = false // Dismiss the confirmation dialog
                            isLongPressed = false // Reset long press state
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false // Dismiss the confirmation dialog
                        isLongPressed = false // Reset long press state
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddAccountDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    // State for input fields
    var accountTitle by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }

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
                Text("Add New Account",
                    style = MaterialTheme.typography.titleLarge,
                    //modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = accountTitle,
                onValueChange = { accountTitle = it },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = initialBalance,
                onValueChange = { initialBalance = it },
                label = { Text("Initial Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
//                Button(onClick = onDismiss) {
//                    Text("Cancel")
//                }
                Button(
                    enabled = accountTitle.isNotBlank() && initialBalance.isNotBlank(),
                    onClick = {
                    val balanceDouble = initialBalance.toDoubleOrNull()
                    // Basic validation
                    if (accountTitle.isNotBlank() && balanceDouble != null) {
                        // Create a new Account object
                        val newAccount = Account(
                            title = accountTitle,
                            amount = balanceDouble,
                            initialAmount = balanceDouble
                        )
                        // Insert the new account using the ViewModel
                        viewModel.addAccount(newAccount)
                        onDismiss() // Close the dialog
                    } else {
                        // TODO: Show validation error message to the user
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }
}

// Composbale per visualizzare la box dei grafici
@Composable
fun GraficiBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = "Graphs",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Qui puoi aggiungere la logica per visualizzare i grafici
            Text("Pie charts...")
            Text("Histogram...")
        }
    }
}