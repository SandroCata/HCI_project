package com.example.budgify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent



//Flag per andare direttamente alla home oppure passare per lo sblocco con pin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setContent {
                NavGraph()
            }
        }
    }
}