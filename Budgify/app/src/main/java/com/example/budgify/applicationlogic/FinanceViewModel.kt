package com.example.budgify.applicationlogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Objective
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {


    // TRANSACTIONS
//    val allTransactions = repository.getAllTransactions().stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        emptyList()
//    )
    val allTransactionsWithDetails = repository.getAllTransactionsWithDetails().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    fun addTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(myTransaction)
            repository.updateAccountBalance(myTransaction.accountId)
        }
    }
    suspend fun updateTransaction(myTransaction: MyTransaction) {
        // Fetch the old transaction to see if the account changed
        val oldTransaction = withContext(Dispatchers.IO) {
            repository.getTransactionById(myTransaction.id) // You'll need this in your Repository
        }

        repository.updateTransaction(myTransaction)

        // Update the balance of the new account if it's different
        if (oldTransaction != null && oldTransaction.accountId != myTransaction.accountId) {
            repository.updateAccountBalance(oldTransaction.accountId)
            repository.updateAccountBalance(myTransaction.accountId)
        } else {
            // If the account is the same, just update the balance of that account
            repository.updateAccountBalance(myTransaction.accountId)
        }
    }
    fun deleteTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(myTransaction)
            repository.updateAccountBalance(myTransaction.accountId)
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