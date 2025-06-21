package com.example.budgify.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.routes.ScreenRoutes
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ObjectivesScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Objectives.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val objectives by viewModel.allObjectives.collectAsState(initial = emptyList())
    val reachedCount = remember(objectives) {
        objectives.count { it.completed }
    }
    val unreachedCount = remember(objectives) {
        objectives.count { !it.completed && it.endDate.isAfter(LocalDate.now().minusDays(1)) } // Active and not completed
    }

    val currentLevel by viewModel.userLevel.collectAsStateWithLifecycle()
    val currentXp by viewModel.userXp.collectAsStateWithLifecycle()
    val xpForNextLevel = remember(currentLevel) { calculateXpForNextLevel(currentLevel) } //

    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(
            navController,
            viewModel,
            showSnackbar = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        ) }
    ){
        innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center, // Add space between sections
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column {

                    val explanatoryText = "Here you can check your stats and access objective management.\nComplete objectives, repay debts or collect credits to gain XP and increase your level.\nBy increasing your level you can unlock new themes!"

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = explanatoryText,
                                style = MaterialTheme.typography.bodyMedium, // Puoi scegliere lo stile che preferisci
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp), // Aggiungi padding per spaziatura
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    // Section 1: Profile Picture, Level, and Level Bar
                    ProfileAndLevelSection(
                        profilePicture = rememberVectorPainter(Icons.Filled.Person),
                        currentLevel = currentLevel,
                        currentXp = currentXp,
                        xpForNextLevel = xpForNextLevel,
                        progressToNextLevel = if (xpForNextLevel > 0) currentXp.toFloat() / xpForNextLevel else 0f
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 2: Reached and Unreached Objectives Count
                    ObjectiveCountsSection(
                        reachedCount = reachedCount,
                        unreachedCount = unreachedCount
                    )

                    // Section 3: Manage Objectives Button
                    ManageObjectivesButton(navController = navController)

                    // Optional: Display XP for demonstration
//                    Text("Current XP: $currentXp / $xpForNextLevel")
//                    Text("Level: $currentLevel")
                }

            }
    }
}

fun calculateXpForNextLevel(level: Int): Int {
    return 100 * level + (level -1) * 50 // Example: 100 for L1->L2, 250 L2->L3, 450 L3->L4 etc.
}

@Composable
fun ObjectiveCountsSection(reachedCount: Int, unreachedCount: Int) {
    Column (
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(5.dp)
    ) {
        Text (
            text = "Objectives",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
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
}

@Composable
fun ProfileAndLevelSection(
    profilePicture: Painter,
    currentLevel: Int,
    currentXp: Int, // Add current XP
    xpForNextLevel: Int, // Add XP needed for next level
    progressToNextLevel: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp), // Increased padding for better spacing
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Space out elements within this section
    ) {
        Text(
            text = "Your Achievements",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row (verticalAlignment = Alignment.CenterVertically) { // Align items in the row
            // Profile Picture
            Image(
                painter = profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp) // Slightly smaller
                    .clip(RoundedCornerShape(50)), // Make it circular
                colorFilter = tint(MaterialTheme.colorScheme.onSurface)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column { // Group Level and XP text
                Text(
                    text = "Level $currentLevel",
                    style = MaterialTheme.typography.headlineSmall // More prominent
                )
                Text(
                    text = "$currentXp / $xpForNextLevel XP", // Display XP progress
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        LinearProgressIndicator(
            progress = { progressToNextLevel }, // Pass progress as a lambda
            modifier = Modifier
                .fillMaxWidth(0.8f) // Adjust width of the bar
                .height(12.dp)
                .clip(RoundedCornerShape(5.dp)),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun ManageObjectivesButton(navController: NavController) {
    Button(
        onClick = {
            navController.navigate("objectives_management_screen")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp) // Add some space above the button
    ) {
        Text("Manage Objectives")
    }
}
