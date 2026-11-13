package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.i18n.LocalAppStrings

@Composable
fun TransactionsScreen(
  transactions: List<TransactionEntity>,
  categories: List<CategoryEntity>,
  currencySymbol: String,
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  selectedTypeFilter: TransactionType?,
  onTypeFilterChanged: (TransactionType?) -> Unit,
  selectedCategoryId: Long?,
  onCategoryFilterChanged: (Long?) -> Unit,
  onDeleteTransaction: (TransactionEntity) -> Unit,
  onOpenAddTransaction: () -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

  // Filtered transactions
  val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, selectedCategoryId) {
    transactions.filter { item ->
      val matchesSearch = searchQuery.isBlank() ||
        item.title.contains(searchQuery, ignoreCase = true) ||
        item.note.contains(searchQuery, ignoreCase = true)

      val matchesType = selectedTypeFilter == null || item.type == selectedTypeFilter
      val matchesCat = selectedCategoryId == null || item.categoryId == selectedCategoryId

      matchesSearch && matchesType && matchesCat
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    floatingActionButton = {
      SmallFloatingActionButton(
        onClick = onOpenAddTransaction,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_add_transaction")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = strings.addTransaction,
          tint = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.size(24.dp)
        )
      }
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      Text(
        text = strings.allTransactions,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
      )

      // Search Field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text(strings.searchTransactions) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("transaction_search_input")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Filter Chips Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedTypeFilter == null && selectedCategoryId == null,
          onClick = {
            onTypeFilterChanged(null)
            onCategoryFilterChanged(null)
          },
          label = { Text("${strings.filterAll} (${transactions.size})") },
          shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
          selected = selectedTypeFilter == TransactionType.EXPENSE,
          onClick = {
            onTypeFilterChanged(if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE)
          },
          label = { Text(strings.filterExpenses) },
          shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
          selected = selectedTypeFilter == TransactionType.INCOME,
          onClick = {
            onTypeFilterChanged(if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME)
          },
          label = { Text(strings.filterIncome) },
          shape = RoundedCornerShape(12.dp)
        )

        categories.forEach { category ->
          val isSelected = selectedCategoryId == category.id
          FilterChip(
            selected = isSelected,
            onClick = {
              onCategoryFilterChanged(if (isSelected) null else category.id)
            },
            label = { Text(category.name) },
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // List or Empty View
      if (filteredTransactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.FilterList,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = strings.noMatchingTransactions,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(filteredTransactions, key = { it.id }) { item ->
            TransactionRowItem(
              transaction = item,
              categories = categories,
              currencySymbol = currencySymbol,
              showTime = true,
              onDelete = { transactionToDelete = item }
            )
          }

          item {
            Spacer(modifier = Modifier.height(72.dp))
          }
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (transactionToDelete != null) {
    val target = transactionToDelete!!
    AlertDialog(
      onDismissRequest = { transactionToDelete = null },
      title = { Text(strings.deleteTransaction) },
      text = { Text("Are you sure you want to delete '${target.title}' ($currencySymbol${String.format("%.2f", target.amount)})? Your account balance will be updated.") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteTransaction(target)
            transactionToDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text(strings.delete, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { transactionToDelete = null }) {
          Text(strings.cancel)
        }
      }
    )
  }
}
