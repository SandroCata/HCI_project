package com.example.budgify.applicationlogic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

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

    fun completeObjectiveAndCreateTransaction(objective: Objective, accountId: Int, categoryId: Int? = null) { // Added optional categoryId
        viewModelScope.launch {
            if (!objective.completed) {
                // 1. Mark objective as completed
                val updatedObjective = objective.copy(completed = true)
                repository.updateObjective(updatedObjective) // You already have this in your repository

                // 2. Create a transaction
                val transactionType = when (objective.type) {
                    ObjectiveType.INCOME -> TransactionType.INCOME
                    ObjectiveType.EXPENSE -> TransactionType.EXPENSE
                }

                val newTransaction = MyTransaction(
                    // id will be auto-generated by Room
                    accountId = accountId, // This will come from the UI
                    type = transactionType,
                    date = LocalDate.now(), // Or objective.endDate, if preferred
                    description = "Objective: ${objective.desc}",
                    amount = objective.amount,
                    categoryId = categoryId // Pass the selected categoryId, or null
                )
                // Use your existing addTransaction function which also handles updating account balance
                addTransaction(newTransaction)

                // The UI should react to changes in allObjectives and allTransactionsWithDetails StateFlows
                // No explicit refresh needed here if your UI collects these flows.
            }
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
    val hasAccounts: Flow<Boolean> = allAccounts.map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false // Or true, depending on your initial state logic
        )

    /**
     * Updates an account's title and initial amount, then recalculates its current balance
     * based on all its associated transactions.
     *
     * @return true if the update and recalculation were successful, false otherwise.
     */
    suspend fun updateAccountAndRecalculateBalance(
        accountId: Int,
        newTitle: String,
        newInitialAmount: Double
    ): Boolean {
        return viewModelScope.async { // Usa async se vuoi restituire un risultato
            // 1. Recupera l'account esistente (opzionale, potresti passare l'account intero)
            val accountToUpdate = repository.getAccountById(accountId) // Dovrai aggiungere getAccountById al DAO e Repository
            if (accountToUpdate == null) {
                Log.e("FinanceViewModel", "Account with ID $accountId not found for update.")
                return@async false
            }

            // 2. Recupera tutte le transazioni per questo account
            //    Assicurati che transactionsByAccountId restituisca un List<MyTransaction> e non un Flow per questo uso specifico,
            //    oppure raccogli il primo valore dal Flow.
            val transactionsForAccount = repository.getTransactionsForAccount(accountId) // Dovrai creare questo metodo

            // 3. Calcola il delta delle transazioni
            var transactionsDelta = 0.0
            transactionsForAccount.forEach { transaction ->
                if (transaction.type == TransactionType.INCOME) {
                    transactionsDelta += transaction.amount
                } else {
                    transactionsDelta -= transaction.amount
                }
            }

            // 4. Calcola il nuovo 'amount' (saldo corrente)
            val newCurrentAmount = newInitialAmount + transactionsDelta

            // 5. Crea l'oggetto Account aggiornato
            val updatedAccount = accountToUpdate.copy(
                title = newTitle,
                initialAmount = newInitialAmount,
                amount = newCurrentAmount // Imposta l'amount ricalcolato
            )

            // 6. Aggiorna l'account nel database
            repository.updateAccount(updatedAccount)
            Log.d("FinanceViewModel", "Account $accountId updated. New Initial: $newInitialAmount, New Current: $newCurrentAmount")
            true
        }.await() // Aspetta il risultato dell'operazione asincrona
    }


    // LOANS --- Nuova sezione per i Prestiti ---
    val allLoans: StateFlow<List<Loan>> = repository.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalCreditLoans: StateFlow<Double> = allLoans
        .map { loans ->
            loans.filter { it.type == LoanType.CREDIT }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalDebtLoans: StateFlow<Double> = allLoans
        .map { loans ->
            loans.filter { it.type == LoanType.DEBT }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val lastThreeLoans: StateFlow<List<Loan>> = allLoans
        .map { loans ->
            // Il DAO già ordina per startDate DESC, quindi prendiamo solo i primi 3
            loans.takeLast(6).reversed()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addLoan(loan: Loan) {
        viewModelScope.launch {
            repository.insertLoan(loan)
            // Qui potresti voler aggiornare qualche altro stato se i prestiti influenzano
            // altri aspetti, come il saldo generale, ma per ora ci concentriamo solo sui prestiti.
        }
    }

    fun updateLoan(loan: Loan) {
        viewModelScope.launch {
            repository.updateLoan(loan)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }
    // --- Fine sezione LOANS ---

    /**
     * Provides a StateFlow map of categories to their total expense amounts for a given account.
     * The map is sorted by amount in descending order.
     */
    fun getExpenseDistributionForAccount(accountId: Int): StateFlow<Map<Category, Double>> {
        return allTransactionsWithDetails
            .map { transactionsWithDetailsList ->
                transactionsWithDetailsList
                    .filter { it.transaction.accountId == accountId &&
                            it.transaction.type == TransactionType.EXPENSE &&
                            it.category != null } // Filter for expenses of the account with a category
                    .groupBy { it.category!! } // Group by the Category object
                    .mapValues { entry ->
                        // Sum the amounts of transactions in each category group
                        entry.value.sumOf { transactionWithDetail -> transactionWithDetail.transaction.amount }
                    }
                    .toList() // Convert to list of pairs for sorting
                    .sortedByDescending { it.second } // Sort by amount descending
                    .toMap() // Convert back to a map (LinkedHashMap preserves order)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap() // Initial value while loading or if no data
            )
    }

    fun getIncomeDistributionForAccount(accountId: Int): StateFlow<Map<Category, Double>> {
        return allTransactionsWithDetails
            .map { transactionsWithDetailsList ->
                transactionsWithDetailsList
                    .filter { it.transaction.accountId == accountId &&
                            it.transaction.type == TransactionType.INCOME &&
                            it.category != null } // Filter for incomes of the account with a category
                    .groupBy { it.category!! } // Group by the Category object
                    .mapValues { entry ->
                        // Sum the amounts of transactions in each category group
                        entry.value.sumOf { transactionWithDetail -> transactionWithDetail.transaction.amount }
                    }
                    .toList() // Convert to list of pairs for sorting
                    .sortedByDescending { it.second } // Sort by amount descending
                    .toMap() // Convert back to a map (LinkedHashMap preserves order)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap() // Initial value while loading or if no data
            )
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