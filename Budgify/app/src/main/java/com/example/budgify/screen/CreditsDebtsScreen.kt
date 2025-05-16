package com.example.budgify.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Importazione corretta per LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Importazione corretta per la delega by
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Importazione corretta
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel // Assumi il percorso corretto
import com.example.budgify.entities.Loan // Assumi il percorso corretto
import com.example.budgify.entities.LoanType // Assumi il percorso corretto
import com.example.budgify.navigation.BottomBar // Assumi il percorso corretto
import com.example.budgify.navigation.TopBar // Assumi il percorso corretto
import com.example.budgify.routes.ScreenRoutes // Assumi il percorso corretto
import java.time.format.DateTimeFormatter

@Composable
fun CreditsDebitsScreen(navController: NavController, viewModel: FinanceViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.CredDeb.route) }
    val totalCredits by viewModel.totalCreditLoans.collectAsStateWithLifecycle()
    val totalDebits by viewModel.totalDebtLoans.collectAsStateWithLifecycle()
    val lastThreeLoans by viewModel.lastThreeLoans.collectAsStateWithLifecycle()

    // Stato per mostrare il dialogo di conferma eliminazione
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var loanToDelete by remember { mutableStateOf<Loan?>(null) }

    Scaffold(
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = { BottomBar(navController, viewModel) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableAmountArea(
                    title = "Total Credits",
                    amount = totalCredits,
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = Color(0xFF4CAF50),
                    onClick = {
                        navController.navigate(ScreenRoutes.CredDebManagement.route)
                    }
                )
                ClickableAmountArea(
                    title = "Total Debts",
                    amount = totalDebits,
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = Color(0xFFF44336),
                    onClick = {
                        navController.navigate(ScreenRoutes.CredDebManagement.route)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Recent Loans",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (lastThreeLoans.isEmpty()) {
                Text("No recent loans recorded.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lastThreeLoans, key = { loan -> loan.id }) { loan ->
                        LoanRow(
                            loan = loan,
                            onLongPress = { // Passa la lambda per il long press
                                loanToDelete = loan // Imposta il prestito da eliminare
                                showDeleteConfirmationDialog = true // Mostra il dialogo
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogo di conferma eliminazione
    if (showDeleteConfirmationDialog && loanToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                loanToDelete = null
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${loanToDelete!!.desc}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLoan(loanToDelete!!) // Chiama il metodo del ViewModel
                        showDeleteConfirmationDialog = false
                        loanToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        loanToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClickableAmountArea(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "%.2f €".format(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class) // Aggiungi questa annotazione
@Composable
fun LoanRow(
    loan: Loan,
    onLongPress: (Loan) -> Unit // Lambda per gestire il long press
) {
    val amountColor = if (loan.type == LoanType.CREDIT) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (loan.type == LoanType.CREDIT) "+" else "-"
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable( // Usa combinedClickable
                onClick = {
                    // Azione per il click normale (se necessaria, altrimenti lasciala vuota)
                    // Ad esempio: navController.navigate("loan_details_screen/${loan.id}")
                },
                onLongClick = { // Gestisci il long click qui
                    onLongPress(loan)
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)) {
                Text(
                    text = loan.desc,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = loan.startDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$sign%.2f €".format(loan.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
