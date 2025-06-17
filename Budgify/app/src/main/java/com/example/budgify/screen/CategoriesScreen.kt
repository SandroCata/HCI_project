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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

// Define the possible sections for categories
enum class CategoriesTab(val title: String) {
    Expenses("Expenses"),
    Income("Income")
}

@Composable
fun CategoriesScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Categories.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(CategoriesTab.Expenses) }

    // State for managing dialogs
    var showAddDialog by remember { mutableStateOf(false) }
    // var showEditDialog by remember { mutableStateOf<Category?>(null) } // Vecchio stato per modifica diretta
    var categoryToAction by remember { mutableStateOf<Category?>(null) } // Nuovo: categoria per scelta azione
    var showCategoryActionChoiceDialog by remember { mutableStateOf(false) } // Nuovo: dialog scelta azione
    var showEditCategoryDialog by remember { mutableStateOf(false) } // Nuovo: per mostrare specificamente il dialog di modifica
    var showDeleteConfirmDialog by remember { mutableStateOf<Category?>(null) } // Rimane per conferma eliminazione

    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    val expenseCategories = remember(allCategories, allCategories.size) { // Aggiunto allCategories.size per triggerare la ricomposizione
        allCategories.filter {
            it.type == CategoryType.EXPENSE &&
                    it.desc != "Debts repaid" &&
                    it.desc != "Credits contracted" &&
                    it.desc != "Objectives (Expense)"
        }
    }
    val incomeCategories = remember(allCategories, allCategories.size) { // Aggiunto allCategories.size
        allCategories.filter {
            it.type == CategoryType.INCOME &&
                    it.desc != "Credits collected" &&
                    it.desc != "Debts contracted" &&
                    it.desc != "Objectives (Income)"
        }
    }
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(
            navController,
            viewModel,
            showSnackbar = showSnackbar
        ) }
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    CategoriesTab.Expenses -> {
                        CategoryGridSection(
                            categories = expenseCategories,
                            categoryType = CategoryType.EXPENSE,
                            backgroundColor = Color(0xffff6f51), // Red
                            onAddClick = { showAddDialog = true },
                            onCategoryClick = { showSnackbar("Hold to choose an action for the category") },
                            onCategoryLongClick = { category ->
                                categoryToAction = category
                                showCategoryActionChoiceDialog = true // Mostra il dialog di scelta
                            },
                            viewModel = viewModel
                        )
                    }
                    CategoriesTab.Income -> {
                        CategoryGridSection(
                            categories = incomeCategories,
                            categoryType = CategoryType.INCOME,
                            backgroundColor = Color(0xff0db201), // Green
                            onAddClick = { showAddDialog = true },
                            onCategoryClick = { showSnackbar("Hold to choose an action for the category") },
                            onCategoryLongClick = { category ->
                                categoryToAction = category
                                showCategoryActionChoiceDialog = true // Mostra il dialog di scelta
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Add Category Dialog (invariato)
    if (showAddDialog) {
        AddCategoryDialog(
            viewModel = viewModel,
            initialType = selectedTab.let {
                when (it) {
                    CategoriesTab.Expenses -> CategoryType.EXPENSE
                    CategoriesTab.Income -> CategoryType.INCOME
                }
            },
            onDismiss = { showAddDialog = false },
            onCategoryAdded = { category ->
                showAddDialog = false
                showSnackbar("Category '${category.desc}' added")
            }
        )
    }

    // Category Action Choice Dialog (Nuovo)
    if (showCategoryActionChoiceDialog && categoryToAction != null) {
        CategoryActionChoiceDialog(
            category = categoryToAction!!,
            onDismiss = {
                showCategoryActionChoiceDialog = false
                categoryToAction = null
            },
            onEditClick = {
                showEditCategoryDialog = true // Imposta lo stato per mostrare EditCategoryDialog
                showCategoryActionChoiceDialog = false
                // categoryToAction rimane impostato per EditCategoryDialog
            },
            onDeleteClick = {
                showDeleteConfirmDialog = categoryToAction // Imposta la categoria per la conferma eliminazione
                showCategoryActionChoiceDialog = false
                // categoryToAction rimane impostato temporaneamente per DeleteCategoryConfirmationDialog
            }
        )
    }


    // Edit Category Dialog (ora attivato da showEditCategoryDialog)
    if (showEditCategoryDialog && categoryToAction != null) {
        EditCategoryDialog(
            category = categoryToAction!!,
            viewModel = viewModel,
            onDismiss = {
                showEditCategoryDialog = false
                categoryToAction = null // Pulisci dopo la chiusura
            },
            onDeleteClick = {
                // Questa onDeleteClick dentro EditCategoryDialog ora apre il dialog di conferma
                showDeleteConfirmDialog = categoryToAction
                showEditCategoryDialog = false // Chiudi il dialog di modifica
                // categoryToAction rimane per il dialog di conferma
            }
        )
    }

    // Delete Confirmation Dialog (leggermente modificato per resettare categoryToAction alla fine)
    showDeleteConfirmDialog?.let { categoryToDelete ->
        DeleteCategoryConfirmationDialog(
            category = categoryToDelete,
            viewModel = viewModel,
            onDismiss = {
                showDeleteConfirmDialog = null
                categoryToAction = null // Assicurati di resettare anche categoryToAction
            },
            onDeleteConfirmed = {
                // Azioni dopo la conferma dell'eliminazione
                showSnackbar("Category '${categoryToDelete.desc}' deleted")
                showDeleteConfirmDialog = null
                categoryToAction = null // Assicurati di resettare anche categoryToAction
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    category: Category,
    viewModel: FinanceViewModel, // Receive viewModel
    backgroundColor: Color,
    onClick: (Category) -> Unit,
    onLongClick: (Category) -> Unit // Callback for long click
) {
    Column(
        modifier = Modifier
            .width(150.dp) // Fixed width for better alignment in grid
            .height(80.dp) // Fixed height
            .clip(RoundedCornerShape(16.dp)) // Rounded corners
            .background(backgroundColor) // Apply background color
            .combinedClickable( // Handle long click
                onClick = { onClick(category) },
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
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick), // Call the provided lambda on click
        contentAlignment = Alignment.Center // Center the content (Icon)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Category",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CategoryGridSection(
    categories: List<Category>,
    categoryType: CategoryType,
    backgroundColor: Color,
    onAddClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit, // Receive long click callback
    viewModel: FinanceViewModel // Receive viewModel
) {
    if (categories.isEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Or GridCells.Adaptive(minSize = 120.dp)
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 8.dp
            ), // Adjust padding as needed
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add the "Add Category" button as the first item
            item {
                AddCategoryButton(categoryType = categoryType, onClick = onAddClick)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the available space
                .padding(16.dp), // Add some padding
            contentAlignment = Alignment.Center // Center the text
        ) {
            Text(
                text = "No categories found for ${categoryType.name.lowercase()}.\nTap the '+' button to add a new one!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Make it slightly less prominent
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Or GridCells.Adaptive(minSize = 120.dp)
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 8.dp
            ), // Adjust padding as needed
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
                    onClick = onCategoryClick,
                    onLongClick = onCategoryLongClick // Pass callback
                )
            }
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
    var selectedType by remember { mutableStateOf(initialType?: CategoryType.EXPENSE) }
    val categoryTypes = CategoryType.entries.toList()
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface( // Use Surface for elevation and shaping
            shape = RoundedCornerShape(16.dp),
            //color = MaterialTheme.colorScheme.surface,
            //tonalElevation = 8.dp, // Add some elevation
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (description.isNotBlank()) {
                                coroutineScope.launch {
                                    val newCategory = Category(
                                        type = selectedType!!,
                                        desc = description
                                        // ID will be auto-generated by Room
                                    )
                                    viewModel.addCategory(newCategory)
                                    onCategoryAdded(newCategory)
                                    //onDismiss()
                                }
                            } else {
                                // Optional: Show an error message if description is empty
                            }
                        },
                        enabled = description.isNotBlank(), // Disable button if description is empty
                        //modifier = Modifier.fillMaxWidth() // Make the Add button fill the width
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryActionChoiceDialog(
    category: Category,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Category: '${category.desc}'")
            XButton(onDismiss)
        }
                },
        text = { Text("What would you like to do?") },
        confirmButton = { // Questo blocco contiene i pulsanti di azione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // O Arrangement.End
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Edit")
                }
                TextButton(onClick = onDeleteClick) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = null
    )
}


@Composable
fun EditCategoryDialog(
    category: Category,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit // Questo ora apre il dialog di conferma
) {
    var description by remember { mutableStateOf(category.desc) }
    val scope = rememberCoroutineScope() // Per lanciare la coroutine dell'update

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp) // Applica il padding qui se vuoi spazio attorno al Surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp) // Padding interno per il contenuto
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Category",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    // Usa XButton se lo hai definito, altrimenti IconButton standard
                    XButton(onDismiss)
                    /* IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }*/
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = {
                            if (description.isNotBlank()) {
                                val updatedCategory = category.copy(desc = description)
                                scope.launch { // Usa lo scope per chiamare la funzione suspend
                                    viewModel.updateCategory(updatedCategory)
                                }
                                onDismiss() // Chiudi il dialog dopo il salvataggio
                            }
                        },
                        enabled = description.isNotBlank() && description != category.desc
                    ) {
                        Text("Save changes")
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
    onDismiss: () -> Unit,
    onDeleteConfirmed: () -> Unit // Callback aggiuntiva per quando l'eliminazione è confermata
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss, // Chiamato se si clicca fuori o si preme back
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the category \"${category.desc}\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch { // Usa lo scope per chiamare la funzione suspend
                        viewModel.deleteCategory(category)
                        onDeleteConfirmed() // Chiama la callback dopo l'eliminazione
                    }
                }
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { // Il pulsante "Cancel" chiama solo onDismiss
                Text("Cancel")
            }
        }
    )
}



