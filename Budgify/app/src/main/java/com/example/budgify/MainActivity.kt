package com.example.budgify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceRepository
import com.example.budgify.applicationlogic.FinanceViewModel


//Flag per andare direttamente alla home oppure passare per lo sblocco con pin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as FinanceApplication // Assicurati di avere una FinanceApplication class
            val database = application.database
            val repository = FinanceRepository(database.transactionDao(), database.accountDao(), database.goalDao())
            val financeViewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.FinanceViewModelFactory(repository))

            NavGraph(viewModel = financeViewModel)
        }
    }
}