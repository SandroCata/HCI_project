package com.example.budgify.screen

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes

// Define the possible sections for categories
enum class CategoriesTab(val title: String) {
    Expenses("Expenses"),
    Income("Income")
}

@Composable
fun CategoriesScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Categories.route) }
    // State variable to track the selected tab
    var selectedTab by remember { mutableStateOf(CategoriesTab.Expenses) }

    // State for managing dialogs
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Category?>(null) } // Store the category being edited
    var showDeleteConfirmDialog by remember { mutableStateOf<Category?>(null) } // Store category for deletion confirmation

    // Collect categories from ViewModel
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    // Filter categories based on type
    val expenseCategories = remember(allCategories) {
        allCategories.filter { it.type == CategoryType.EXPENSE }
    }
    val incomeCategories = remember(allCategories) {
        allCategories.filter { it.type == CategoryType.INCOME }
    }

    Scaffold(
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController, viewModel) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                CategoriesTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Content based on the selected tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp) // Add padding around the grid content
            ) {
                when (selectedTab) {
                    CategoriesTab.Expenses -> {
                        CategoryGridSection(
                            categories = expenseCategories,
                            categoryType = CategoryType.EXPENSE,
                            backgroundColor = Color(0xffff6f51), // Red
                            onAddClick = { showAddDialog = true },
                            onCategoryLongClick = { category -> showEditDialog = category },
                            viewModel = viewModel // Pass viewModel down
                        )
                    }
                    CategoriesTab.Income -> {
                        CategoryGridSection(
                            categories = incomeCategories,
                            categoryType = CategoryType.INCOME,
                            backgroundColor = Color(0xff0db201), // Green
                            onAddClick = { showAddDialog = true },
                            onCategoryLongClick = { category -> showEditDialog = category },
                            viewModel = viewModel // Pass viewModel down
                        )
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Add Category Dialog
    if (showAddDialog) {
        AddCategoryDialog(
            viewModel = viewModel,
            initialType = selectedTab.let { // Determine initial type based on selected tab
                when (it) {
                    CategoriesTab.Expenses -> CategoryType.EXPENSE
                    CategoriesTab.Income -> CategoryType.INCOME
                }
            },
            onDismiss = { showAddDialog = false },
            onCategoryAdded = { }
        )
    }

    // Edit Category Dialog
    showEditDialog?.let { categoryToEdit ->
        EditCategoryDialog(
            category = categoryToEdit,
            viewModel = viewModel,
            onDismiss = { showEditDialog = null },
            onDeleteClick = {
                showEditDialog = null // Dismiss edit dialog first
                showDeleteConfirmDialog = categoryToEdit // Then show delete confirmation
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { categoryToDelete ->
        DeleteCategoryConfirmationDialog(
            category = categoryToDelete,
            viewModel = viewModel,
            onDismiss = { showDeleteConfirmDialog = null }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    category: Category,
    viewModel: FinanceViewModel, // Receive viewModel
    backgroundColor: Color,
    onLongClick: (Category) -> Unit // Callback for long click
) {
    Column(
        modifier = Modifier
            .width(150.dp) // Fixed width for better alignment in grid
            .height(80.dp) // Fixed height
            .clip(RoundedCornerShape(16.dp)) // Rounded corners
            .background(backgroundColor) // Apply background color
            .combinedClickable( // Handle long click
                onClick = { /* Optional: Handle regular click if needed */ },
                onLongClick = { onLongClick(category) } // Trigger the long click callback
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = category.desc,
            textAlign = TextAlign.Center,
            color = Color.White // Make text visible on colored background
        )
    }
}

@Composable
fun AddCategoryButton(
    categoryType: CategoryType, // Keep type for context if needed inside
    onClick: () -> Unit // Use a simple onClick lambda
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .clickable(onClick = onClick), // Call the provided lambda on click
        contentAlignment = Alignment.Center // Center the content (Icon)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Category",
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
fun CategoryGridSection(
    categories: List<Category>,
    categoryType: CategoryType,
    backgroundColor: Color,
    onAddClick: () -> Unit,
    onCategoryLongClick: (Category) -> Unit, // Receive long click callback
    viewModel: FinanceViewModel // Receive viewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Or GridCells.Adaptive(minSize = 120.dp)
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp), // Adjust padding as needed
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add the "Add Category" button as the first item
        item {
            AddCategoryButton(categoryType = categoryType, onClick = onAddClick)
        }

        // Display the list of categories
        items(categories, key = { it.id }) { category -> // Add a key for performance
            CategoryItem(
                category = category,
                viewModel = viewModel, // Pass viewModel
                backgroundColor = backgroundColor,
                onLongClick = onCategoryLongClick // Pass callback
            )
        }
    }
}


// Dialog Composable Functions

@Composable
fun AddCategoryDialog(
    viewModel: FinanceViewModel,
    initialType: CategoryType?,
    onDismiss: () -> Unit,
    onCategoryAdded: (Category) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    val categoryTypes = CategoryType.entries.toList()

    Dialog(onDismissRequest = onDismiss) {
        Surface( // Use Surface for elevation and shaping
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp, // Add some elevation
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row( // Row for title and close button
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add Category",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f) // Allow text to take available space
                    )
                    XButton(onDismiss)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialType == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Type:")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            categoryTypes.forEach { type ->
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Removed the old Cancel button row
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            val newCategory = Category(
                                type = selectedType!!,
                                desc = description
                                // ID will be auto-generated by Room
                            )
                            viewModel.addCategory(newCategory)
                            onDismiss()
                        } else {
                            // Optional: Show an error message if description is empty
                        }
                    },
                    enabled = description.isNotBlank(), // Disable button if description is empty
                    modifier = Modifier.fillMaxWidth() // Make the Add button fill the width
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: Category,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var description by remember { mutableStateOf(category.desc) }
    // Type is generally not editable for categories, but if needed, add state & UI like in AddCategoryDialog

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Category",
                        style = MaterialTheme.typography.titleLarge,
                    )

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

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            if (description.isNotBlank()) {
                                val updatedCategory = category.copy(
                                    desc = description
                                    // Keep the original type: type = category.type
                                )
                                viewModel.updateCategory(updatedCategory)
                                onDismiss()
                            } else {
                                // Optional: Show error
                            }
                        },
                        enabled = description.isNotBlank() && description != category.desc // Enable only if changed and not blank
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@Composable
fun DeleteCategoryConfirmationDialog(
    category: Category,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the category \"${category.desc}\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.deleteCategory(category)
                    onDismiss()
                }
            ) {
                Text("Delete", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



