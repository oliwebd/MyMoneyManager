package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenAddTransaction,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        modifier = Modifier.testTag("fab_add_transaction")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Transaction",
          tint = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.size(28.dp)
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
        text = "All Transactions",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
      )

      // Search Field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Search transactions, payees...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("transaction_search_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

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
          label = { Text("All (${transactions.size})") },
          shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
          selected = selectedTypeFilter == TransactionType.EXPENSE,
          onClick = {
            onTypeFilterChanged(if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE)
          },
          label = { Text("Expenses") },
          shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
          selected = selectedTypeFilter == TransactionType.INCOME,
          onClick = {
            onTypeFilterChanged(if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME)
          },
          label = { Text("Income") },
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

      Spacer(modifier = Modifier.height(12.dp))

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
              text = if (searchQuery.isNotBlank()) "No transactions found matching '$searchQuery'" else "No transactions in this category yet.",
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
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredTransactions, key = { it.id }) { item ->
            val cat = categories.find { it.id == item.categoryId }
            val catColor = try {
              Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#1A73E8"))
            } catch (_: Exception) {
              MaterialTheme.colorScheme.primary
            }
            val isIncome = item.type == TransactionType.INCOME
            val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(item.timestamp))

            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .background(catColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = CategoryIconHelper.getIcon(cat?.iconName ?: "category"),
                      contentDescription = null,
                      tint = catColor,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = item.title,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = cat?.name ?: "General",
                        style = MaterialTheme.typography.labelSmall,
                        color = catColor,
                        fontWeight = FontWeight.Medium
                      )
                      Text(
                        text = " • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    if (item.note.isNotBlank()) {
                      Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                      )
                    }
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "${if (isIncome) "+" else "-"}$currencySymbol${String.format("%.2f", item.amount)}",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (isIncome) Color(0xFF0F9D58) else Color(0xFFEA4335)
                    )
                    if (item.isRecurring) {
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                      ) {
                        Text(
                          text = "Recurring",
                          style = MaterialTheme.typography.labelSmall,
                          fontSize = 9.sp,
                          color = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.width(6.dp))
                  IconButton(
                    onClick = { transactionToDelete = item },
                    modifier = Modifier.size(36.dp).testTag("delete_transaction_${item.id}")
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
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
      title = { Text("Delete Transaction") },
      text = { Text("Are you sure you want to delete '${target.title}' ($currencySymbol${String.format("%.2f", target.amount)})? Your account balance will be updated.") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteTransaction(target)
            transactionToDelete = null
          },
          colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { transactionToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
