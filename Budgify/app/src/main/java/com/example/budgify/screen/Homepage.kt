package com.example.budgify.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.budgify.datastruct.Account
import com.example.budgify.datastruct.Transaction
import com.example.budgify.routes.ScreenRoutes
import java.time.LocalDate

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

val itemsTopBar = listOf(
    ScreenRoutes.Home,
    ScreenRoutes.Settings
)

@Composable
fun Homepage(navController: NavController) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Home.route) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
    ){
        innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Box per i pie chart e istogramma
                GraficiBox()

                // Box per i conti e il saldo totale
                ContiBox()

                LastTransactionBox()
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
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.date.toString(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "${transaction.amount}€",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun LastTransactionBox() {
    // Qui simuliamo i dati da un file locale, che poi andranno presi da un DB
    val transactions = listOf(
        Transaction(1, "Bank",false, LocalDate.now(), "Spesa Alimentari", 50.0, "Spesa"),
        Transaction(2, "Bank",false, LocalDate.now().minusDays(1), "Trasporto", 25.0, "Spesa"),
        Transaction(3, "Bank",true, LocalDate.now().minusDays(3), "Stipendio", 1500.0, "Entrata"),
        Transaction(6, "Bank",true, LocalDate.now().minusDays(5), "Lavoro", 100.0, "Entrata"),
        Transaction(7, "Bank",false, LocalDate.now().minusDays(6), "Spesa Alimentari", 65.0, "Spesa"),
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
                text = "Ultime Transazioni",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(transactions.take(5)) { transaction ->
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
        Account("Conto Corrente", 1000.0),
        Account("Conto Risparmio", 5000.0),
        Account("Carta di Credito", 2500.0),
        Account("Wallet", 150.0)
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
                text = "Saldo Totale: $totalBalance €",
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
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { /* TODO: Handle add account logic */ }) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Account",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

//Composbale per visualizzare le sezioni di ogni account
@Composable
fun AccountItem(account: Account) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = account.title, fontWeight = FontWeight.Bold)
        Text(text = "${account.amount}€", color = Color.Gray)
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
                text = "Grafici",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Qui puoi aggiungere la logica per visualizzare i grafici
            Text("Grafico a torta...")
            Text("Istogramma...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, currentRoute: String) {
    val title = when (currentRoute) {
        "home_screen" -> "Dashboard"
        "objectives_screen" -> "Objectives"
        "settings_screen" -> "Settings"
        "transactions_screen" -> "Transactions"
        "cred_deb_screen" -> "Loans"
        "categories_screen" -> "Categories"
        else -> ""
    }
    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 30.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.LightGray,
            titleContentColor = Color.Black
        ),
        navigationIcon = {
            if (currentRoute != ScreenRoutes.Home.route) {
                IconButton(onClick = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(50.dp))
                }
            }
        },
        actions = { // Aggiunte le icone in alto a destra
            IconButton(onClick = {
                navController.navigate(ScreenRoutes.Settings.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                Icon( Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(50.dp))
            }

        }
    )
}

@Composable
fun BottomBar(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(bottom = 0.dp)
    ) {
        NavigationBar(modifier = Modifier.align(Alignment.BottomCenter)) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val desc = ""
            items.forEach { screen ->
                    Modifier.size(35.dp) // Icona standard per le altre schermate
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = screen.icon),
                            contentDescription = null
                        )
                    },
                    label = {
                            Text(screen.title, style = smallTextStyle)
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        if (screen == ScreenRoutes.Adding) {
                            showDialog = true
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

