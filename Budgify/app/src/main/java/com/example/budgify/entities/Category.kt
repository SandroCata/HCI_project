package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: CategoryType,
    val desc: String
)

object DefaultCategories {
    val OBJECTIVES_EXP = Category(desc = "Objectives (Expense)", type = CategoryType.EXPENSE) // Or your chosen type
    val OBJECTIVES_INC = Category(desc = "Objectives (Income)", type = CategoryType.INCOME)
    val LOANS_INC = Category(desc = "Credits", type = CategoryType.INCOME)          // Or your chosen type
    val LOANS_EXP = Category(desc = "Debts", type = CategoryType.EXPENSE)
}