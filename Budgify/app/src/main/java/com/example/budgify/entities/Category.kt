package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "categories")
data class Category(@PrimaryKey(autoGenerate = true) val id: Int = 0, val type: CategoryType, val desc: String)