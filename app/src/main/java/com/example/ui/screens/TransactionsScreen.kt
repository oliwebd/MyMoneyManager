package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun filterTransactions(
  transactions: List<TransactionEntity>,
  categories: List<CategoryEntity>,
  searchQuery: String,
  selectedTypeFilter: TransactionType?,
  selectedCategoryId: Long?,
  selectedDateRange: Pair<Long, Long>?
): List<TransactionEntity> {
  val trimmedQuery = searchQuery.trim()
  val categoryNames = categories.associate { it.id to it.name }
  val displayDateFormat = if (trimmedQuery.isNotEmpty()) {
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
  } else null
  val isoDateFormat = if (trimmedQuery.isNotEmpty()) {
    SimpleDateFormat("yyyy-MM-dd", Locale.US)
  } else null

  return transactions.filter { item ->
    val categoryName = categoryNames[item.categoryId].orEmpty()
    val matchesSearch = if (trimmedQuery.isEmpty()) {
      true
    } else {
      val dateText = displayDateFormat!!.format(Date(item.timestamp))
      val isoDateText = isoDateFormat!!.format(Date(item.timestamp))
      item.title.contains(trimmedQuery, ignoreCase = true) ||
        item.note.contains(trimmedQuery, ignoreCase = true) ||
        categoryName.contains(trimmedQuery, ignoreCase = true) ||
        dateText.contains(trimmedQuery, ignoreCase = true) ||
        isoDateText.contains(trimmedQuery, ignoreCase = true)
    }

    val matchesType = selectedTypeFilter == null || item.type == selectedTypeFilter
    val matchesCat = selectedCategoryId == null || item.categoryId == selectedCategoryId
    val matchesDate = selectedDateRange == null ||
      (item.timestamp >= selectedDateRange.first && item.timestamp <= selectedDateRange.second)

    matchesSearch && matchesType && matchesCat && matchesDate
  }
}

internal fun utcDateRangeToLocalBounds(
  startUtcMillis: Long,
  endUtcMillis: Long,
  localTimeZone: TimeZone = TimeZone.getDefault()
): Pair<Long, Long> {
  val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

  utcCal.timeInMillis = startUtcMillis
  val startLocal = Calendar.getInstance(localTimeZone).apply {
    set(
      utcCal.get(Calendar.YEAR),
      utcCal.get(Calendar.MONTH),
      utcCal.get(Calendar.DAY_OF_MONTH),
      0, 0, 0
    )
    set(Calendar.MILLISECOND, 0)
  }

  utcCal.timeInMillis = endUtcMillis
  val endLocal = Calendar.getInstance(localTimeZone).apply {
    set(
      utcCal.get(Calendar.YEAR),
      utcCal.get(Calendar.MONTH),
      utcCal.get(Calendar.DAY_OF_MONTH),
      23, 59, 59
    )
    set(Calendar.MILLISECOND, 999)
  }

  return if (startLocal.timeInMillis <= endLocal.timeInMillis) {
    startLocal.timeInMillis to endLocal.timeInMillis
  } else {
    endLocal.timeInMillis to startLocal.timeInMillis
  }
}

internal fun localMillisToUtcDateMillis(
  localMillis: Long,
  localTimeZone: TimeZone = TimeZone.getDefault()
): Long {
  val localCal = Calendar.getInstance(localTimeZone).apply {
    timeInMillis = localMillis
  }
  return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    clear()
    set(
      localCal.get(Calendar.YEAR),
      localCal.get(Calendar.MONTH),
      localCal.get(Calendar.DAY_OF_MONTH)
    )
  }.timeInMillis
}

internal fun utcDateMillisToLocalMillis(
  utcDateMillis: Long,
  timeOfDayFromMillis: Long,
  localTimeZone: TimeZone = TimeZone.getDefault()
): Long {
  val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    timeInMillis = utcDateMillis
  }
  return Calendar.getInstance(localTimeZone).apply {
    timeInMillis = timeOfDayFromMillis
    set(
      utcCal.get(Calendar.YEAR),
      utcCal.get(Calendar.MONTH),
      utcCal.get(Calendar.DAY_OF_MONTH)
    )
  }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
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
  selectedDateRange: Pair<Long, Long>? = null,
  onDateRangeFilterChanged: (Pair<Long, Long>?) -> Unit = {},
  onDeleteTransaction: (TransactionEntity) -> Unit,
  onEditTransaction: (TransactionEntity) -> Unit,
  onOpenAddTransaction: () -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
  var showDateRangeDialog by remember { mutableStateOf(false) }

  // Filtered transactions
  val filteredTransactions = remember(
    transactions,
    categories,
    searchQuery,
    selectedTypeFilter,
    selectedCategoryId,
    selectedDateRange
  ) {
    filterTransactions(
      transactions = transactions,
      categories = categories,
      searchQuery = searchQuery,
      selectedTypeFilter = selectedTypeFilter,
      selectedCategoryId = selectedCategoryId,
      selectedDateRange = selectedDateRange
    )
  }

  val dateRangeLabel = remember(selectedDateRange, strings.dateRange) {
    if (selectedDateRange == null) {
      strings.dateRange
    } else {
      val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
      val start = fmt.format(Date(selectedDateRange.first))
      val end = fmt.format(Date(selectedDateRange.second))
      if (start == end) start else "$start - $end"
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

      // Search Field (filters by title, category, or date)
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text(strings.searchTransactions) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onSearchQueryChanged("") },
                modifier = Modifier.testTag("clear_search_button")
              ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
            IconButton(
              onClick = { showDateRangeDialog = true },
              modifier = Modifier.testTag("transaction_date_range_button")
            ) {
              Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = strings.dateRange,
                tint = if (selectedDateRange != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
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
          selected = selectedTypeFilter == null && selectedCategoryId == null && selectedDateRange == null,
          onClick = {
            onTypeFilterChanged(null)
            onCategoryFilterChanged(null)
            onDateRangeFilterChanged(null)
          },
          label = { Text("${strings.filterAll} (${transactions.size})") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("filter_all_chip")
        )

        FilterChip(
          selected = selectedDateRange != null,
          onClick = { showDateRangeDialog = true },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          },
          trailingIcon = if (selectedDateRange != null) {
            {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear date range",
                modifier = Modifier
                  .size(16.dp)
                  .clickable { onDateRangeFilterChanged(null) }
                  .testTag("clear_date_range_icon")
              )
            }
          } else null,
          label = { Text(dateRangeLabel) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("filter_date_range_chip")
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
              onDelete = { transactionToDelete = item },
              onClick = { onEditTransaction(item) }
            )
          }

          item {
            Spacer(modifier = Modifier.height(72.dp))
          }
        }
      }
    }
  }

  // Date Range Picker Dialog
  if (showDateRangeDialog) {
    val dateRangePickerState = rememberDateRangePickerState(
      initialSelectedStartDateMillis = selectedDateRange?.first?.let { localMillisToUtcDateMillis(it) },
      initialSelectedEndDateMillis = selectedDateRange?.second?.let { localMillisToUtcDateMillis(it) }
    )

    DatePickerDialog(
      onDismissRequest = { showDateRangeDialog = false },
      confirmButton = {
        TextButton(
          onClick = {
            val startUtc = dateRangePickerState.selectedStartDateMillis
            val endUtc = dateRangePickerState.selectedEndDateMillis ?: startUtc
            if (startUtc != null && endUtc != null) {
              onDateRangeFilterChanged(utcDateRangeToLocalBounds(startUtc, endUtc))
            }
            showDateRangeDialog = false
          },
          enabled = dateRangePickerState.selectedStartDateMillis != null,
          modifier = Modifier.testTag("confirm_date_range_button")
        ) {
          Text("Apply")
        }
      },
      dismissButton = {
        Row {
          if (selectedDateRange != null) {
            TextButton(
              onClick = {
                onDateRangeFilterChanged(null)
                showDateRangeDialog = false
              },
              modifier = Modifier.testTag("clear_date_range_dialog_button")
            ) {
              Text("Clear")
            }
          }
          TextButton(onClick = { showDateRangeDialog = false }) {
            Text(strings.cancel)
          }
        }
      }
    ) {
      DateRangePicker(
        state = dateRangePickerState,
        title = {
          Text(
            text = strings.dateRange,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp)
          )
        },
        showModeToggle = false,
        modifier = Modifier
          .fillMaxWidth()
          .height(440.dp)
      )
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
