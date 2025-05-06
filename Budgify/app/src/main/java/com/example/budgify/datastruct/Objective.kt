package com.example.budgify.datastruct

import java.util.Date

enum class ObjectiveType {
    EXPENSE,
    INCOME
}

data class Objective(val type: ObjectiveType, val desc: String, val amount: Double, val date: Date) {

}
