package com.example.budgify.screen

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
    var balancesVisible by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply innerPadding to the Box
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                //.padding(innerPadding)
            ) {
                // Box per i pie chart e istogramma
                item {
                    GraficiBox(viewModel = viewModel,
                            showSnackbar = showSnackbar)
                }

                // Box per i conti e il saldo totale
                item {
                    ContiBox(
                        viewModel,
                        balancesVisible = balancesVisible,
                        onToggleBalanceVisibility = { balancesVisible = !balancesVisible },
                        showSnackbar = { message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }

                        }
                    )
                }

                item {
                    LastTransactionBox(
                        viewModel = viewModel,
                        showSnackbar = showSnackbar
                    )
                }
            }
//            CustomVerticalLazyListScrollbar(
//                        lazyListState = lazyListState,
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(8.dp) // Width of the scrollbar
//                    .align(Alignment.CenterEnd) // Position it to the right
//                    .padding(vertical = 4.dp) // Optional vertical padding for the scrollbar itself
//            )
        }
    }
}

@Composable
fun CustomVerticalLazyListScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
) {
    val layoutInfo = lazyListState.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    val visibleItemsInfo = layoutInfo.visibleItemsInfo

    if (totalItemsCount == 0 || visibleItemsInfo.isEmpty()) {
        return // No items or no visible items, no scrollbar needed
    }

    // Calculate if scrolling is possible
    val firstVisibleItem = visibleItemsInfo.first()
    val lastVisibleItem = visibleItemsInfo.last()

    // Estimate total content height and viewport height
    // This is an estimation because item heights can vary.
    val averageItemHeight = visibleItemsInfo.sumOf { it.size } / visibleItemsInfo.size.toFloat()
    val estimatedTotalContentHeight = totalItemsCount * averageItemHeight
    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset.toFloat()

    if (estimatedTotalContentHeight <= viewportHeight) {
        return // Content fits within viewport, no scrollbar needed
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val maxHeightPx = constraints.maxHeight.toFloat() // Height of the scrollbar track area

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(trackColor, RoundedCornerShape(4.dp))
        ) {
            // Calculate thumb size and position
            val thumbSizeRatio = (viewportHeight / estimatedTotalContentHeight)
                .coerceIn(0.05f, 1f) // Ensure thumb has a minimum size
            val minThumbHeightPx = with(density) { 16.dp.toPx() }
            val thumbHeightPx = (thumbSizeRatio * maxHeightPx).coerceAtLeast(minThumbHeightPx)

            val totalScrollableRange = (estimatedTotalContentHeight - viewportHeight).coerceAtLeast(0.01f)
            val scrolledPx = (firstVisibleItem.index * averageItemHeight) + (-firstVisibleItem.offset)

            // Calculate current scroll progress
            // firstVisibleItemScrollOffset is negative or zero.
            // It's the offset of the first visible item from the start of the viewport.
            // val scrolledThroughItemsHeight = firstVisibleItem.index * averageItemHeight + (-firstVisibleItem.offset)
            val scrollProgress = (scrolledPx / totalScrollableRange).coerceIn(0f, 1f)

            val availableTrackSpaceForThumb = maxHeightPx - thumbHeightPx
            val thumbOffsetYPx = scrollProgress * availableTrackSpaceForThumb

            if (thumbHeightPx > 0f && thumbHeightPx < maxHeightPx) { // Ensure thumb is drawable and smaller than track
                // Ensure thumbOffsetYPx does not cause the thumb to go out of bounds
                val finalThumbOffsetYPx = thumbOffsetYPx.coerceIn(0f, maxHeightPx - thumbHeightPx)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { thumbHeightPx.toDp() })
                        .align(Alignment.TopStart)
                        .padding(top = with(density) { finalThumbOffsetYPx.toDp() }) // Use coerced value
                        .background(thumbColor, RoundedCornerShape(4.dp))
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
    onClick: (MyTransaction) -> Unit,
    onLongClick: (MyTransaction) -> Unit // This will now trigger the action choice dialog
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
                    onClick(myTransaction)
                },
                onLongClick = {
                    onLongClick(myTransaction) // Call the lambda on long click
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Modifica formattedDescription2 per includere l'importo
            val amountText = "${if (myTransaction.type == TransactionType.INCOME) "+" else "-"} ${myTransaction.amount}€"
            val amountColor = if (myTransaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.9f, green = 0.0f, blue = 0.0f)

            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(color = amountColor, fontWeight = FontWeight.Bold)) {
                    append(amountText)
                }
                append("  ") // Aggiungi spazio dopo l'importo
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
                style = MaterialTheme.typography.bodyMedium
            )
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
        }
        /*
        Text(
            text = "${if (myTransaction.type == TransactionType.INCOME) "+" else "-"} ${myTransaction.amount}€",
            color = if (myTransaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )*/
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun LastTransactionBox(
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    val transactionsWithDetails by viewModel.allTransactionsWithDetails.collectAsStateWithLifecycle()
    var transactionToAction by remember { mutableStateOf<MyTransaction?>(null) } // For action choice
    var showTransactionActionChoiceDialog by remember { mutableStateOf(false) }
    var showEditTransactionDialog by remember { mutableStateOf(false) }
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
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Latest Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Hold on a transaction to manage it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (transactionsWithDetails.isEmpty()) {
                Text("No transactions found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    transactionsWithDetails.takeLast(5).reversed()
                        .forEach { transactionWithDetails ->
                            TransactionItem(
                                transactionWithDetails = transactionWithDetails,
                                onClick = { transaction ->
                                    showSnackbar("Hold to edit or delete the transaction")
                                },
                                onLongClick = { transaction ->
                                    transactionToAction = transaction
                                    showTransactionActionChoiceDialog = true
                                }
                            )
                        }
                }
            }
        }
    }

    // Transaction Action Choice Dialog
    if (showTransactionActionChoiceDialog && transactionToAction != null) {
        AlertDialog(
            onDismissRequest = {
                showTransactionActionChoiceDialog = false
                transactionToAction = null
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "'${transactionToAction?.description}'",
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
            text = { Text("What would you like to do with this transaction?") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            showEditTransactionDialog = true
                            showTransactionActionChoiceDialog = false
                            // transactionToAction remains set for the edit dialog
                        }
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = {
                            showDeleteTransactionConfirmationDialog = true
                            showTransactionActionChoiceDialog = false
                            // transactionToAction remains set for the delete confirmation
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = null
        )
    }


    // Show the Edit Transaction Dialog
    if (showEditTransactionDialog && transactionToAction != null) {
        EditTransactionDialog(
            transaction = transactionToAction!!,
            viewModel = viewModel,
            onDismiss = {
                showEditTransactionDialog = false
                transactionToAction = null
            }
        )
    }

    // Delete Transaction Confirmation Dialog
    if (showDeleteTransactionConfirmationDialog && transactionToAction != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteTransactionConfirmationDialog = false
                // Keep transactionToAction if you want to potentially go back to action choice,
                // or nullify it if the flow always ends here. For simplicity, let's nullify.
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
                            transactionToAction = null // Clear after deletion
                            showSnackbar("Transaction deleted")
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteTransactionConfirmationDialog = false
                        // transactionToAction = null // Optionally clear here too
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
fun EditTransactionDialog(
    transaction: MyTransaction, // The transaction to be edited
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    // Use the existing transaction data as initial state for editing
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString().replace('.', ',')) } // Use comma for display
    val categories by viewModel.categoriesForTransactionDialog.collectAsStateWithLifecycle(initialValue = emptyList())
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
    var isOriginalCategoryDefault by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check if the initial category is a default one
    LaunchedEffect(key1 = transaction.categoryId) {
        if (transaction.categoryId != null) {
            isOriginalCategoryDefault = viewModel.isDefaultCategory(transaction.categoryId)
        } else {
            isOriginalCategoryDefault = false
        }
    }
    val allCategoriesFromVm by viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

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

            if (!isOriginalCategoryDefault) {
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
                            text = {
                                Text(
                                    "Uncategorized",
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
                            },
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
            } else {
                // Display the current default category as non-editable text
                val originalCategoryName = remember(allCategoriesFromVm, transaction.categoryId) {
                    allCategoriesFromVm.firstOrNull { it.id == transaction.categoryId }?.desc ?: "System Category"
                }
                TextField(
                    value = originalCategoryName,
                    onValueChange = {},
                    label = { Text("Category (System)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
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
                horizontalArrangement = Arrangement.Center // Align save button to the end
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

// Composbale per visualizzare la box dei conti
@Composable
fun ContiBox(
    viewModel: FinanceViewModel,
    balancesVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    showSnackbar: (String) -> Unit
) {

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
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleLarge,
                //modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row( // Row for "Total Balance" and the visibility toggle icon
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (balancesVisible) "Total Balance: $totalBalance €" else "Total Balance: **** €",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onToggleBalanceVisibility) { // <-- NEW: Toggle Icon
                    Icon(
                        imageVector = if (balancesVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (balancesVisible) "Hide balances" else "Show balances"
                    )
                }
            }
            Text(
                text = "Hold on an account to manage it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (accounts.isEmpty()) {
                Text(
                    text = "No accounts found. Tap the '+' button to add a new one!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            //Sezione scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Iterate through the collected accounts
                accounts.forEach { account ->
                    AccountItem(
                        account = account,
                        viewModel = viewModel,
                        balancesVisible = balancesVisible,
                        showSnackbar = showSnackbar
                    )
                }
                // Aggiungi l'item con il "+"
                AddAccountItem(
                    viewModel = viewModel,
                    showSnackbar = showSnackbar
                ) // Pass ViewModel to AddAccountItem
            }
        }
    }
}

@Composable
fun AddAccountItem(
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
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
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add Account",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Show the Add Account dialog if showAddAccountDialog is true
    if (showAddAccountDialog) {
        AddAccountDialog(
            viewModel = viewModel,
            onDismiss = { showAddAccountDialog = false },
            onAccountAdded = { account ->
                showAddAccountDialog = false
                showSnackbar("Account '${account.title}' added!")
            }
        )
    }
}

//Composbale per visualizzare le sezioni di ogni account
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    account: Account,
    viewModel: FinanceViewModel,
    balancesVisible: Boolean,
    showSnackbar: (String) -> Unit
) {
    // var isLongPressed by remember { mutableStateOf(false) } // Non più necessario direttamente per l'icona
    var showActionChoiceDialog by remember { mutableStateOf(false) } // Nuovo: per il dialogo di scelta
    var showEditAccountDialog by remember { mutableStateOf(false) }  // Nuovo: per il dialogo di modifica
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier
        .padding(8.dp)
        .width(150.dp)
        .height(65.dp)
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            onClick = {
                // Puoi mantenere lo snackbar o rimuoverlo se il long press ha un'azione chiara
                showSnackbar("Hold to edit or delete the account")
            },
            onLongClick = {
                showActionChoiceDialog = true // Mostra il dialogo di scelta azione
            }
        )) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Usa fillMaxSize per occupare tutto lo spazio del Box
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = account.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = if (balancesVisible) "${account.amount}€" else "**** €", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // L'icona di eliminazione non è più mostrata direttamente qui,
        // la scelta avverrà tramite il dialogo.
        // if (isLongPressed) { ... }
    }

    // Dialogo di Scelta Azione (Nuovo)
    if (showActionChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showActionChoiceDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "'${account.title}'",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XButton({ showActionChoiceDialog = false })
                }},
            text = { Text("What would you like to do with this account?") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // O Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showEditAccountDialog = true
                            showActionChoiceDialog = false
                        }
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = {
                            showDeleteConfirmationDialog = true
                            showActionChoiceDialog = false
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = null // O un TextButton per "Cancel" se preferisci separarlo
        )
    }


    // Dialogo di Conferma Eliminazione (esistente, ma ora chiamato da showActionChoiceDialog)
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the account \"${account.title}\"?\nAll transactions related to this account will also be deleted") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteAccount(account)
                            showDeleteConfirmationDialog = false
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialogo di Modifica Account (Nuovo)
    if (showEditAccountDialog) {
        EditAccountDialog(
            accountToEdit = account,
            viewModel = viewModel,
            onDismiss = { showEditAccountDialog = false },
            onAccountUpdated = { updatedAccount ->
                showEditAccountDialog = false
                showSnackbar("Account '${updatedAccount.title}' updated!")
            }
        )
    }
}

@Composable
fun AddAccountDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onAccountAdded: (Account) -> Unit
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
                Text(
                    "Add New Account",
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
                label = { Text("Balance") },
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
                            onAccountAdded(newAccount)
                            //onDismiss() // Close the dialog
                        } else {
                            Log.d("AddAccountDialog", "Validation failed")
                        }
                    }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun EditAccountDialog(
    accountToEdit: Account,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onAccountUpdated: (Account) -> Unit // Potresti anche rimuovere il parametro Account qui se non lo usi più
) {
    var accountTitle by remember { mutableStateOf(accountToEdit.title) }

    // Mostra l'amount ATTUALE nel TextField, ma ricorda l'initialAmount originale
    // per calcolare la DIFFERENZA che l'utente vuole applicare all'initialAmount.
    var currentBalanceDisplayString by remember {
        mutableStateOf(accountToEdit.amount.toString().replace('.', ','))
    }
    val originalInitialAmount = accountToEdit.initialAmount // Conserva l'initialAmount originale

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
                Text("Edit Account", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = accountTitle,
                onValueChange = { accountTitle = it },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = accountTitle.isBlank() && errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            // L'utente vede e modifica questo campo, che inizialmente mostra l'AMOUNT attuale
            TextField(
                value = currentBalanceDisplayString,
                onValueChange = { currentBalanceDisplayString = it },
                label = { Text("Balance") }, // Etichetta cambiata per chiarezza
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = currentBalanceDisplayString.replace(',', '.').toDoubleOrNull() == null && errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        errorMessage = null
                        if (accountTitle.isBlank()) {
                            errorMessage = "Account name cannot be empty."
                            return@Button
                        }

                        val newDisplayedBalanceDouble = currentBalanceDisplayString.replace(',', '.').toDoubleOrNull()

                        if (newDisplayedBalanceDouble == null) {
                            errorMessage = "Please enter a valid balance."
                            return@Button
                        }

                        // Calcola la differenza tra il saldo attuale originale e quello nuovo visualizzato
                        // Questo delta verrà applicato all'initialAmount originale.
                        val balanceDifference = newDisplayedBalanceDouble - accountToEdit.amount
                        val newCalculatedInitialAmount = originalInitialAmount + balanceDifference

                        coroutineScope.launch {
                            val success = viewModel.updateAccountAndRecalculateBalance(
                                accountId = accountToEdit.id,
                                newTitle = accountTitle,
                                newInitialAmount = newCalculatedInitialAmount // Passa il NUOVO initialAmount calcolato
                            )
                            if (success) {
                                // La UI si aggiornerà tramite il Flow.
                                // onAccountUpdated è ancora utile per lo snackbar/chiusura.
                                // Passiamo dati indicativi, poiché il vero aggiornamento è nel ViewModel.
                                onAccountUpdated(accountToEdit.copy(title = accountTitle, initialAmount = newCalculatedInitialAmount, amount = newDisplayedBalanceDouble))
                                onDismiss()
                            } else {
                                errorMessage = "Failed to update account. Please try again."
                            }
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

// Define colors for pie chart slices
val pieChartColorsDefaults = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
    Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B)
)
val incomeChartColors = listOf(
    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFFEB3B),
    Color(0xFF009688), Color(0xFF03A9F4), Color(0xFF2196F3), Color(0xFF00BCD4),
    Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF9C27B0), Color(0xFFE91E63) // Added more diverse colors
)
val expenseChartColors = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF5722),
    Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF03A9F4) // Added more diverse colors
)


data class PieSlice(val categoryName: String, val amount: Double, val color: Color)

enum class ChartType {
    PIE,
    HISTOGRAM
}

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    slices: List<PieSlice>
) {
    if (slices.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    val totalAmount = slices.sumOf { it.amount }
    if (totalAmount == 0.0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    var startAngle = -90f

    Canvas(modifier = modifier) {
        slices.forEach { slice ->
            val sweepAngle = (slice.amount / totalAmount * 360f).toFloat()
            if (sweepAngle > 0f) {
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                )
            }
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryDistributionPieChart(
    title: String,
    transactionType: TransactionType,
    account: Account,
    allTransactions: List<TransactionWithDetails>,
    colors: List<Color>,
    legendItemLimit: Int? = 3 // NEW: Parameter to control legend items
) {
    // Re-calculate the data map here to include uncategorized transactions.
    val categoryDataMap = remember(allTransactions, account, transactionType) {
        allTransactions
            .filter { it.transaction.accountId == account.id && it.transaction.type == transactionType }
            .groupBy { it.category?.desc ?: "Uncategorized" } // Group by description, use "Uncategorized" for null.
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (categoryDataMap.isEmpty()) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No ${transactionType.name.lowercase()} data",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Sort the map entries by amount in descending order.
            val sortedEntries = categoryDataMap.entries.sortedByDescending { it.value }

            val pieSlices = sortedEntries.mapIndexedNotNull { index, entry ->
                if (entry.value > 0) {
                    PieSlice(
                        categoryName = entry.key, // The key is now the category name String.
                        amount = entry.value,
                        color = colors[index % colors.size]
                    )
                } else null
            }

            if (pieSlices.isEmpty()){
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${transactionType.name.lowercase()} to display",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                PieChart(
                    modifier = Modifier
                        .size(90.dp),
                    slices = pieSlices
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Use the new reusable legend composable
                ChartLegend(
                    items = pieSlices.map { it.categoryName to it.amount },
                    colors = pieSlices.map { it.color },
                    limit = legendItemLimit
                )
            }
        }
    }
}

// NEW: Reusable Legend Composable
@Composable
fun ChartLegend(
    items: List<Pair<String, Double>>,
    colors: List<Color>,
    limit: Int?
) {
    val itemsToShow = if (limit != null) items.take(limit) else items
    val colorsToShow = if (limit != null) colors.take(limit) else colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        itemsToShow.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors[index], RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.first}: ${"%.2f".format(item.second)}€",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (limit != null && items.size > limit) {
            Text(
                text = "+ ${items.size - limit} more...",
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GraficiBox(viewModel: FinanceViewModel,
               showSnackbar: (String) -> Unit) {
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allTransactionsWithDetails by viewModel.allTransactionsWithDetails.collectAsStateWithLifecycle()

    // State for toggling between Pie and Histogram
    var currentChartType by remember { mutableStateOf(ChartType.PIE) }
    // NEW: State for toggling between Expenses and Incomes
    var displayedTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    // NEW: State for the detail dialog
    var showChartDetailDialog by remember { mutableStateOf(false) }
    var selectedAccountForDetail by remember { mutableStateOf<Account?>(null) }


    val accountsWithTransactions = remember(allAccounts, allTransactionsWithDetails) {
        allAccounts.filter { account ->
            allTransactionsWithDetails.any { transactionDetail ->
                transactionDetail.transaction.accountId == account.id
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "Accounts Overview",
                    style = MaterialTheme.typography.titleLarge,
                )


                // Container for the toggle buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 4.dp)
                ) {
                    // Button to toggle between Expense/Income
                    IconButton(onClick = {
                        displayedTransactionType = if (displayedTransactionType == TransactionType.EXPENSE) TransactionType.INCOME else TransactionType.EXPENSE
                    }) {
                        Icon(
                            imageVector = if (displayedTransactionType == TransactionType.EXPENSE) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                            contentDescription = "Toggle Expense/Income View",
                            tint = if (displayedTransactionType == TransactionType.EXPENSE) expenseChartColors[0] else incomeChartColors[0]
                        )
                    }

                    // Button to toggle between Pie/Histogram
                    IconButton(onClick = {
                        currentChartType = if (currentChartType == ChartType.PIE) ChartType.HISTOGRAM else ChartType.PIE
                    }) {
                        Icon(
                            imageVector = if (currentChartType == ChartType.PIE) Icons.Filled.PieChart else Icons.Filled.BarChart,
                            contentDescription = "Toggle Chart Type",
                        )
                    }
                }
            }
            Text(
                text = "Hold on a chart to expand",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (accountsWithTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (allAccounts.isEmpty()) "No accounts yet. Add an account to see charts."
                        else "No accounts with transactions to display."
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    accountsWithTransactions.forEach { account ->
                        SingleAccountChartsCard(
                            account = account,
                            chartType = currentChartType,
                            displayedTransactionType = displayedTransactionType,
                            allTransactions = allTransactionsWithDetails,
                            showSnackbar = showSnackbar,
                            onLongClick = {
                                selectedAccountForDetail = account
                                showChartDetailDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // NEW: Show the detail dialog when triggered
    if (showChartDetailDialog && selectedAccountForDetail != null) {
        ChartDetailDialog(
            account = selectedAccountForDetail!!,
            chartType = currentChartType,
            transactionType = displayedTransactionType,
            allTransactions = allTransactionsWithDetails,
            onDismiss = { showChartDetailDialog = false }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleAccountChartsCard(
    account: Account,
    chartType: ChartType,
    displayedTransactionType: TransactionType,
    allTransactions: List<TransactionWithDetails>,
    onLongClick: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(250.dp) // Made the card narrower as it shows one chart now
            .padding(start = 0.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .combinedClickable(
                onClick = {
                    showSnackbar("Hold on the chart to expand")
                }, // Can add a snackbar here if needed
                onLongClick = onLongClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = account.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Show only one chart based on the displayedTransactionType
        when (chartType) {
            ChartType.PIE -> CategoryDistributionPieChart(
                title = if (displayedTransactionType == TransactionType.EXPENSE) "Expenses" else "Incomes",
                transactionType = displayedTransactionType,
                account = account,
                allTransactions = allTransactions,
                colors = if (displayedTransactionType == TransactionType.EXPENSE) expenseChartColors else incomeChartColors,
                legendItemLimit = 3
            )
            ChartType.HISTOGRAM -> CategoryDistributionHistogramChart(
                title = if (displayedTransactionType == TransactionType.EXPENSE) "Expenses" else "Incomes",
                transactionType = displayedTransactionType,
                account = account,
                allTransactions = allTransactions,
                colors = if (displayedTransactionType == TransactionType.EXPENSE) expenseChartColors else incomeChartColors,
                legendItemLimit = 3
            )
        }
    }
}

// Composable for the detailed chart dialog
@Composable
fun ChartDetailDialog(
    account: Account,
    chartType: ChartType,
    transactionType: TransactionType,
    allTransactions: List<TransactionWithDetails>,
    onDismiss: () -> Unit
) {
    val categoryDataMap = remember(allTransactions, account, transactionType) {
        allTransactions
            .filter { it.transaction.accountId == account.id && it.transaction.type == transactionType }
            .groupBy { it.category?.desc ?: "Uncategorized" }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
            .entries.sortedByDescending { it.value } // Already sorted
    }

    val colors = if (transactionType == TransactionType.EXPENSE) expenseChartColors else incomeChartColors

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CHANGED: Title now correctly shows "Expenses" or "Incomes"
                val typeLabel = if (transactionType == TransactionType.EXPENSE) "Expenses" else "Incomes"
                Text(
                    text = "${account.title}: $typeLabel",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                XButton(onDismiss)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (categoryDataMap.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data to display.")
                }
            } else {
                // Chart Display
                when (chartType) {
                    ChartType.PIE -> {
                        val pieSlices = categoryDataMap.mapIndexedNotNull { index, entry ->
                            if (entry.value > 0) PieSlice(entry.key, entry.value, colors[index % colors.size]) else null
                        }
                        PieChart(modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterHorizontally), slices = pieSlices)
                    }
                    ChartType.HISTOGRAM -> {
                        val histogramBars = categoryDataMap.mapIndexedNotNull { index, entry ->
                            if (entry.value > 0) HistogramBarData(entry.key, entry.value, colors[index % colors.size]) else null
                        }
                        HistogramChart(
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth(),
                            bars = histogramBars,
                            showValuesOnBars = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Full Legend in a LazyColumn
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    val legendItems = categoryDataMap.map { it.key to it.value }
                    val legendColors = categoryDataMap.mapIndexed { index, _ -> colors[index % colors.size] }

                    items(legendItems.size) { index ->
                        val item = legendItems[index]
                        val color = legendColors[index]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.first}: ${"%.2f".format(item.second)}€",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


data class HistogramBarData(val categoryName: String, val amount: Double, val color: Color)


@Composable
fun CategoryDistributionHistogramChart(
    title: String,
    transactionType: TransactionType,
    account: Account,
    allTransactions: List<TransactionWithDetails>,
    colors: List<Color>,
    legendItemLimit: Int? = 3 // NEW: Parameter to control legend items
) {
    // Re-calculate the data map here to include uncategorized transactions.
    val categoryDataMap = remember(allTransactions, account, transactionType) {
        allTransactions
            .filter { it.transaction.accountId == account.id && it.transaction.type == transactionType }
            .groupBy { it.category?.desc ?: "Uncategorized" } // Group by description, use "Uncategorized" for null.
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (categoryDataMap.isEmpty()) {
            Box(
                modifier = Modifier
                    .height(90.dp) // Keep consistent height
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No ${transactionType.name.lowercase()} data",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Sort the map entries by amount in descending order.
            val sortedEntries = categoryDataMap.entries.sortedByDescending { it.value }

            val histogramBars = sortedEntries.mapIndexedNotNull { index, entry ->
                if (entry.value > 0) { // Only include bars with a positive amount
                    HistogramBarData(
                        categoryName = entry.key, // The key is now the category name String.
                        amount = entry.value,
                        color = colors[index % colors.size] // Cycle through predefined colors
                    )
                } else null
            }

            if (histogramBars.isEmpty()){
                Box(
                    modifier = Modifier
                        .height(90.dp) // Keep consistent height
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${transactionType.name.lowercase()} to display",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                HistogramChart( // Call the new generic Histogram composable
                    modifier = Modifier
                        .height(90.dp) // Define height for the histogram area
                        .fillMaxWidth(),
                    bars = histogramBars,
                    showValuesOnBars = false // Optionally turn this on
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Use the new reusable legend composable
                ChartLegend(
                    items = histogramBars.map { it.categoryName to it.amount },
                    colors = histogramBars.map { it.color },
                    limit = legendItemLimit
                )
            }
        }
    }
}

@Composable
fun HistogramChart(
    modifier: Modifier = Modifier,
    bars: List<HistogramBarData>,
    barWidthToSpacingRatio: Float = 2f, // For multi-bar dynamic layout
    singleBarFixedWidth: Dp = 50.dp,   // For the single-bar case
    showValuesOnBars: Boolean = true,
    valueTextSizeSp: Float = 9f
) {
    if (bars.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data for histogram", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    val maxAmount = bars.maxOfOrNull { it.amount } ?: 0.0
    if (maxAmount == 0.0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Amounts are zero", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    val density = LocalDensity.current
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = density.run { valueTextSizeSp.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val numberOfBars = bars.size

        // HYBRID LOGIC: Check the number of bars
        if (numberOfBars == 1) {
            // --- CASE 1: Only one bar ---
            // Use a fixed width and center it.
            val bar = bars.first()
            val barWidthPx = singleBarFixedWidth.toPx()
            val startX = (size.width - barWidthPx) / 2 // Center the bar
            val barHeightPx = (bar.amount / maxAmount * size.height).toFloat()

            if (barHeightPx > 0f) {
                drawRect(
                    color = bar.color,
                    topLeft = Offset(x = startX, y = size.height - barHeightPx),
                    size = Size(width = barWidthPx, height = barHeightPx)
                )

                if (showValuesOnBars && bar.amount > 0) {
                    val valueText = "%.0f".format(bar.amount)
                    val textY = size.height - barHeightPx - 4.dp.toPx()
                    val textX = startX + barWidthPx / 2
                    if (textY > textPaint.textSize) {
                        drawContext.canvas.nativeCanvas.drawText(valueText, textX, textY, textPaint)
                    }
                }
            }
        } else {
            // --- CASE 2: Multiple bars ---
            // Use the original dynamic spacing logic.
            val totalSlotUnits = barWidthToSpacingRatio + 1
            val slotWidthPx = size.width / (numberOfBars * totalSlotUnits - 1)
            val barActualWidthPx = slotWidthPx * barWidthToSpacingRatio
            val spacingActualPx = slotWidthPx
            var currentX = 0f

            bars.forEach { bar ->
                val barHeightPx = (bar.amount / maxAmount * size.height).toFloat()
                if (barHeightPx > 0f) {
                    drawRect(
                        color = bar.color,
                        topLeft = Offset(x = currentX, y = size.height - barHeightPx),
                        size = Size(width = barActualWidthPx, height = barHeightPx)
                    )

                    if (showValuesOnBars && bar.amount > 0) {
                        val valueText = "%.0f".format(bar.amount)
                        val textY = size.height - barHeightPx - 4.dp.toPx()
                        val textX = currentX + barActualWidthPx / 2
                        if (textY > textPaint.textSize) {
                            drawContext.canvas.nativeCanvas.drawText(valueText, textX, textY, textPaint)
                        }
                    }
                }
                currentX += barActualWidthPx + spacingActualPx
            }
        }
    }
}
