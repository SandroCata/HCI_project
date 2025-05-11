package com.example.budgify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceRepository
import com.example.budgify.applicationlogic.FinanceViewModel


//Flag per andare direttamente alla home oppure passare per lo sblocco con pin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for a saved PIN on app startup
        val savedPin = getSavedPin()

        setContent {
            val application = application as FinanceApplication
            val database = application.database
            val repository = FinanceRepository(
                database.transactionDao(),
                database.accountDao(),
                database.goalDao()
            )
            val financeViewModel: FinanceViewModel =
                viewModel(factory = FinanceViewModel.FinanceViewModelFactory(repository))

            // Pass the initial PIN state to the NavGraph
            NavGraph(
                viewModel = financeViewModel
            )
        }
    }

    private fun getSavedPin(): String? {
        return try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                this,
                "AppSettings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            sharedPreferences.getString("access_pin", null)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading PIN", e)
            null
        }
    }
}