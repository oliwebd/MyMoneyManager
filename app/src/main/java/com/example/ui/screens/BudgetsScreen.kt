package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.ui.components.CategoryIconHelper
import com.example.ui.viewmodel.BudgetProgressItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
  monthYear: String,
  budgetProgressList: List<BudgetProgressItem>,
  categories: List<CategoryEntity>,
  currencySymbol: String,
  onSaveBudget: (categoryId: Long, limit: Double) -> Unit,
  onDeleteBudget: (MonthlyBudgetEntity) -> Unit,
  onTriggerTestNotification: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var budgetToEdit by remember { mutableStateOf<MonthlyBudgetEntity?>(null) }
  var budgetToDelete by remember { mutableStateOf<MonthlyBudgetEntity?>(null) }

  val totalBudget = budgetProgressList.sumOf { it.limit }
  val totalSpent = budgetProgressList.sumOf { it.spent }
  val overallPct = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.testTag("fab_add_budget")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Budget Goal")
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
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Monthly Budgets",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Track goals & get real-time threshold alerts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onTriggerTestNotification,
          modifier = Modifier.testTag("test_budget_notification_button")
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Test Notification",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Summary Gauge Card
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total Monthly Spending Limit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currencySymbol${String.format("%,.2f", totalSpent)} of $currencySymbol${String.format("%,.2f", totalBudget)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = when {
                overallPct >= 100 -> Color(0xFFFCE8E6)
                overallPct >= 75 -> Color(0xFFFEF7E0)
                else -> Color(0xFFE6F4EA)
              }
            ) {
              Text(
                text = "$overallPct%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                  overallPct >= 100 -> Color(0xFFC5221F)
                  overallPct >= 75 -> Color(0xFFB06000)
                  else -> Color(0xFF137333)
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          val animProgress by animateFloatAsState(
            targetValue = (totalSpent / (if (totalBudget > 0) totalBudget else 1.0)).toFloat().coerceIn(0f, 1f),
            label = "overallProgress"
          )

          LinearProgressIndicator(
            progress = { animProgress },
            color = when {
              overallPct >= 100 -> Color(0xFFEA4335)
              overallPct >= 75 -> Color(0xFFF29900)
              else -> Color(0xFF0F9D58)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Budgets List
      if (budgetProgressList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No budgets created yet for this month.",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Set a budget to get real-time spending milestone alerts.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { showAddDialog = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("empty_add_budget_button")
            ) {
              Text("Add Your First Budget")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(budgetProgressList, key = { it.budget.id }) { item ->
            val catColor = try {
              Color(android.graphics.Color.parseColor(item.category?.colorHex ?: "#1A73E8"))
            } catch (_: Exception) {
              MaterialTheme.colorScheme.primary
            }

            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .background(catColor.copy(alpha = 0.15f), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = CategoryIconHelper.getIcon(item.category?.iconName ?: "savings"),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = item.category?.name ?: "Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = "$currencySymbol${String.format("%.2f", item.spent)} / $currencySymbol${String.format("%.2f", item.limit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = when {
                        item.isOverBudget -> Color(0xFFFCE8E6)
                        item.isWarning -> Color(0xFFFEF7E0)
                        else -> Color(0xFFE6F4EA)
                      }
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(
                          imageVector = when {
                            item.isOverBudget -> Icons.Default.Warning
                            item.isWarning -> Icons.Default.Warning
                            else -> Icons.Default.CheckCircle
                          },
                          contentDescription = null,
                          tint = when {
                            item.isOverBudget -> Color(0xFFC5221F)
                            item.isWarning -> Color(0xFFB06000)
                            else -> Color(0xFF137333)
                          },
                          modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = "${item.percentage}%",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = when {
                            item.isOverBudget -> Color(0xFFC5221F)
                            item.isWarning -> Color(0xFFB06000)
                            else -> Color(0xFF137333)
                          }
                        )
                      }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                      onClick = { budgetToDelete = item.budget },
                      modifier = Modifier.size(32.dp).testTag("delete_budget_${item.budget.id}")
                    ) {
                      Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Budget",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val itemProgress by animateFloatAsState(
                  targetValue = (item.spent / (if (item.limit > 0) item.limit else 1.0)).toFloat().coerceIn(0f, 1f),
                  label = "itemProgress"
                )

                LinearProgressIndicator(
                  progress = { itemProgress },
                  color = when {
                    item.isOverBudget -> Color(0xFFEA4335)
                    item.isWarning -> Color(0xFFF29900)
                    else -> Color(0xFF0F9D58)
                  },
                  trackColor = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                )
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

  // Add / Edit Budget Dialog
  if (showAddDialog) {
    val expenseCategories = categories.filter { it.isExpense }
    var selectedCatId by remember { mutableLongStateOf(expenseCategories.firstOrNull()?.id ?: 1L) }
    var limitInput by remember { mutableStateOf("300") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Set Monthly Budget Goal") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = "Choose category and spending threshold:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Category Dropdown
          var expanded by remember { mutableStateOf(false) }
          val selectedCat = expenseCategories.find { it.id == selectedCatId }

          ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
          ) {
            OutlinedTextField(
              value = selectedCat?.name ?: "Select Category",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
              modifier = Modifier
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
              shape = RoundedCornerShape(14.dp)
            )
            ExposedDropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false }
            ) {
              expenseCategories.forEach { cat ->
                DropdownMenuItem(
                  text = { Text(cat.name) },
                  onClick = {
                    selectedCatId = cat.id
                    expanded = false
                  }
                )
              }
            }
          }

          // Limit Amount Input
          OutlinedTextField(
            value = limitInput,
            onValueChange = { limitInput = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Monthly Limit ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          )

          // Quick Presets
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            listOf(100.0, 250.0, 500.0, 1000.0).forEach { preset ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.clickable { limitInput = preset.toInt().toString() }
              ) {
                Text(
                  text = "$currencySymbol${preset.toInt()}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = limitInput.toDoubleOrNull() ?: 100.0
            onSaveBudget(selectedCatId, amount)
            showAddDialog = false
          },
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Save Budget")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Budget Dialog
  if (budgetToDelete != null) {
    val b = budgetToDelete!!
    AlertDialog(
      onDismissRequest = { budgetToDelete = null },
      title = { Text("Remove Budget Goal") },
      text = { Text("Are you sure you want to delete this monthly budget limit?") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteBudget(b)
            budgetToDelete = null
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Remove", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { budgetToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
