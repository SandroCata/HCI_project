package com.example.budgify.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.copy
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.text.forEach
import kotlin.text.lowercase
import kotlin.text.toList

// Enum for the sections in LoanManagementScreen, aligning with LoanType
enum class LoanSectionType(val title: String, val loanType: LoanType) {
    DEBTS("Debts", LoanType.DEBT),
    CREDITS("Credits", LoanType.CREDIT); // Semicolon needed if you add functions/companion object

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
            if (listState.layoutInfo.totalItemsCount <= listState.layoutInfo.visibleItemsInfo.size) {
                true
            } else {
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtBottom = lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
                isAtTop || isAtBottom || !listState.isScrollInProgress
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // Pass the selected LoanType for filtering
                    LoansSection(
                        loanType = selectedSection.loanType,
                        listState = listState,
                        viewModel = viewModel,
                        showSnackbar = showSnackbar
                    )
                }
            }
            AnimatedVisibility(
                visible = showButton,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        navController.navigate(ScreenRoutes.CredDeb.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Back to Loans overview")
                }
            }
        }
    }
}

@Composable
fun LoansSection(
    loanType: LoanType, // To filter which loans to display
    listState: LazyListState,
    viewModel: FinanceViewModel,
    showSnackbar: (String) -> Unit
) {
    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle() // Assuming viewModel.allLoans
    val filteredLoans = remember(allLoans, loanType) {
        allLoans.filter { it.type == loanType }
    }

    if (filteredLoans.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No ${loanType.name.lowercase()}s found.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredLoans, key = { it.id }) { loan ->
                LoanItem(loan = loan, viewModel = viewModel, showSnackbar = showSnackbar)
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

    val backgroundColor = when (loan.type) {
        LoanType.CREDIT -> Color(0xFF4CAF50).copy(alpha = 0.8f) // Greenish for credit
        LoanType.DEBT -> Color(0xFFF44336).copy(alpha = 0.8f)   // Reddish for debt
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = { showSnackbar("Hold to edit or delete the ${loan.type.name.lowercase()}") },
                onLongClick = { showActionChoiceDialog = true }
            )
            .padding(16.dp),
    ) {
        Text(
            text = "${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }}: ${loan.desc}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(text = "Amount: ${loan.amount}€", color = Color.White)
        Text(text = "Start Date: ${loan.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", color = Color.White)
        loan.endDate?.let {
            Text(text = "End Date: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", color = Color.White)
        }
    }

    if (showActionChoiceDialog) {
        LoanActionChoiceDialog(
            loan = loan,
            onDismiss = { showActionChoiceDialog = false },
            onEditClick = {
                showEditDialog = true
                showActionChoiceDialog = false
            },
            onDeleteClick = {
                showDeleteConfirmationDialog = true
                showActionChoiceDialog = false
            }
        )
    }

    if (showEditDialog) {
        EditLoanDialog(
            loan = loan,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            showSnackbar = showSnackbar
        )
    }

    if (showDeleteConfirmationDialog) {
        ConfirmLoanDeleteDialog(
            loan = loan,
            onDismiss = { showDeleteConfirmationDialog = false },
            onConfirmDelete = {
                viewModel.deleteLoan(loan) // ViewModel handles deletion
                showDeleteConfirmationDialog = false
                showSnackbar("${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' deleted")
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loan: Loan,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    var description by remember { mutableStateOf(loan.desc) }
    var amount by remember { mutableStateOf(loan.amount.toString().replace('.',',')) }
    var selectedStartDate by remember { mutableStateOf(loan.startDate) }
    var selectedEndDate by remember { mutableStateOf(loan.endDate) } // Nullable
    var selectedLoanType by remember { mutableStateOf(loan.type) } // Type can be edited
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
                Text("Edit Loan", style = MaterialTheme.typography.titleLarge)
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
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = amount.replace(',','.').toDoubleOrNull() == null && errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Loan Type Radio Buttons
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
                isError = errorMessage != null && selectedEndDate != null && selectedStartDate.isAfter(selectedEndDate)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = selectedEndDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Optional",
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
                isError = errorMessage != null && selectedEndDate != null && selectedStartDate.isAfter(selectedEndDate)
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        errorMessage = null // Reset error
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()

                        if (description.isBlank()) {
                            errorMessage = "Description cannot be empty."
                            return@Button
                        }
                        if (amountDouble == null || amountDouble <= 0) {
                            errorMessage = "Please enter a valid positive amount."
                            return@Button
                        }
                        if (selectedEndDate != null && selectedStartDate.isAfter(selectedEndDate)) {
                            errorMessage = "Start date cannot be after end date."
                            return@Button
                        }

                        val updatedLoan = loan.copy(
                            desc = description,
                            amount = amountDouble,
                            startDate = selectedStartDate,
                            endDate = selectedEndDate,
                            type = selectedLoanType
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
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
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePickerDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedEndDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli())
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
                    enabled = true // Allow null selection
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedEndDate = null // Clear if canceling from a set date
                    showEndDatePickerDialog = false
                }) { Text("Clear & Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}