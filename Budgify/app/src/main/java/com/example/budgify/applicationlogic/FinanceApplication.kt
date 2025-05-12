package com.example.budgify.applicationlogic

import android.app.Application
import com.example.budgify.database.FinanceDatabase // Assicurati che l'import per FinanceDatabase sia corretto
import com.example.budgify.applicationlogic.FinanceRepository // Assicurati che l'import per FinanceRepository sia corretto

class FinanceApplication : Application() {
    // Utilizza lazy per inizializzare il database solo quando è necessario.
    // Questo assicura che l'istanza del database venga creata la prima volta che si accede a 'database'.
    val database: FinanceDatabase by lazy {
        // Chiama il metodo getDatabase del companion object di FinanceDatabase,
        // passandogli il contesto dell'applicazione.
        FinanceDatabase.getDatabase(this)
    }

    // Inizializza il repository, passandogli i DAO ottenuti dall'istanza del database.
    // Anche qui usiamo lazy per inizializzare il repository solo quando necessario.
    val repository: FinanceRepository by lazy {
        FinanceRepository(database.transactionDao(), database.accountDao(), database.goalDao(), database.categoryDao())
    }

    // Potresti aggiungere qui altre inizializzazioni a livello di applicazione se necessario.
}