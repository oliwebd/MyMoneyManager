package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringAlertEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.CategorySlice
import com.example.ui.components.MonthlyComparisonBarChart
import com.example.ui.components.MonthlyTrendItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  selectedMonthYear: String,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  transactions: List<TransactionEntity>,
  categories: List<CategoryEntity>,
  accounts: List<AccountEntity>,
  activeAlerts: List<RecurringAlertEntity>,
  categorySlices: List<CategorySlice>,
  totalMonthExpense: Double,
  historicalTrends: List<MonthlyTrendItem>,
  currencySymbol: String,
  baseCurrencyCode: String,
  onOpenAddTransaction: () -> Unit,
  onNavigateToTransactions: () -> Unit,
  onNavigateToBudgets: () -> Unit,
  onNavigateToBackup: () -> Unit,
  onLockApp: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Format Month-Year label (e.g., "Nov 2026")
  val displayMonthLabel = try {
    val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonthYear)
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date ?: Date())
  } catch (_: Exception) {
    selectedMonthYear
  }

  // Calculate totals for selected month
  val monthTransactions = transactions.filter {
    SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.timestamp)) == selectedMonthYear
  }
  val totalIncome = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
  val totalExpense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
  val netSavings = totalIncome - totalExpense

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar with Lock and Security status
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "M3Note",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "AES-256",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
          Text(
            text = "MyMoneyManager Vault",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onLockApp,
          modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .testTag("lock_app_button")
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock App",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    // Month Selector
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onPrevMonth, modifier = Modifier.testTag("prev_month_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
          }

          Text(
            text = displayMonthLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )

          IconButton(onClick = onNextMonth, modifier = Modifier.testTag("next_month_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
          }
        }
      }
    }

    // Hero Balance Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                colors = listOf(
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                  MaterialTheme.colorScheme.surface
                )
              )
            )
            .padding(20.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Net Cash Flow",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = baseCurrencyCode,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "${if (netSavings >= 0) "+" else ""}$currencySymbol${String.format("%,.2f", netSavings)}",
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.Bold,
              color = if (netSavings >= 0) Color(0xFF0F9D58) else Color(0xFFEA4335)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isDark = isSystemInDarkTheme()
            val incomePillBg = if (isDark) Color(0xFF132B1C) else Color(0xFFE6F4EA)
            val incomePillText = if (isDark) Color(0xFF81C995) else Color(0xFF137333)
            val incomeIconBg = if (isDark) Color(0xFF0F9D58) else Color(0xFF0F9D58)

            val expensePillBg = if (isDark) Color(0xFF3B1E1D) else Color(0xFFFCE8E6)
            val expensePillText = if (isDark) Color(0xFFF28B82) else Color(0xFFC5221F)
            val expenseIconBg = if (isDark) Color(0xFFEA4335) else Color(0xFFEA4335)

            // Income / Expense Dual Summary Pills
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Income Pill
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = incomePillBg,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .background(incomeIconBg, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowUpward,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Income",
                      style = MaterialTheme.typography.labelSmall,
                      color = incomePillText
                    )
                    Text(
                      text = "$currencySymbol${String.format("%,.1f", totalIncome)}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = incomePillText
                    )
                  }
                }
              }

              // Expense Pill
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = expensePillBg,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .background(expenseIconBg, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowDownward,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Expense",
                      style = MaterialTheme.typography.labelSmall,
                      color = expensePillText
                    )
                    Text(
                      text = "$currencySymbol${String.format("%,.1f", totalExpense)}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = expensePillText
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Quick Action Bar
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          QuickActionItem(
            icon = Icons.Default.Add,
            label = "Add Entry",
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onOpenAddTransaction,
            testTag = "quick_add_transaction_button"
          )
          QuickActionItem(
            icon = Icons.Default.Savings,
            label = "Budgets",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onNavigateToBudgets,
            testTag = "quick_budgets_button"
          )
          QuickActionItem(
            icon = Icons.Default.Security,
            label = "Vault",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onNavigateToBackup,
            testTag = "quick_backup_button"
          )
        }
      }
    }

    // Active Recurring Bill Reminder Banner
    if (activeAlerts.isNotEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            val firstAlert = activeAlerts.first()
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Upcoming Recurring Bill",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                text = "${firstAlert.transactionTitle}: $currencySymbol${String.format("%.2f", firstAlert.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        }
      }
    }

    // Category Doughnut Chart
    item {
      CategoryDonutChart(
        slices = categorySlices,
        totalAmount = totalMonthExpense,
        currencySymbol = currencySymbol
      )
    }

    // Historical 6-Month Cash Flow Bar Chart
    item {
      MonthlyComparisonBarChart(
        trends = historicalTrends,
        currencySymbol = currencySymbol
      )
    }

    // Recent Transactions Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Transactions",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onNavigateToTransactions, modifier = Modifier.testTag("view_all_transactions_button")) {
          Text("View All (${transactions.size})")
        }
      }
    }

    // Recent Transactions List (top 5)
    val recentFive = transactions.take(5)
    if (recentFive.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No transactions logged yet. Tap 'Add Entry' to start!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(recentFive) { item ->
        TransactionRowItem(
          transaction = item,
          categories = categories,
          currencySymbol = currencySymbol
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
fun TransactionRowItem(
  transaction: TransactionEntity,
  categories: List<CategoryEntity>,
  currencySymbol: String,
  onDelete: (() -> Unit)? = null
) {
  val cat = categories.find { it.id == transaction.categoryId }
  val catColor = try {
    Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#1A73E8"))
  } catch (_: Exception) {
    MaterialTheme.colorScheme.primary
  }

  val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.timestamp))
  val isIncome = transaction.type == TransactionType.INCOME

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
            .size(42.dp)
            .background(catColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = CategoryIconHelper.getIcon(cat?.iconName ?: "category"),
            contentDescription = null,
            tint = catColor,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = transaction.title,
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
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${if (isIncome) "+" else "-"}$currencySymbol${String.format("%.2f", transaction.amount)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (isIncome) Color(0xFF0F9D58) else Color(0xFFEA4335)
        )
        if (transaction.isRecurring) {
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
    }
  }
}

@Composable
private fun QuickActionItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  containerColor: Color,
  contentColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(color = containerColor, shape = CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1
    )
  }
}

