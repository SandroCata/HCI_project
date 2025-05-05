package com.example.budgify

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgify.screen.Homepage
import com.example.budgify.screen.Objectives

@Composable
fun NavGraph() {

    //function to navigate from one screen to another
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        composable("home_screen") { Homepage(navController) }
        composable("objectives_screen") { Objectives(navController) }
    }
}