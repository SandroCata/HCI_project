package com.example.budgify.applicationlogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.entities.Objective
import com.example.budgify.entities.Transaction
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // Espone i dati come StateFlow per la UI
    val allTransactions = repository.getAllTransactions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Funzione per aggiungere una transazione
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    // OBJECTIVES
    val allObjectives = repository.getAllObjectives().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addObjective(objective: Objective) {
        viewModelScope.launch {
            repository.insertObjective(objective)
        }
    }

    fun updateObjective(objective: Objective) {
        viewModelScope.launch {
            repository.updateObjective(objective)
        }
    }

    fun deleteObjective(objective: Objective) {
        viewModelScope.launch {
            repository.deleteObjective(objective)
        }
    }

    // ... altre funzioni per gestire i dati

    // Factory per creare l'istanza del ViewModel
    class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}