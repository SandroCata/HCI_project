package com.example.budgify.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.routes.ScreenRoutes

@Composable
fun CategoriesScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Categories.route) }
    // State variable to track the selected section
    var selectedSection by remember { mutableStateOf(CategoriesSection.Active) }

    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController, viewModel) }
    ){
            innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            TabRow(selectedTabIndex = selectedSection.ordinal) {
                CategoriesSection.entries.forEach { section ->
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
                    .padding(16.dp) // Add padding around the content within the selected section
            ) {
                when (selectedSection) {
                    CategoriesSection.Active -> {
                        ExpensesSection()
                    }

                    CategoriesSection.Completed -> {
                        IncomeSection()
                    }
                }
            }


        }
    }
}

// Define the possible sections
enum class CategoriesSection(val title: String) {
    Active("Expenses"),
    Completed("Income")
}

@Composable
fun ObjectiveItem(obj: Category) {

    Column (
        modifier = Modifier
            .width(150.dp) // Aggiunto una larghezza fissa per un migliore allineamento
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp)) // Angoli arrotondati per la box dell'item
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Allinea orizzontalmente al centro
        verticalArrangement = Arrangement.Center
    ) {

            Text(
                text = obj.desc,
                textAlign = TextAlign.Center, // Allinea il testo al centro
            )
        }

}

@Composable
fun AddCategoryButton(categoryType: CategoryType) {
    Box (
        modifier = Modifier
            .width(150.dp) // You might want to adjust these dimensions
            .height(80.dp)  // based on the grid cell arrangement
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .clickable {
                // TODO: Handle add category logic here
                // This lambda is executed when the Box is clicked.
                // For example:
                // viewModel.onAddCategoryClicked(categoryType)
                println("Add category button clicked for type: $categoryType")
            }
    ){
        Column (
            modifier = Modifier
                .width(150.dp) // Aggiunto una larghezza fissa per un migliore allineamento
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)) // Angoli arrotondati per la box dell'item
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Allinea orizzontalmente al centro
            verticalArrangement = Arrangement.Center

        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Category",
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
fun ExpensesSection(){
    val ExpenseCategories = listOf(
        Category(1,CategoryType.EXPENSE, "Food"),
        Category(2,CategoryType.EXPENSE, "Transportation"),
        Category(3,CategoryType.EXPENSE, "Housing"),
        Category(4,CategoryType.EXPENSE, "Entertainment")
    )

    // Use LazyVerticalGrid instead of Column
    LazyVerticalGrid(
        // Define the grid cells. Fixed(2) means two columns of equal width.
        // You can also use GridCells.Adaptive(minSize = 100.dp) for a responsive grid
        // where the number of columns adapts to the available width with a minimum item size.
        columns = GridCells.Fixed(3),
        // Add some padding around the entire grid if needed
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 50.dp),
        // Add space between rows and columns
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add the "Add Category" button as the first item
        item {
            AddCategoryButton(CategoryType.EXPENSE)
        }
        // Use the items extension function to efficiently display the list
        items(ExpenseCategories) { category ->
            // Each item in the grid will be this Box containing an ObjectiveItem
            Box (
                modifier = Modifier
                    .width(150.dp) // You might want to adjust these dimensions
                    .height(80.dp)  // based on the grid cell arrangement
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xffff6f51))  //Red
            ){
                ObjectiveItem(category)
            }
        }
    }
}

@Composable
fun IncomeSection(){
    val IncomeCategories = listOf(
        Category(5,CategoryType.INCOME, "Salary"),
        Category(6, CategoryType.INCOME, "Freelance"),
        Category(7, CategoryType.INCOME, "Investments")
    )

    // Use LazyVerticalGrid instead of Column
    LazyVerticalGrid(
        // Define the grid cells. Fixed(2) means two columns of equal width.
        // You can also use GridCells.Adaptive(minSize = 100.dp) for a responsive grid
        // where the number of columns adapts to the available width with a minimum item size.
        columns = GridCells.Fixed(3),
        // Add some padding around the entire grid if needed
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 50.dp),
        // Add space between rows and columns
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add the "Add Category" button as the first item
        item {
            AddCategoryButton(CategoryType.INCOME)
        }
        // Use the items extension function to efficiently display the list
        items(IncomeCategories) { category ->
            // Each item in the grid will be this Box containing an ObjectiveItem
            Box (
                modifier = Modifier
                    .width(150.dp) // You might want to adjust these dimensions
                    .height(80.dp)  // based on the grid cell arrangement
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xff0db201)) //Green
            ){
                ObjectiveItem(category)
            }
        }
    }
}



