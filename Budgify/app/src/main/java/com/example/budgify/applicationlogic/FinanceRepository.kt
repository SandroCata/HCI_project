package com.example.budgify.applicationlogic

import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.CategoryDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.Objective
import com.example.budgify.entities.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val transactionDao: TransactionDao, private val accountDao: AccountDao, private val objectiveDao: ObjectiveDao, private val categoryDao: CategoryDao) {

    // Esempio di funzione per ottenere tutte le transazioni
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    // Esempio di funzione per inserire una transazione
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    // Esempio di funzione per ottenere un account per ID
    fun getAccountById(id: Int): Flow<Account> {
        return accountDao.getAccountById(id)
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

}
