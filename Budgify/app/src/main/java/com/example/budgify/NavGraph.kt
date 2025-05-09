package com.example.budgify

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.screen.Homepage
import com.example.budgify.screen.ObjectivesManagementScreen
import com.example.budgify.screen.ObjectivesScreen
import com.example.budgify.screen.Settings
import com.example.budgify.screen.TransactionsScreen


@Composable
fun NavGraph(viewModel: FinanceViewModel) {

    //function to navigate from one screen to another
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        composable("home_screen") { Homepage(navController, viewModel) }
        composable("objectives_screen") { ObjectivesScreen(navController, viewModel) }
        composable("objectives_management_screen") { ObjectivesManagementScreen(navController, viewModel) }
        composable("settings_screen") { Settings(navController, viewModel) }
        composable("transactions_screen") { TransactionsScreen(navController, viewModel) }
        composable("manage_objectives_screen") { ObjectivesManagementScreen(navController, viewModel) }
        /*
        composable("cred_deb_screen") { CredDeb() }
        composable("credits_screen") { Credits() }
        composable("debits_screen") { Debits() }
        composable("categories_screen") { Categories() }
         */
    }
}