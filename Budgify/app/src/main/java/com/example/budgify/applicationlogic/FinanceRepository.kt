package com.example.budgify.applicationlogic

import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val objectiveDao: ObjectiveDao,
    private val categoryDao: CategoryDao
) {

    // TRANSACTIONS
//    fun getAllTransactions(): Flow<List<MyTransaction>> {
//        return transactionDao.getAllTransactions()
//    }
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllTransactionsWithDetails()
    }
    suspend fun getTransactionById(transactionId: Int): MyTransaction? = transactionDao.getTransactionById(transactionId)

    suspend fun insertTransaction(myTransaction: MyTransaction) {
        transactionDao.insert(myTransaction)
    }
    suspend fun updateTransaction(myTransaction: MyTransaction) {
        transactionDao.update(myTransaction)
    }
    suspend fun deleteTransaction(myTransaction: MyTransaction) {
        transactionDao.delete(myTransaction)
    }

    // OBJECTIVES
    fun getAllObjectives(): Flow<List<Objective>> {
        return objectiveDao.getAllGoalsByDate()
    }
    suspend fun insertObjective(objective: Objective) {
        objectiveDao.insert(objective)
    }
    fun getObjectiveById(id: Int): Flow<Objective> {
        return objectiveDao.getGoalById(id)
    }
    suspend fun updateObjective(objective: Objective) {
        objectiveDao.update(objective)
    }
    suspend fun deleteObjective(objective: Objective) {
        objectiveDao.delete(objective)
    }

    //CATEGORIES
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }
    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }
    fun getCategoryById(id: Int): Flow<Category> {
        return categoryDao.getCategoryById(id)
    }
    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }
    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    // ACCOUNTS
    fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts()
    }
    suspend fun insertAccount(account: Account) {
        accountDao.insert(account)
    }
    // Inside your FinanceRepository class
    suspend fun getAccountById(accountId: Int): Account? = accountDao.getAccountById(accountId)
    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }
    suspend fun deleteAccount(account: Account) {
        accountDao.delete(account)
    }

    suspend fun updateAccountBalance(accountId: Int) {
        // Get the account from the database
        val account = accountDao.getAccountById(accountId)

        if (account != null) {
            // Get all transactions for this account
            val transactionsForAccount = transactionDao.getTransactionsForAccount(accountId)

            // Calculate the new balance
            var newBalance = 0.0
            transactionsForAccount.forEach { transaction ->
                newBalance += if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
            }

            // Update the account's amount in the database
            val updatedAccount = account.copy(amount = account.initialAmount + newBalance)
            accountDao.update(updatedAccount)
        }
    }
}