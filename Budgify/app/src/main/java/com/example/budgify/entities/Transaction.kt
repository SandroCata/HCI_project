package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val account: String,
    val type: TransactionType,
    val date: LocalDate,
    val description: String,
    val amount: Double,
    val category: Category
)