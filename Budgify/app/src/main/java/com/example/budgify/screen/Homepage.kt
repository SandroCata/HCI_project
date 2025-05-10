package com.example.budgify.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgify.BottomBar
import com.example.budgify.TopBar
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.Transaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.routes.ScreenRoutes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Definisci gli stili del testo
val smallTextStyle = TextStyle(fontSize = 11.8.sp)

// Definisco la lista di schermate
val items = listOf(
    ScreenRoutes.Transactions,
    ScreenRoutes.Objectives,
    ScreenRoutes.Adding,
    ScreenRoutes.CredDeb,
    ScreenRoutes.Categories
)

@Composable
fun Homepage(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Home.route) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
    ){
        innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Box per i pie chart e istogramma
                item {
                    GraficiBox()
                }

                // Box per i conti e il saldo totale
                item {
                    ContiBox()
                }

                item {
                    LastTransactionBox()
                }
            }
        }
}

//Composbale per visualizzare le transazioni
@Composable
fun TransactionItem(transaction: Transaction) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            val formattedDescription1 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.description)
                }
                append("  (")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(transaction.category)
                }
                append(")")
            }
            Text(
                text = formattedDescription1,
                style = MaterialTheme.typography.bodyMedium,
            )
            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.account)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(
                text = formattedDescription2,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} ${transaction.amount}€",
            color = if (transaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun LastTransactionBox() {
    // Qui simuliamo i dati da un file locale, che poi andranno presi da un DB
    val transactions = listOf(
        Transaction(1, "Bank",TransactionType.EXPENSE, LocalDate.now(), "Spesa Alimentari", 50.0, "Cibo"),
        Transaction(2, "Wallet",TransactionType.EXPENSE, LocalDate.now().minusDays(1), "Biglietto Autobus", 2.0, "Trasporto"),
        Transaction(3, "Bank",TransactionType.INCOME, LocalDate.now().minusDays(3), "Mensilitá lavoro", 1500.0, "Stipendio"),
        Transaction(6, "Bank",TransactionType.INCOME, LocalDate.now().minusDays(3), "Maglietta Vinted", 100.0, "Vendite"),
        Transaction(7, "Wallet",TransactionType.EXPENSE, LocalDate.now().minusDays(1), "Spesa Alimentari", 65.0, "Cibo"),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = "Latest Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                transactions.take(5).forEach { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

// Composbale per visualizzare la box dei conti
@Composable
fun ContiBox() {
    // Dati di esempio per i conti
    val accounts = listOf(
        Account(1,"Bank", 1000.0),
        Account(2,"Savings", 5000.0),
        Account(3, "Wallet", 150.0)
    )

    // Calcola il saldo totale
    val totalBalance = accounts.sumOf { it.amount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = "Total Balance: $totalBalance €",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            //Sezione scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                accounts.forEach { account ->
                    AccountItem(account = account)
                }
                // Aggiungi l'item con il "+"
                AddAccountItem()
            }
        }
    }
}

@Composable
fun AddAccountItem() {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                // TODO: Handle add account logic here
                // This lambda is executed when the Box is clicked.
                // For example:
            }
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Account",
            modifier = Modifier.size(40.dp)
        )
    }
}

//Composbale per visualizzare le sezioni di ogni account
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(account: Account) {

    var isLongPressed by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .padding(8.dp)
        .width(150.dp)
        .height(65.dp)
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            onClick = {
                if (isLongPressed) {
                    isLongPressed = false // Reset if long pressed and clicked again
                } else {
                    // Handle regular click
                }
            },
            onLongClick = {
                isLongPressed = true // Set state on long press
            }
        )){
        Column(
            modifier = Modifier
                .width(150.dp)
                .height(65.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = account.title, fontWeight = FontWeight.Bold)

            Text(text = "${account.amount}€", color = Color.Gray)
        }

        if (isLongPressed) {
            IconButton(
                onClick = {
                    // Handle account removal
                    isLongPressed = false // Reset state after action
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.RemoveCircle, contentDescription = "Remove Account")
            }
        }
    }


}

// Composbale per visualizzare la box dei grafici
@Composable
fun GraficiBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = "Graphs",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Qui puoi aggiungere la logica per visualizzare i grafici
            Text("Pie charts...")
            Text("Histogram...")
        }
    }
}