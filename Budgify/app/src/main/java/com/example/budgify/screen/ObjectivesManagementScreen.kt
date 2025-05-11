package com.example.budgify.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.routes.ScreenRoutes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.derivedStateOf
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar

enum class ObjectiveSectionType {
    ACTIVE,
    EXPIRED
}

@Composable
fun ObjectivesManagementScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.ObjectivesManagement.route) }
    // State variable to track the selected section
    var selectedSection by remember { mutableStateOf(ObjectivesManagementSection.Active) }

    val listState = rememberLazyGridState()
    val showButton by remember {
        derivedStateOf {
            val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            val isAtBottom = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
            isAtTop || isAtBottom
        }
    }

    Scaffold(
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) },
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
                        .padding(16.dp) // Add padding around the content within the selected section
                ) {
                    when (selectedSection) {
                        ObjectivesManagementSection.Active -> {
                            ObjectivesSection(ObjectiveSectionType.ACTIVE, listState)
                        }

                        ObjectivesManagementSection.Expired -> {
                            ObjectivesSection(ObjectiveSectionType.EXPIRED, listState)
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

@Composable
fun ObjectiveItem(obj: Objective) {

    // Determina il colore di sfondo in base al tipo di obiettivo
    val backgroundColor = when (obj.type) {
        ObjectiveType.INCOME -> Color(0xff0db201) // Verde semi-trasparente per profitto
        ObjectiveType.EXPENSE -> Color(0xffff6f51) // Rosso semi-trasparente per spesa
    }

    Column (
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp) // Aggiunto una larghezza fissa per un migliore allineamento
            .clip(RoundedCornerShape(8.dp)) // Angoli arrotondati per la box dell'item
            .background(backgroundColor) // Applica il colore di sfondo
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
}

@Composable
fun ObjectivesSection(type: ObjectiveSectionType, listState: LazyGridState) {
    val objectives = listOf(
        Objective(1, ObjectiveType.EXPENSE, "Desc1", 100.0, LocalDate.now(), LocalDate.now().plusDays(7)),
        Objective(2, ObjectiveType.INCOME, "Desc2", 200.0, LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), false),
        Objective(3, ObjectiveType.EXPENSE,"Desc3", 300.0,LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), false),
        Objective(4, ObjectiveType.EXPENSE,"Desc4", 300.0, LocalDate.now().minusDays(3), LocalDate.now().plusDays(3), false),
        Objective(5, ObjectiveType.EXPENSE,"Desc5", 300.0, LocalDate.now().minusDays(3), LocalDate.now().plusDays(3), false),
        Objective(6, ObjectiveType.INCOME,"Desc6", 300.0, LocalDate.now().minusDays(5), LocalDate.now().plusDays(3), false),
        Objective(6, ObjectiveType.INCOME,"Desc6", 300.0, LocalDate.now().minusDays(5), LocalDate.now().plusDays(3), false),
        Objective(6, ObjectiveType.INCOME,"Desc6", 300.0, LocalDate.now().minusDays(5), LocalDate.now().plusDays(3), false)
        )

    // Use LazyVerticalGrid instead of Column
    LazyVerticalGrid(
        // Define the grid cells. Fixed(2) means two columns of equal width.
        // You can also use GridCells.Adaptive(minSize = 100.dp) for a responsive grid
        // where the number of columns adapts to the available width with a minimum item size.
        columns = GridCells.Fixed(1),
        // Add some padding around the entire grid if needed
        state = listState,
        contentPadding = PaddingValues(16.dp),
        // Add space between rows and columns
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Use the items extension function to efficiently display the list
        items(objectives) { objective ->
            // Each item in the grid will be this Box containing an ObjectiveItem
            Box (
                modifier = Modifier.fillMaxSize()
            ){
                ObjectiveItem(objective)
            }
        }
    }
}