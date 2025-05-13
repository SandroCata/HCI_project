package com.example.budgify.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.screen.items
import com.example.budgify.screen.smallTextStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, currentRoute: String) {
    val title = when (currentRoute) {
        "home_screen" -> "Dashboard"
        "objectives_screen" -> "Objectives"
        "objectives_management_screen" -> "Manage Objectives"
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
                Icon( Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurface)
            }

        }
    )
}

@Composable
fun BottomBar(navController: NavController, viewModel: FinanceViewModel) {
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
            AddTransactionDialog(
                onDismiss = { showAddTransactionDialog = false },
                viewModel = viewModel
            ) {
                // Handle transaction added
            }
        }

        // Dialog for adding an objective
        if (showAddObjectiveDialog) {
            AddObjectiveDialog(
                onDismiss = { showAddObjectiveDialog = false },
                viewModel = viewModel
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onTransactionAdded: (MyTransaction) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) } // State for selected category
    var selectedCategory = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.id == selectedCategoryId }
    }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) } // State for selected date
    var selectedType by remember { mutableStateOf<TransactionType>(TransactionType.EXPENSE) } // State for transaction type (Expense/Income)
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    var accountExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) } // State for selected account
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId }
    }
    val transactionTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME)

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
                value = selectedDate?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
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
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    // Add validation for selectedAccountId, selectedCategoryId, selectedDate, etc.
                    if (description.isNotBlank() && amountDouble != null && selectedDate != null && selectedAccountId != null && selectedCategoryId != null) {
                        val newTransaction = MyTransaction(
                            accountId = selectedAccountId!!, // Use the selected ID
                            type = selectedType,
                            date = selectedDate!!,
                            description = description,
                            amount = amountDouble,
                            categoryId = selectedCategoryId // Use the selected ID
                        )
                        viewModel.addTransaction(newTransaction)
                        onDismiss()
                    } else {
                        // Show validation error to the user
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }


    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
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
                        // Convert the selected date from milliseconds to LocalDate
                        selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                    },
                    enabled = confirmEnabled.value // Enable OK button only if a date is selected
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObjectiveDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel,
    onObjectiveAdded: (Objective) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") } // Or use a dropdown/radio buttons
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) } // State for selected date
    var selectedType by remember { mutableStateOf(ObjectiveType.EXPENSE) } // State for objective type (Expense/Income)
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val objectiveTypes = ObjectiveType.entries.toList()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Add Objective", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

             // Input fields for transaction details
             // For example, using TextField:
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

            // Date Selection TextField (opens DatePickerDialog)
            TextField(
                value = selectedDate?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "", // Format the selected date for display
                onValueChange = {}, // Value is set by the DatePickerDialog
                label = { Text("Date") },
                readOnly = true, // Make it read-only so the keyboard doesn't show
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { showDatePickerDialog = true } // Open dialog on click
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Objective Type (Radio Buttons)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // Distribute radio buttons horizontally
                ) {
                    objectiveTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                selectedType = type
                            } // Allow clicking the whole Row
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type } // Update selectedType on click
                            )
                            Text(type.name) // Display the enum name (e.g., "EXPENSE", "INCOME")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = {
                    // **Validation (Add your validation logic here)**
                    val amountDouble = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountDouble != null && selectedDate != null) {
                        // Create Objective object
                        val newObjective = Objective(
                            id = 0, // Room will auto-generate the ID
                            type = selectedType,
                            desc = description,
                            amount = amountDouble,
                            startDate = LocalDate.now(),
                            endDate = selectedDate!!
                        )

                        // Insert objective using the ViewModel
                        viewModel.addObjective(newObjective)

                        onDismiss() // Close the dialog after adding
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
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
                        // Convert the selected date from milliseconds to LocalDate
                        selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                    },
                    enabled = confirmEnabled.value // Enable OK button only if a date is selected
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

@Composable
fun AddLoanDialog(onDismiss: () -> Unit, onTransactionAdded: (MyTransaction) -> Unit) {
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