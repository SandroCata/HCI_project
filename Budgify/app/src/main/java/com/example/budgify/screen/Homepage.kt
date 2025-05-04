package com.example.budgify.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar

import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.budgify.routes.ScreenRoutes

// Definisci gli stili del testo
val smallTextStyle = TextStyle(fontSize = 11.sp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homepage(navController: NavController) {
    // Definisco la lista di schermate
    val items = listOf(
        ScreenRoutes.Transactions,
        ScreenRoutes.Objectives,
        ScreenRoutes.Adding,
        ScreenRoutes.CredDeb,
        ScreenRoutes.Categories
    )

    var showDialog by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf(ScreenRoutes.Home.route) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar
            CenterAlignedTopAppBar(
                title = { Text("Dashboard", fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.LightGray,
                    titleContentColor = Color.Black
                ),
                navigationIcon = {
                    if (currentRoute != ScreenRoutes.Home.route) {
                        IconButton(onClick = {
                            navController.navigate(ScreenRoutes.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Icon(Icons.Filled.Home, contentDescription = "Home")
                        }
                    }
                }
            )

            Box(modifier = Modifier.fillMaxSize().padding(bottom = 0.dp)) {
            // Barra di navigazione inferiore
            NavigationBar (modifier = Modifier.align(Alignment.BottomCenter)) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val desc=""
                items.forEach { screen ->
                    val iconModifier = if (screen == ScreenRoutes.Adding) {
                        Modifier.size(36.dp)// Icona ingrandita per "Add"
                    } else {
                        Modifier.size(24.dp) // Icona standard per le altre schermate
                    }
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.icon),
                                contentDescription = null,
                                modifier = iconModifier
                            )
                        },
                        label = { Text(if (screen == ScreenRoutes.Adding) "" else screen.title, style = smallTextStyle) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            if (screen == ScreenRoutes.Adding) {
                                showDialog = true
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}