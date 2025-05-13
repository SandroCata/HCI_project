package com.example.budgify.applicationlogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Objective
import com.example.budgify.entities.MyTransaction
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {


    // TRANSACTIONS
    val allTransactions = repository.getAllTransactions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allTransactionsWithDetails = repository.getAllTransactionsWithDetails().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(myTransaction)
        }
    }

    fun updateTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(myTransaction)
        }
    }

    fun deleteTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(myTransaction)
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

    //CATEGORIES
    val allCategories = repository.getAllCategories().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // ACCOUNTS
    val allAccounts = repository.getAllAccounts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

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