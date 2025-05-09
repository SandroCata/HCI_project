package com.example.budgify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgify.dataaccessobjects.AccountDao
import com.example.budgify.dataaccessobjects.ObjectiveDao
import com.example.budgify.dataaccessobjects.TransactionDao
import com.example.budgify.entities.Account
import com.example.budgify.entities.Objective
import androidx.room.TypeConverter
import com.example.budgify.entities.Transaction
import com.example.budgify.entities.TransactionType
import java.time.LocalDate
import java.util.Date

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    // Esempio per TransactionType (Enum)
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(name: String): TransactionType {
        return enumValueOf<TransactionType>(name)
    }
}

@Database(entities = [Transaction::class, Account::class, Objective::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Se hai bisogno di TypeConverters
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): ObjectiveDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database" // Il nome del tuo file database
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not covered in this codelab.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}