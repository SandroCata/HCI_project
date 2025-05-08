package com.example.budgify.datastruct

import java.time.LocalDate

enum class ObjectiveType {
    EXPENSE,
    INCOME
}

data class Objective(val type: ObjectiveType, val desc: String, val amount: Double, val date: LocalDate) {

}
