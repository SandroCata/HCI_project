package com.example.budgify.applicationlogic

import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val transactionDao: TransactionDao, private val accountDao: AccountDao, private val objectiveDao: ObjectiveDao) {

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

    // ... altre funzioni per interagire con i DAO
}
