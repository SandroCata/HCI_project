package com.example.budgify.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.budgify.routes.ScreenRoutes

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
        }
    }
}

// Define the possible sections
enum class ObjectivesManagementSection(val title: String) {
    Active("Active Objectives"),
    Completed("Completed Objectives")
}

@Composable
fun ActiveObjectivesSection() {
    Column {
        Text("Content for Active Objectives")
        // Add your Composables to display active objectives here
        // For example, a LazyColumn to list objectives
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