package com.example.budgify.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ObjectiveSectionType {
    ACTIVE,
    EXPIRED
}

@Composable
fun ObjectivesManagementScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.ObjectivesManagement.route) }
    // State variable to track the selected section
    var selectedSection by remember { mutableStateOf(ObjectivesManagementSection.Active) }

    val listState = rememberLazyListState()
    val showButton by remember {
        derivedStateOf {
            val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            val isAtBottom = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
            isAtTop || isAtBottom
        }
    }

    Scaffold(
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController, viewModel) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply Scaffold's padding
            // Optional: If you want a background *behind* the scrollable content but not the button
            // .background(MaterialTheme.colorScheme.background) // or a specific color
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                //.padding(innerPadding)
            ) {
                TabRow(selectedTabIndex = selectedSection.ordinal) {
                    ObjectivesManagementSection.entries.forEach { section ->
                        Tab(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            text = { Text(section.title) }
                        )
                    }
                }

                // Section 2: Content based on the selected section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 0.dp) // Add padding around the content within the selected section
                ) {
                    when (selectedSection) {
                        ObjectivesManagementSection.Active -> {
                            ObjectivesSection(ObjectiveSectionType.ACTIVE, listState, viewModel)
                        }

                        ObjectivesManagementSection.Expired -> {
                            ObjectivesSection(ObjectiveSectionType.EXPIRED, listState, viewModel)
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showButton,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }), // Slide in from bottom
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }), // Slide out to bottom
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        navController.navigate("objectives_screen")
                    },
                    modifier = Modifier
                        //.align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Back to Objectives")
                }
            }
        }
    }
}

// Define the possible sections
enum class ObjectivesManagementSection(val title: String) {
    Active("Active Objectives"),
    Expired("Expired Objectives")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObjectiveItem(obj: Objective, viewModel: FinanceViewModel) {

    // State to control dialog visibility
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Determina il colore di sfondo in base al tipo di obiettivo
    val backgroundColor = when (obj.type) {
        ObjectiveType.INCOME -> Color(0xff0db201) // Verde semi-trasparente per profitto
        ObjectiveType.EXPENSE -> Color(0xffff6f51) // Rosso semi-trasparente per spesa
    }

    Column (
        modifier = Modifier
            //.width(150.dp) // Aggiunto una larghezza fissa per un migliore allineamento
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Angoli arrotondati per la box dell'item
            .background(backgroundColor) // Applica il colore di sfondo
            .combinedClickable( // Use combinedClickable for long press
                onClick = { /* Handle normal click if needed */ },
                onLongClick = {
                    showEditDialog = true // Or show a menu with Edit/Delete options
                    // For simplicity, we'll show the edit dialog directly.
                    // You might want to show a small context menu instead.
                }
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Allinea orizzontalmente al centro
    ) {
        Text(
            text = obj.desc,
            textAlign = TextAlign.Center // Allinea il testo al centro
        )
        Text(
            text = "Start: ${obj.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", // Formatta la data"obj.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), // Formatta la data
            textAlign = TextAlign.Center // Allinea il testo al centro
        )
        Text(
            text = "End: ${obj.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", // Formatta la data"obj.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), // Formatta la data
            textAlign = TextAlign.Center // Allinea il testo al centro
        )
        Text(
            text = "${obj.amount}€", // Aggiunto l'euro
            textAlign = TextAlign.Center // Allinea il testo al centro
        )
    }
    // Edit Objective Dialog
    if (showEditDialog) {
        EditObjectiveDialog(
            objective = obj,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onDeleteClick = {
                showEditDialog = false // Dismiss edit dialog
                showDeleteConfirmationDialog = true // Show delete confirmation
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            objective = obj,
            viewModel = viewModel,
            onDismiss = { showDeleteConfirmationDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditObjectiveDialog(
    objective: Objective,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var description by remember { mutableStateOf(objective.desc) }
    var amount by remember { mutableStateOf(objective.amount.toString()) }
    var selectedStartDate by remember { mutableStateOf(objective.startDate) }
    var selectedEndDate by remember { mutableStateOf(objective.endDate) }
    var selectedType by remember { mutableStateOf(objective.type) }
    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
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
                Text("Edit Objective", style = MaterialTheme.typography.titleLarge)
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

            // Start Date Selection
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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // End Date Selection
            TextField(
                value = selectedEndDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("End Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select End Date",
                        modifier = Modifier.clickable { showEndDatePickerDialog = true }
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    objectiveTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type })
                            Text(type.name)
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
                    onClick = onDeleteClick,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
                Button(onClick = {
                    // **Validation (Add your validation logic here)**
                    val amountDouble = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountDouble != null) {
                        val updatedObjective = objective.copy(
                            desc = description,
                            amount = amountDouble,
                            startDate = selectedStartDate,
                            endDate = selectedEndDate,
                            type = selectedType
                        )
                        viewModel.updateObjective(updatedObjective)
                        onDismiss()
                    } else {
                        // Show error
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }

    if (showStartDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                showStartDatePickerDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStartDatePickerDialog = false
                        // Convert the selected date from milliseconds to LocalDate
                        selectedStartDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }!!
                    },
                    enabled = confirmEnabled.value // Enable OK button only if a date is selected
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePickerDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                showEndDatePickerDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndDatePickerDialog = false
                        // Convert the selected date from milliseconds to LocalDate
                        selectedEndDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }!!
                    },
                    enabled = confirmEnabled.value // Enable OK button only if a date is selected
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePickerDialog = false
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
fun DeleteConfirmationDialog(
    objective: Objective,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the objective \"${objective.desc}\"?") },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.deleteObjective(objective)
                    onDismiss()
                }
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
fun ObjectivesSection(type: ObjectiveSectionType, listState: LazyListState, viewModel: FinanceViewModel) {
    val objectives by viewModel.allObjectives.collectAsStateWithLifecycle()
    val filteredObjectives = remember(objectives, type) {
        when (type) {
            ObjectiveSectionType.ACTIVE -> objectives.filter { !isObjectiveExpired(it) }
            ObjectiveSectionType.EXPIRED -> objectives.filter { isObjectiveExpired(it) }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 65.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Use the items extension function to efficiently display the list
        items(filteredObjectives) { objective ->
            Box {
                ObjectiveItem(objective, viewModel)
            }
        }
    }
}

fun isObjectiveExpired(objective: Objective): Boolean {
    // Replace with your actual completion logic based on your Objective data
    // For example, if you have a `isCompleted` flag:
    // return objective.isCompleted

    // Or if based on date:
    return objective.endDate.isBefore(LocalDate.now())
}