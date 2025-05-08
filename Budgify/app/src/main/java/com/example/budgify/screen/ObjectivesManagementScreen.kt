package com.example.budgify.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.budgify.datastruct.Objective
import com.example.budgify.datastruct.ObjectiveType
import com.example.budgify.routes.ScreenRoutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: fix navigation of bottom bar

@Composable
fun ObjectivesManagementScreen(navController: NavController) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.ObjectivesManagement.route) }
    // State variable to track the selected section
    var selectedSection by remember { mutableStateOf(ObjectivesManagementSection.Active) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
    ){
        innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
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
                        ActiveObjectivesSection()
                    }

                    ObjectivesManagementSection.Completed -> {
                        CompletedObjectivesSection()
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate("objectives_screen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text("Back to Objectives")
            }
        }
    }
}

// Define the possible sections
enum class ObjectivesManagementSection(val title: String) {
    Active("Active Objectives"),
    Completed("Completed Objectives")
}

@Composable
fun ObjectiveItem(obj: Objective) {

    val formatter = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }

    // Determina il colore di sfondo in base al tipo di obiettivo
    val backgroundColor = when (obj.type) {
        ObjectiveType.INCOME -> Color.Green.copy(alpha = 0.3f) // Verde semi-trasparente per profitto
        ObjectiveType.EXPENSE -> Color.Red.copy(alpha = 0.3f) // Rosso semi-trasparente per spesa
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
            text = formatter.format(obj.date), // Formatta la data
            textAlign = TextAlign.Center // Allinea il testo al centro
        )
        Text(
            text = "${obj.amount}€", // Aggiunto l'euro
            textAlign = TextAlign.Center // Allinea il testo al centro
        )
    }
}

@Composable
fun ActiveObjectivesSection() {
    val objectives = listOf(
        Objective(ObjectiveType.EXPENSE, "Desc1", 100.0, Date()),
        Objective(ObjectiveType.INCOME, "Desc2", 200.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc3", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc4", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc5", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc6", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc7", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc8", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc9", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc10", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc11", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc12", 300.0, Date()),
        Objective(ObjectiveType.EXPENSE,"Desc13", 300.0, Date()),
        // Add more objectives to see the grid scrolling
    )
    // Use LazyVerticalGrid instead of Column
    LazyVerticalGrid(
        // Define the grid cells. Fixed(2) means two columns of equal width.
        // You can also use GridCells.Adaptive(minSize = 100.dp) for a responsive grid
        // where the number of columns adapts to the available width with a minimum item size.
        columns = GridCells.Fixed(2),
        // Add some padding around the entire grid if needed
        contentPadding = PaddingValues(16.dp),
        // Add space between rows and columns
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Use the items extension function to efficiently display the list
        items(objectives) { objective ->
            // Each item in the grid will be this Box containing an ObjectiveItem
            Box (
                modifier = Modifier
                    .width(150.dp) // You might want to adjust these dimensions
                    .height(80.dp)  // based on the grid cell arrangement
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ){
                ObjectiveItem(objective)
            }
        }
    }
}

@Composable
fun CompletedObjectivesSection() {
    Column {
        Text("Content for Completed Objectives")
        // Add your Composables to display completed objectives here
        // For example, a LazyColumn to list completed objectives
    }
}