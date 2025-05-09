package com.example.budgify.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Transaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.routes.ScreenRoutes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Transactions.route) }
    // State to hold the selected date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController) }
    ){
            innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top, // Distribute space between sections
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                // Pass the onDaySelected lambda to update the selectedDate state
                MonthlyCalendar(
                    onDaySelected = { date ->
                        selectedDate = date // Update the selected date state
                    }
                )
            }

                item {
                    // Pass the selected date to TransactionBox
                    TransactionBox(selectedDate = selectedDate)
                }
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

        // Grid of the month days - Use a simple Column with Rows instead of LazyVerticalGrid
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Divide the calendarDays list into rows of 7 days
            val weeks = calendarDays.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Add empty boxes for padding at the start if the first week is not full
                    val daysInWeek = week.size
                    val emptySlots = 7 - daysInWeek

                    week.forEach { day ->
                        val isSelected = selectedDate.value == day
                        val isToday = day == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f) // Give each day equal weight in the row
                                .aspectRatio(1f) // Keep the aspect ratio
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
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add empty boxes to fill the remaining space in the last row
                    repeat(emptySlots) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Composbale per visualizzare gli ultimi movimenti
@Composable
fun TransactionBox(selectedDate: LocalDate?) {
    // Here we simulate data from a local file, which will then be taken from a DB
    val allTransactions = listOf(
        Transaction(1, "Bank", TransactionType.EXPENSE, LocalDate.now(), "Spesa Alimentari", 50.00, "Cibo"),
        Transaction(2, "Wallet",TransactionType.EXPENSE, LocalDate.now(), "Biglietto Autobus", 2.00, "Trasporto"),
        Transaction(3, "Bank",TransactionType.INCOME, LocalDate.now().minusDays(1), "Mensilitá lavoro", 1500.00, "Stipendio"),
        Transaction(6, "Wallet",TransactionType.INCOME, LocalDate.now().minusDays(1), "Maglietta Vinted", 10.00, "Vendite"),
        Transaction(7, "Bank",TransactionType.EXPENSE, LocalDate.now().minusDays(1), "Abbonamento Palestra", 65.00, "Allenamento"),
        Transaction(7, "Wallet",TransactionType.EXPENSE, LocalDate.now().minusDays(3), "Cinema", 65.00, "Svago"),
        Transaction(7, "Bank",TransactionType.EXPENSE, LocalDate.now().minusDays(5), "Spesa Alimentari", 65.00, "Cibo"),
        Transaction(7, "Wallet",TransactionType.EXPENSE, LocalDate.now().minusDays(2), "Spesa Alimentari", 65.00, "Cibo"),
        Transaction(7, "Bank",TransactionType.EXPENSE, LocalDate.now().minusDays(2), "Spesa Alimentari", 65.00, "Cibo"),
        Transaction(7, "Wallet",TransactionType.EXPENSE, LocalDate.now().minusDays(1), "Spesa Alimentari", 65.00, "Cibo"),
    )

    // Filter transactions by the selected date
    val transactionsForSelectedDate = if (selectedDate != null) {
        allTransactions.filter { it.date == selectedDate }
    } else {
        // If no date is selected, maybe show the latest transactions
        // or an empty list, depending on your desired behavior.
        // For now, let's show all transactions if no date is selected.
        // You might want to adjust this.
        allTransactions.take(10) // Keep showing latest 10 if no date is selected
    }

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
                text = if (selectedDate != null) "${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}" else "Latest",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // Display filtered transactions
                if (transactionsForSelectedDate.isEmpty() && selectedDate != null) {
                    Text("No transactions for this date.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    transactionsForSelectedDate.forEach { transaction ->
                        TransactionItem1(transaction = transaction)
                    }
                }
            }
        }
    }
}

// Assume TransactionItem is a composable that displays a single transaction
@Composable
fun TransactionItem1(transaction: Transaction) {
    // Your existing TransactionItem implementation
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val formattedDescription1 = buildAnnotatedString {
                // Aggiungi la descrizione in grassetto
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.description)
                }
                // Aggiungi lo spazio e la parentesi aperta
                append("  (")
                // Aggiungi la categoria in corsivo
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(transaction.category)
                }
                // Aggiungi la parentesi chiusa
                append(")")
            }
            Text(text = formattedDescription1, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(3.dp))
            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.account)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(transaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(text = formattedDescription2, fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
        }
        Text(
            text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} ${transaction.amount}€",
            color = if (transaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )
    }
}





