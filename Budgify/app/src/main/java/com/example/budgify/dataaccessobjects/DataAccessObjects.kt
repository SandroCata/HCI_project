package com.example.budgify.dataaccessobjects

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgify.entities.Account
import com.example.budgify.entities.Objective
import com.example.budgify.entities.Transaction
import com.example.budgify.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): Flow<Transaction>
}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts ORDER BY title ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Int): Flow<Account>
}

@Dao
interface ObjectiveDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(objective: Objective)

    @Update
    suspend fun update(objective: Objective)

    @Delete
    suspend fun delete(objective: Objective)

    @Query("SELECT * FROM objectives ORDER BY startDate ASC")
    fun getAllGoalsByDate(): Flow<List<Objective>>

    @Query("SELECT * FROM objectives WHERE id = :id")
    fun getGoalById(id: Int): Flow<Objective>
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM objectives WHERE id = :id")
    fun getCategoryById(id: Int): Flow<Category>
}