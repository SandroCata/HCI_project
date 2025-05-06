package com.example.budgify.routes

import com.example.budgify.R

// Definisci la classe Screen (enum)
enum class ScreenRoutes(val route: String, val title: String, val icon: Int) {
    Home("home_screen", "Home", 999999999),
    Settings("settings_screen", "Settings", 999999998),
    Transactions("transactions_screen", "Transactions", R.drawable.paid),
    Objectives("objectives_screen", "Objectives", R.drawable.reward),
    ManageObjectives("manage_objectives_screen", "Manage Objectives", 999999997),
    Adding("adding_screen", "Add", R.drawable.add),
    CredDeb("cred_deb_screen", "Loans", R.drawable.cred),
    Credits("credits_screen", "Credits", 999999996),
    Debits("debits_screen", "Debits", 999999995),
    Categories("categories_screen", "Categories", R.drawable.categories)
}