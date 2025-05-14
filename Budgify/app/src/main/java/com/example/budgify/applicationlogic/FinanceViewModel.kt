package com.example.budgify.applicationlogic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    // TODO: fix the balance update after transaction edit
    suspend fun updateTransaction(myTransaction: MyTransaction) {
        val oldTransaction = withContext(Dispatchers.IO) {
            repository.getTransactionById(myTransaction.id)
        }
        // val oldTransaction = repository.getTransactionById(myTransaction.id)
        //Log.d("FinanceViewModel", "Old Transaction: $oldTransaction")
        //Log.d("FinanceViewModel", "Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
        // Update the transaction in the database
        repository.updateTransaction(myTransaction)
        //Log.d("FinanceViewModel", "New Transaction: $myTransaction")
        // Now, after the transaction is updated, update the account balance(s)
        if (oldTransaction != null) {
            //Log.d("FinanceViewModel", "Old Transaction is not null")
            if (oldTransaction.accountId != myTransaction.accountId) {
                //Log.d("FinanceViewModel", "Account changed")
                // Account changed: Update balance of old and new accounts
                withContext(Dispatchers.IO) {
                    repository.updateAccountBalance(oldTransaction.accountId)
                    repository.updateAccountBalance(myTransaction.accountId)
                }
                //Log.d("FinanceViewModel", "1-Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
            } else {
                //Log.d("FinanceViewModel", "Account is the same")
                // Account is the same: Update balance of the current account
                withContext(Dispatchers.IO) {
                    repository.updateAccountBalance(myTransaction.accountId)
                }
                //Log.d("FinanceViewModel", "2-Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
            }
        } else {
            //Log.d("FinanceViewModel", "Old Transaction is null")
            // This case is unexpected for an update, but if the old transaction
            // wasn't found, we can still try to update the balance of the
            // account the transaction is currently associated with.
            withContext(Dispatchers.IO) {
                repository.updateAccountBalance(myTransaction.accountId)
            }
            //Log.d("FinanceViewModel", "3-Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
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