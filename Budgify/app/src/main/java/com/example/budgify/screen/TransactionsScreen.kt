package com.example.budgify.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgify.datastruct.Transaction
import com.example.budgify.routes.ScreenRoutes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(navController: NavController) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Transactions.route) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
    ){
            innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top, // Distribute space between sections
            horizontalAlignment = Alignment.CenterHorizontally
        ) {



                // Qui inserisci il calendario
                MonthlyCalendar(
                    onDaySelected = { selectedDate ->
                        // TODO: Qui gestisci cosa succede quando un giorno è selezionato.
                        // Ad esempio, potresti caricare le transazioni per quella data.
                        println("Giorno selezionato in TransactionsScreen: $selectedDate")
                    }
                )



                TransactionBox() // Aggiungi le ultime transazioni qui

            }


        }
    }


@Composable
fun MonthlyCalendar(
    onDaySelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }

    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun")

    // Calcola l'offset per allineare il primo giorno del mese
    val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 // 1 per lunedì, 7 per domenica
    val startOffset = if (firstDayOfWeekValue == 0) 6 else firstDayOfWeekValue - 1 // 0-6 per allineamento con Lun-Dom

    val daysOfMonth = (1..daysInMonth).map { currentMonth.atDay(it) }
    val calendarDays = (List(startOffset) { null } + daysOfMonth).map { it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        // Header del calendario (mese e navigazione)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mese Precedente")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Mese Successivo")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nomi dei giorni della settimana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Griglia dei giorni del mese
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7 colonne per i giorni della settimana
            contentPadding = PaddingValues(0.dp),
            userScrollEnabled = false // Disabilita lo scroll verticale per il calendario
        ) {
            items(calendarDays.size) { index ->
                val day = calendarDays[index]
                val isSelected = selectedDate.value == day
                val isToday = day == LocalDate.now()

                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // Rende le celle quadrate
                        .padding(2.dp)
                        .clickable(enabled = day != null) {
                            if (day != null) {
                                selectedDate.value = day
                                onDaySelected(day)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                        ) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> LocalContentColor.current
                                },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun TransactionBox() {
    // Qui simuliamo i dati da un file locale, che poi andranno presi da un DB
    val transactions = listOf(
        Transaction(1, "Bank",false, LocalDate.now(), "Spesa Alimentari", 50.0, "Spesa"),
        Transaction(2, "Bank",false, LocalDate.now().minusDays(1), "Trasporto", 25.0, "Spesa"),
        Transaction(3, "Bank",true, LocalDate.now().minusDays(3), "Stipendio", 1500.0, "Entrata"),
        Transaction(6, "Bank",true, LocalDate.now().minusDays(5), "Lavoro", 100.0, "Entrata"),
        Transaction(7, "Bank",false, LocalDate.now().minusDays(6), "Spesa Alimentari", 65.0, "Spesa"),
        Transaction(7, "Bank",false, LocalDate.now().minusDays(6), "Spesa Alimentari", 65.0, "Spesa"),
        Transaction(7, "Bank",false, LocalDate.now().minusDays(6), "Spesa Alimentari", 65.0, "Spesa"),
        Transaction(7, "Bank",false, LocalDate.now().minusDays(6), "Spesa Alimentari", 65.0, "Spesa"),
        Transaction(7, "Bank",false, LocalDate.now().minusDays(6), "Spesa Alimentari", 65.0, "Spesa"),
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
                .fillMaxHeight()
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
                items(transactions.take(10)) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}





