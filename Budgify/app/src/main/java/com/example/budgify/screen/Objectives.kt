package com.example.budgify.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.budgify.routes.ScreenRoutes

@Composable
fun Objectives(navController: NavController) {
    var currentRoute by remember { mutableStateOf(ScreenRoutes.Objectives.route) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
    ){
        innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.SpaceBetween // Distribute space between sections
            ) {
                //TopBar(navController, currentRoute)
                Column {
                    // Section 1: Profile Picture, Level, and Level Bar
                    ProfileAndLevelSection(
                        profilePicture = rememberVectorPainter(Icons.Filled.Person), // Replace with your logic to get the profile picture
                        currentLevel = 5, // Replace with your user's current level
                        progressToNextLevel = 0.75f // Replace with user's progress (0.0 to 1.0)
                    )

                    // Section 2: Reached and Unreached Objectives Count
                    ObjectiveCountsSection(
                        reachedCount = 15, // Replace with the number of reached objectives
                        unreachedCount = 5 // Replace with the number of unreached objectives
                    )

                    // Section 3: Manage Objectives Button
                    ManageObjectivesButton(navController = navController)
                }

                //BottomBar(navController)
            }
    }
}

@Composable
fun ObjectiveCountsSection(reachedCount: Int, unreachedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Add vertical space
        horizontalArrangement = Arrangement.SpaceEvenly // Distribute space between counts
    ) {
        // Reached Objectives Count
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = reachedCount.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Reached",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Unreached Objectives Count
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = unreachedCount.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Unreached",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ProfileAndLevelSection(profilePicture: Painter, currentLevel: Int, progressToNextLevel: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), // Add some space below this section
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            // Profile Picture
            Image(
                painter = profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(96.dp) // Adjust size as needed
                // You might add clipping (e.g., CircleShape) and other modifiers here
            )

            Spacer(modifier = Modifier.width(8.dp)) // Space between picture and text

            // Level Text
            Text(
                text = "Level $currentLevel",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Text(
            text = "Progress to next level: $progressToNextLevel"
        )
        Spacer(modifier = Modifier.height(4.dp)) // Space between level and bar
        // Level Bar (using LinearProgressIndicator)
        LinearProgressIndicator(
            progress = { progressToNextLevel }, // Pass progress as a lambda
            modifier = Modifier.fillMaxWidth(0.8f).height(10.dp), // Adjust width of the bar
        )
    }
}

@Composable
fun ManageObjectivesButton(navController: NavController) {
    Button(
        onClick = {
            // TODO: Replace "objectives_management_screen" with the actual route
            // of your Objectives Management screen in your NavGraph.
            navController.navigate("objectives_management_screen")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp) // Add some space above the button
    ) {
        Text("Manage Objectives")
    }
}
