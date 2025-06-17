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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import com.example.budgify.routes.ARG_INITIAL_LOAN_TYPE
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.screen.AddAccountDialog
import com.example.budgify.screen.AddCategoryDialog
import com.example.budgify.screen.items
import com.example.budgify.screen.smallTextStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        "cred_deb_management_screen/{$ARG_INITIAL_LOAN_TYPE}?" -> "Manage Loans"
        "categories_screen" -> "Categories"
        else -> ""
    }
    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 30.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
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
                    Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurface)
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
fun BottomBar(
    navController: NavController,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddObjectiveDialog by remember { mutableStateOf(false) }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var showForceAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    val hasAccounts by viewModel.hasAccounts.collectAsStateWithLifecycle(initialValue = null)

    Box(
        modifier = Modifier
            .padding(bottom = 0.dp)
    ) {
        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.surface, // Explicitly use surface from current theme
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // val desc = ""
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
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface, // Or onSecondaryContainer, if indicator is secondaryContainer
                        selectedTextColor = MaterialTheme.colorScheme.onSurface, // Or onSecondaryContainer
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant, // <<--- THIS IS THE CHANGE
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                viewModel = viewModel ,
                onTransactionAdded = { transaction ->
                    showAddTransactionDialog = false // Dismiss dialog first
                    showSnackbar("Transaction '${transaction.description}' added!") // <--- Call showSnackbar
                }
            )
        }

        // Dialog for adding an objective
        if (showAddObjectiveDialog) {
            AddObjectiveDialog(
                onDismiss = { showAddObjectiveDialog = false },
                viewModel = viewModel,
                onObjectiveAdded = { objective ->
                    showAddObjectiveDialog = false
                    showSnackbar("Objective '${objective.desc}' added!")
                }
            )
        }

        // Dialog for adding a loan
        if (showAddLoanDialog) {
            AddLoanDialog(
                onDismiss = { showAddLoanDialog = false },
                viewModel = viewModel,
                onLoanAdded = { loan ->
                    showAddLoanDialog = false
                    showSnackbar("Loan '${loan.desc}' added!")
                }
            )
        }

        if (showForceAddAccountDialog) {
            AddAccountDialog(
                viewModel = viewModel,
                onDismiss = { showForceAddAccountDialog = false },
                onAccountAdded = { account ->
                    showForceAddAccountDialog = false
                    showSnackbar("Account '${account.title}' added!")
                    showAddTransactionDialog = true
                }
            )
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
    val categoriesForDropdown by viewModel.categoriesForTransactionDialog.collectAsStateWithLifecycle(
        initialValue = emptyList() // Provide an initial empty list
    )
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) } // State for selected category
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) } // State for selected date
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

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
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
                    "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    //modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                XButton(onDismiss)
            }
            Text("Add a transaction to record an expense or income in your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
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
                    value = selectedCategory?.desc ?: "Uncategorized", // Display description or placeholder
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
                    DropdownMenuItem(text = { Text("Uncategorized", style = TextStyle(fontWeight = FontWeight.Bold), fontStyle = FontStyle.Italic) }, onClick = {
                        selectedCategoryId = null
                        selectedCategory = null
                        categoryExpanded = false
                    },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    categoriesForDropdown.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.desc} (${category.type})") },
                            onClick = {
                                selectedCategoryId = category.id // Store the ID
                                selectedCategory = category
                                categoryExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Add New Category...", style = TextStyle(fontWeight = FontWeight.Bold), fontStyle = FontStyle.Italic) },
                        onClick = {
                            categoryExpanded = false // Close the category dropdown
                            showAddCategoryDialog = true // Show the Add Category dialog
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (accounts.isNotEmpty()) {
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
            } else {
                Text(
                    "No accounts available. Please create an account first to save transactions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
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

            if (selectedCategory == null) {
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
            } else {
                selectedType = when (selectedCategory!!.type) {
                    CategoryType.EXPENSE -> TransactionType.EXPENSE
                    CategoryType.INCOME -> TransactionType.INCOME
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
//                Button(onClick = onDismiss) {
//                    Text("Cancel")
//                }
                Button(
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedDate != null && selectedAccountId != null,
                    onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    // Add validation for selectedAccountId, selectedCategoryId, selectedDate, etc.
                    if (description.isNotBlank() && amountDouble != null && selectedDate != null && selectedAccountId != null) {
                        val newTransaction = MyTransaction(
                            accountId = selectedAccountId!!, // Use the selected ID
                            type = selectedType,
                            date = selectedDate!!,
                            description = description,
                            amount = amountDouble,
                            categoryId = selectedCategoryId // Use the selected ID
                        )
                        viewModel.addTransaction(newTransaction)
                        //onDismiss()
                        onTransactionAdded(newTransaction)
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

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            viewModel = viewModel,
            onDismiss = { showAddCategoryDialog = false },
            initialType = null,
            onCategoryAdded = { newCategory ->
                selectedCategoryId = newCategory.id // Update the state in AddTransactionDialog
//                selectedCategory = newCategory // Update the state in AddTransactionDialog
                showAddCategoryDialog = false
            }
        )
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
    // var type by remember { mutableStateOf("Expense") } // Or use a dropdown/radio buttons
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) } // State for selected date
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Objective", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }
            Text("Add an objective to record a sum of money you want to obtain or spend in the future.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

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
                value = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "", // Format the selected date for display
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
                horizontalArrangement = Arrangement.Center
            ) {
//                Button(onClick = onDismiss) {
//                    Text("Cancel")
//                }
                Button(
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedDate != null,
                    onClick = {
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
                        //onDismiss() // Close the dialog after adding
                        onObjectiveAdded(newObjective)
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }

    if (showDatePickerDialog) {
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
fun AddLoanDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel,
    onLoanAdded: (Loan) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) } // Opzionale
    var selectedType by remember { mutableStateOf(LoanType.DEBT) }
    val loanTypes = LoanType.entries.toList()

    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    var accountExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId }
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Loan", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }
            Text("Add a loan to record a credit or a debit you have contracted to your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank() && showErrorDialog // Mostra errore solo se si tenta di salvare e il campo è vuoto
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                // L'errore sull'amount viene gestito nella logica del pulsante, non direttamente qui per isError,
                // a meno che non si voglia un feedback immediato più complesso.
                // isError = (amount.isBlank() || amount.replace(',', '.').toDoubleOrNull() == null || amount.replace(',', '.').toDoubleOrNull()!! <= 0) && showErrorDialog
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (accounts.isNotEmpty()) {
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
                        label = { Text("Account") }, // Label for clarity
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        isError = selectedAccountId == null && showErrorDialog // Error if no account selected and trying to save
                    )

                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.title} (${account.amount}€)") },
                                onClick = {
                                    selectedAccountId = account.id // Store the ID
                                    accountExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No accounts available. Please create an account first to associate with this loan's transaction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = selectedStartDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {}, // ReadOnly
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

            TextField(
                value = selectedEndDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Not set",
                onValueChange = {}, // ReadOnly
                label = { Text("End Date (Optional)") },
                readOnly = true,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedEndDate != null) {
                            IconButton(onClick = { selectedEndDate = null }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear End Date"
                                )
                            }
                        }
                        IconButton(onClick = {
                            datePickerTarget = "END"
                            showDatePickerDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select End Date"
                            )
                        }
                    }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center // O Arrangement.End per allineare i pulsanti a destra
            ) {
                Button(
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedStartDate != null && selectedAccountId != null && accounts.isNotEmpty(),
                    onClick = {
                        if (description.isBlank()) {
                            triggerError("Description cannot be empty.")
                            return@Button
                        }

                        val sanitizedAmount = amount.replace(',', '.') // Sostituisci la virgola
                        val amountDouble = sanitizedAmount.toDoubleOrNull()

                        if (amountDouble == null || amountDouble <= 0) {
                            triggerError("Please enter a valid amount greater than zero.")
                            return@Button
                        }

                        if (selectedAccountId == null) {
                            triggerError("Please select an account to associate with this loan's transaction.")
                            return@Button
                        }

                        // selectedAccount non dovrebbe essere null qui grazie al check precedente
                        val currentAccount = selectedAccount!!


                        if (selectedType == LoanType.CREDIT && amountDouble > currentAccount.amount) {
                            errorMessage = "The selected account '${currentAccount.title}' does not have enough balance to grant this credit.\n\n" +
                                    "Required: $amountDouble €\n" +
                                    "Available in '${currentAccount.title}': ${currentAccount.amount} €\n\n" +
                                    "Please choose another account or add funds to this one."
                            showInsufficientBalanceDialog = true // Mostra il dialogo di saldo insufficiente
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

                        val newLoan = Loan(
                            desc = description,
                            amount = amountDouble, // Usa l'amount convertito
                            type = selectedType,
                            startDate = selectedStartDate!!,
                            endDate = selectedEndDate
                        )
                        viewModel.addLoan(newLoan, selectedAccountId!!)
                        onLoanAdded(newLoan) // Callback per notificare l'aggiunta
                        onDismiss() // Chiudi il dialogo
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }

    // Dialogo di errore per Saldo Insufficiente
    if (showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = {
                showInsufficientBalanceDialog = false
                // Non resettare altri campi, l'utente potrebbe voler cambiare solo l'account o l'importo
            },
            title = { Text("Insufficient Balance") },
            text = { Text(errorMessage) }, // L'errorMessage è già stato impostato con i dettagli
            confirmButton = {
                TextButton(onClick = {
                    showInsufficientBalanceDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDatePickerDialog) {
        val initialDateForPicker = when (datePickerTarget) {
            "START" -> selectedStartDate ?: LocalDate.now()
            "END" -> selectedEndDate ?: selectedStartDate ?: LocalDate.now()
            else -> LocalDate.now()
        }
        val initialDateMillis = initialDateForPicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = {
                showDatePickerDialog = false
                datePickerTarget = null
            },
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
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerTarget = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Validation Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun XButton(onDismiss: () -> Unit) {
    IconButton(
        onClick = onDismiss,
        modifier = Modifier
            .size(24.dp) // Set a fixed size for the IconButton
            .clip(CircleShape) // Clip the IconButton to a circle shape
            .background(MaterialTheme.colorScheme.surfaceContainerHighest) // Add a background color to the circle
    ) { // X button
        Icon(Icons.Filled.Close, contentDescription = "Close")
    }
}