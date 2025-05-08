package com.example.budgify.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.budgify.datastruct.Account
import com.example.budgify.datastruct.Transaction
import com.example.budgify.routes.ScreenRoutes
import java.time.LocalDate
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
fun Homepage(navController: NavController) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Home.route) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
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
                    ContiBox()
                }

                item {
                    LastTransactionBox()
                }
            }
        }
}

//Composbale per visualizzare le transazioni
@Composable
fun TransactionItem(transaction: Transaction) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            val formattedDescription1 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.description)
                }
                append("  (")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(transaction.category)
                }
                append(")")
            }
            Text(
                text = formattedDescription1,
                style = MaterialTheme.typography.bodyMedium,
            )
            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.account)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(
                text = formattedDescription2,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "${if (transaction.type) "+" else "-"} ${transaction.amount}€",
            color = if (transaction.type) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun LastTransactionBox() {
    // Qui simuliamo i dati da un file locale, che poi andranno presi da un DB
    val transactions = listOf(
        Transaction(1, "Bank",false, LocalDate.now(), "Spesa Alimentari", 50.0, "Cibo"),
        Transaction(2, "Wallet",false, LocalDate.now().minusDays(1), "Biglietto Autobus", 2.0, "Trasporto"),
        Transaction(3, "Bank",true, LocalDate.now().minusDays(3), "Mensilitá lavoro", 1500.0, "Stipendio"),
        Transaction(6, "Bank",true, LocalDate.now().minusDays(3), "Maglietta Vinted", 100.0, "Vendite"),
        Transaction(7, "Wallet",false, LocalDate.now().minusDays(1), "Spesa Alimentari", 65.0, "Cibo"),
    )
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
                transactions.take(5).forEach { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

// Composbale per visualizzare la box dei conti
@Composable
fun ContiBox() {
    // Dati di esempio per i conti
    val accounts = listOf(
        Account("Bank", 1000.0),
        Account("Savings", 5000.0),
        Account("Wallet", 150.0)
    )

    // Calcola il saldo totale
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
                accounts.forEach { account ->
                    AccountItem(account = account)
                }
                // Aggiungi l'item con il "+"
                AddAccountItem()
            }
        }
    }
}

@Composable
fun AddAccountItem() {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { /* TODO: Handle add account logic */ }) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Account",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

//Composbale per visualizzare le sezioni di ogni account
@Composable
fun AccountItem(account: Account) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = account.title, fontWeight = FontWeight.Bold)
        Text(text = "${account.amount}€", color = Color.Gray)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, currentRoute: String) {
    val title = when (currentRoute) {
        "home_screen" -> "Dashboard"
        "objectives_screen" -> "Objectives"
        "objectives_management_screen" -> "Objectives"
        "settings_screen" -> "Settings"
        "transactions_screen" -> "Transactions"
        "cred_deb_screen" -> "Loans"
        "categories_screen" -> "Categories"
        else -> ""
    }
    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 30.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.LightGray,
            titleContentColor = Color.Black
        ),
        navigationIcon = {
            if (currentRoute != ScreenRoutes.Home.route) {
                IconButton(onClick = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(50.dp))
                }
            }
        },
        actions = { // Aggiunte le icone in alto a destra
            IconButton(onClick = {
                navController.navigate(ScreenRoutes.Settings.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                Icon( Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(50.dp))
            }

        }
    )
}

@Composable
fun BottomBar(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddObjectiveDialog by remember { mutableStateOf(false) }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(bottom = 0.dp)
    ) {
        NavigationBar(modifier = Modifier.align(Alignment.BottomCenter)) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val desc = ""
            items.forEach { screen ->
                val iconModifier = if (screen == ScreenRoutes.Adding) {
                    Modifier.size(45.dp)// Icona ingrandita per "Add"
                } else {
                    Modifier.size(30.dp) // Icona standard per le altre schermate
                }
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = screen.icon),
                            contentDescription = null,
                            modifier = iconModifier
                        )
                    },
                    label = { if(screen != ScreenRoutes.Adding){ //aggiunto l'if per mostrare la label
                        Text(screen.title, style = smallTextStyle)
                    } },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        if (screen == ScreenRoutes.Adding) {
                            showDialog = true
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }

        // Custom Dialog
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Transparent) // Set background to transparent
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // This line disables the visual click indication
                            onClick = { showDialog = false }
                        ),
                    contentAlignment = Alignment.BottomCenter // This aligns the content within the Box to the bottom

                ) {
                    Column(
                        modifier = Modifier
                            .padding(0.dp, 0.dp, 0.dp, 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                showAddTransactionDialog = true
                                showDialog = false
                                      },
                            modifier = Modifier
                                .padding(0.dp)
                                .width(150.dp)
                                .height(50.dp)
                        ) {
                            Text("Transaction")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                showAddObjectiveDialog = true
                                showDialog = false
                                      },
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp)
                                .padding(0.dp)
                        ) {
                            Text("Objective")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                showAddLoanDialog = true
                                showDialog = false
                                      },
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp)
                                .padding(0.dp)
                        ) {
                            Text("Loan")
                        }
                    }
                }
            }
        }

        // Dialog for adding a transaction
    if (showAddTransactionDialog) {
        AddTransactionDialog(onDismiss = { showAddTransactionDialog = false }) {
            // Handle transaction added
        }
    }

    // Dialog for adding an objective
    if (showAddObjectiveDialog) {
        AddObjectiveDialog(onDismiss = { showAddObjectiveDialog = false }) {
            // Handle objective added
        }
    }

    // Dialog for adding a loan
    if (showAddLoanDialog) {
        AddLoanDialog(onDismiss = { showAddLoanDialog = false }) {
            // Handle loan added
        }
    }
    }
}

@Composable
fun AddTransactionDialog(onDismiss: () -> Unit, onTransactionAdded: (Transaction) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") } // State for selected category
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) } // State for selected date
    var selectedType by remember { mutableStateOf("Expense") } // State for transaction type (Expense/Income)
    var selectedAccount by remember { mutableStateOf("") } // State for selected account

    val categories = listOf("Food", "Transport", "Shopping", "Utilities", "Salary", "Other") // Example categories
    val accounts = listOf("Cash", "Bank Account 1", "Credit Card") // Example accounts
    val transactionTypes = listOf("Expense", "Income")

    // State for showing the DatePickerDialog
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Add Transaction",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            Spacer(modifier = Modifier.height(16.dp))

            //Input fields for transaction details
            //For example, using TextField:
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Amount TextField (with Number keyboard)
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category Dropdown Menu
            //var categoryExpanded by remember { mutableStateOf(false) }
            //ExposedDropdownMenuBox(
            //    expanded = categoryExpanded,
            //    onExpandedChange = { categoryExpanded = !categoryExpanded },
            //    modifier = Modifier.fillMaxWidth()
            //) {
            //    TextField(
            //        modifier = Modifier
            //            .menuAnchor() // Anchor the dropdown to the TextField
            //            .fillMaxWidth(),
            //        readOnly = true,
            //        value = selectedCategory,
            //        onValueChange = {},
            //        label = { Text("Category") },
            //        trailingIcon = {
            //            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
            //        },
            //        colors = ExposedDropdownMenuDefaults.textFieldColors()
            //    )
            //    ExposedDropdownMenu(
            //        expanded = categoryExpanded,
            //        onDismissRequest = { categoryExpanded = false }
            //    ) {
            //        categories.forEach { category ->
            //            DropdownMenuItem(
            //                text = { Text(category) },
            //                onClick = {
            //                    selectedCategory = category
            //                    categoryExpanded = false
            //                },
            //                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            //            )
            //        }
            //    }
            //}
            Spacer(modifier = Modifier.height(8.dp))

            // Date Selection (opens DatePickerDialog)
            //TextField(
            //    value = selectedDate?.format(androidx.core.i18n.DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
            //    onValueChange = {}, // Value is set by the DatePickerDialog
            //    label = { Text("Date") },
            //    readOnly = true, // Make it read-only so the keyboard doesn't show
            //    trailingIcon = {
            //        Icon(
            //            imageVector = Icons.Default.CalendarToday,
            //            contentDescription = "Select Date",
            //            modifier = Modifier.clickable { showDatePickerDialog = true } // Open dialog on click
            //        )
            //    },
            //    modifier = Modifier.fillMaxWidth()
            //)
            Spacer(modifier = Modifier.height(8.dp))

            // Transaction Type (Radio Buttons or Tabs)
            // Using Radio Buttons for simplicity
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
                            Text(type)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            //// Account Dropdown Menu
            //var accountExpanded by remember { mutableStateOf(false) }
            //ExposedDropdownMenuBox(
            //    expanded = accountExpanded,
            //    onExpandedChange = { accountExpanded = !accountExpanded },
            //    modifier = Modifier.fillMaxWidth()
            //) {
            //    TextField(
            //        modifier = Modifier
            //            .menuAnchor() // Anchor the dropdown to the TextField
            //            .fillMaxWidth(),
            //        readOnly = true,
            //        value = selectedAccount,
            //        onValueChange = {},
            //        label = { Text("Account") },
            //        trailingIcon = {
            //            ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
            //        },
            //        colors = ExposedDropdownMenuDefaults.textFieldColors()
            //    )
            //    ExposedDropdownMenu(
            //        expanded = accountExpanded,
            //        onDismissRequest = { accountExpanded = false }
            //    ) {
            //        accounts.forEach { account ->
            //            DropdownMenuItem(
            //                text = { Text(account) },
            //                onClick = {
            //                    selectedAccount = account
            //                    accountExpanded = false
            //                },
            //                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            //            )
            //        }
            //    }
            //}

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = {
                    // Create Transaction object and pass it back
                    // Valdate input first
                    // val newTransaction = Transaction(...)
                    // onTransactionAdded(newTransaction)
                    onDismiss() // Close the dialog after adding
                }) {
                    Text("Add")
                }
            }
        }
    }

    // DatePickerDialog
    //if (showDatePickerDialog) {
    //    val datePickerState = rememberDatePickerState()
    //    val confirmEnabled = remember {
    //        derivedStateOf { datePickerState.selectedDateMillis != null }
    //    }
    //    DatePickerDialog(
    //        onDismissRequest = {
    //            showDatePickerDialog = false
    //        },
    //        confirmButton = {
    //            TextButton(
    //                onClick = {
    //                    showDatePickerDialog = false
    //                    selectedDate = datePickerState.selectedDateMillis?.let {
    //                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    //                    }
    //                },
    //                enabled = confirmEnabled.value
    //            ) {
    //                Text("OK")
    //            }
    //        },
    //        dismissButton = {
    //            TextButton(
    //                onClick = {
    //                    showDatePickerDialog = false
    //                }
    //            ) {
    //                Text("Cancel")
    //            }
    //        }
    //    ) {
    //        DatePicker(state = datePickerState)
    //    }
    //}
}

@Composable
fun AddObjectiveDialog(onDismiss: () -> Unit, onTransactionAdded: (Transaction) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") } // Or use a dropdown/radio buttons

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields for transaction details
            // For example, using TextField:
            // TextField(
            //     value = description,
            //     onValueChange = { description = it },
            //     label = { Text("Description") }
            // )
            // Spacer(modifier = Modifier.height(8.dp))
            // TextField(
            //     value = amount,
            //     onValueChange = { amount = it },
            //     label = { Text("Amount") },
            //     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            // )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = {
                    // Create Transaction object and pass it back
                    // Valdate input first
                    // val newTransaction = Transaction(...)
                    // onTransactionAdded(newTransaction)
                    onDismiss() // Close the dialog after adding
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun AddLoanDialog(onDismiss: () -> Unit, onTransactionAdded: (Transaction) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") } // Or use a dropdown/radio buttons

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields for transaction details
            // For example, using TextField:
            // TextField(
            //     value = description,
            //     onValueChange = { description = it },
            //     label = { Text("Description") }
            // )
            // Spacer(modifier = Modifier.height(8.dp))
            // TextField(
            //     value = amount,
            //     onValueChange = { amount = it },
            //     label = { Text("Amount") },
            //     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            // )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = {
                    // Create Transaction object and pass it back
                    // Valdate input first
                    // val newTransaction = Transaction(...)
                    // onTransactionAdded(newTransaction)
                    onDismiss() // Close the dialog after adding
                }) {
                    Text("Add")
                }
            }
        }
    }
}