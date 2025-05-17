package com.example.budgify.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.routes.ScreenRoutes
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Transactions.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // State to hold the selected date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold (
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(
            navController,
            viewModel,
            showSnackbar = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        ) }
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
                    TransactionBox(selectedDate = selectedDate, viewModel = viewModel)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mese Precedente")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mese Successivo")
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
@OptIn(ExperimentalFoundationApi::class) // Opt-in for ExperimentalFoundationApi
@Composable
fun TransactionBox(selectedDate: LocalDate?, viewModel: FinanceViewModel) {
    // Collect all transactions with details from the ViewModel
    val allTransactionsWithDetails by viewModel.allTransactionsWithDetails.collectAsState(initial = emptyList())

    // Filter transactions by the selected date
    val transactionsForSelectedDate = if (selectedDate != null) {
        allTransactionsWithDetails.filter { it.transaction.date == selectedDate }
    } else {
        // If no date is selected, maybe show the latest transactions
        // or an empty list, depending on your desired behavior.
        // For now, let's show the latest 10 transactions if no date is selected.
        allTransactionsWithDetails.takeLast(5).reversed()
    }

    // State to hold the transaction to be edited and control dialog visibility
    var transactionToEdit by remember { mutableStateOf<MyTransaction?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

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
                text = if (selectedDate != null) selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) else "Latest",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // Display filtered transactions
                if (transactionsForSelectedDate.isEmpty() && selectedDate != null) {
                    Text("No transactions for this date.", style = MaterialTheme.typography.bodyMedium)
                } else if (transactionsForSelectedDate.isEmpty()) {
                    Text("No transactions found.", style = MaterialTheme.typography.bodyMedium)
                }
                else {
                    transactionsForSelectedDate.forEach { transactionWithDetails ->
                        // Pass the TransactionWithDetails object to the updated TransactionItem
                        TransactionItem1(
                            transactionWithDetails = transactionWithDetails,
                            onLongClick = { transaction ->
                                // Set the transaction to be edited and show the dialog
                                transactionToEdit = transaction
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Show the Edit Transaction Dialog if showEditDialog is true and transactionToEdit is not null
    if (showEditDialog && transactionToEdit != null) {
        EditTransactionDialog(
            transaction = transactionToEdit!!, // Pass the transaction to the dialog
            viewModel = viewModel,
            onDismiss = {
                showEditDialog = false
                transactionToEdit = null // Clear the transaction when dialog is dismissed
            }
        )
    }
}

// Assume TransactionItem is a composable that displays a single transaction
@OptIn(ExperimentalFoundationApi::class) // Opt-in for ExperimentalFoundationApi
@Composable
fun TransactionItem1(
    transactionWithDetails: TransactionWithDetails,
    onLongClick: (MyTransaction) -> Unit // Add a long click lambda parameter
) {
    val myTransaction = transactionWithDetails.transaction
    val account = transactionWithDetails.account
    val category = transactionWithDetails.category

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable( // Use combinedClickable
                onClick = {
                    // Handle regular click if needed (e.g., view details)
                },
                onLongClick = {
                    onLongClick(myTransaction) // Call the lambda on long click
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val formattedDescription1 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(myTransaction.description)
                }
                append("  (")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    // Access the description from the related Category object
                    append(category?.desc ?: "Uncategorized") // Use safe call and default if category is null
                }
                append(")")
            }
            Text(text = formattedDescription1, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(3.dp))
            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    // Access the title from the related Account object
                    append(account.title)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    // Date formatting remains the same
                    append(myTransaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(text = formattedDescription2, fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
        }
        Text(
            text = "${if (myTransaction.type == TransactionType.INCOME) "+" else "-"} ${myTransaction.amount}€",
            color = if (myTransaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.7f, green = 0.0f, blue = 0.0f),
            fontWeight = FontWeight.Bold
        )
    }
}





