package com.example.budgify.datastruct

import java.time.LocalDate

class Transaction(val id: Int, val account: String, val type: Boolean, val date: LocalDate, val description: String, val amount: Double, val category: String) {
}