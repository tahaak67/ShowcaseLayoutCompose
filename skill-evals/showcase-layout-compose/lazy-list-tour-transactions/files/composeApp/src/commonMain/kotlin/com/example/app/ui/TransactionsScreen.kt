package com.example.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.data.SettingsStore

data class Transaction(val id: Int, val merchant: String, val amount: String, val date: String)

private val transactions = List(60) { i ->
    Transaction(i, listOf("Coffee Corner", "Grocer", "Metro", "Bookshop", "Pharmacy")[i % 5], "-$${(i * 7 % 90) + 3}.50", "Oct ${30 - i / 2}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(settings: SettingsStore, onExportCsv: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Transactions") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item(key = "summary") { SummaryCard(spent = "$1,284.50", budget = "$1,600") }
            items(transactions, key = { it.id }) { tx -> TransactionRow(tx) }
            item(key = "export") {
                OutlinedButton(onClick = onExportCsv, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Text("Export CSV", Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(spent: String, budget: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("October", style = MaterialTheme.typography.labelLarge)
            Text("$spent spent", style = MaterialTheme.typography.headlineSmall)
            Text("of $budget budget", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(tx.merchant) },
        supportingContent = { Text(tx.date) },
        trailingContent = { Text(tx.amount) },
        modifier = modifier,
    )
}
