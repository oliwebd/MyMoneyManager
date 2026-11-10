package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringInterval
import com.example.data.model.TransactionType
import com.example.ui.components.AutoSuggestTextField
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.TouchAdjustmentScroller

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
  sheetState: SheetState,
  categories: List<CategoryEntity>,
  accounts: List<AccountEntity>,
  recentTitles: List<String>,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onSave: (
    title: String,
    amount: Double,
    type: TransactionType,
    categoryId: Long,
    accountId: Long,
    note: String,
    isRecurring: Boolean,
    interval: RecurringInterval
  ) -> Unit
) {
  var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("0.00") }
  var amountValue by remember { mutableDoubleStateOf(0.0) }
  var selectedCategoryId by remember(categories, selectedType) {
    val filtered = categories.filter { if (selectedType == TransactionType.EXPENSE) it.isExpense else !it.isExpense }
    mutableLongStateOf(filtered.firstOrNull()?.id ?: 1L)
  }
  var selectedAccountId by remember(accounts) {
    mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L)
  }
  var note by remember { mutableStateOf("") }
  var isRecurring by remember { mutableStateOf(false) }
  var recurringInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }

  val isDark = isSystemInDarkTheme()
  val accentColor = if (selectedType == TransactionType.INCOME) {
    if (isDark) Color(0xFF2EBD6E) else Color(0xFF0F9D58)
  } else {
    if (isDark) Color(0xFFE25241) else Color(0xFFEA4335)
  }

  fun updateAmount(newVal: Double) {
    val rounded = String.format("%.2f", newVal)
    amountValue = newVal
    amountText = rounded
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header with Close
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "New Transaction",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_button")) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Expense / Income Segmented Switch
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val expenseSelected = selectedType == TransactionType.EXPENSE
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (expenseSelected) accentColor else Color.Transparent,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { selectedType = TransactionType.EXPENSE }
            .testTag("type_expense_tab")
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = if (expenseSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Expense",
              fontWeight = FontWeight.Bold,
              color = if (expenseSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        val incomeSelected = selectedType == TransactionType.INCOME
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (incomeSelected) accentColor else Color.Transparent,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { selectedType = TransactionType.INCOME }
            .testTag("type_income_tab")
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = if (incomeSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Income",
              fontWeight = FontWeight.Bold,
              color = if (incomeSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Amount Box with Direct Free Typing
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Amount ($currencySymbol)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = currencySymbol,
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.Bold,
              color = accentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
              value = amountText,
              onValueChange = { input ->
                // Clean input to valid decimal
                val filtered = input.filter { it.isDigit() || it == '.' }
                amountText = filtered
                amountValue = filtered.toDoubleOrNull() ?: 0.0
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = accentColor
              ),
              singleLine = true,
              modifier = Modifier
                .width(180.dp)
                .testTag("amount_input_field")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Tactile Touch Scroller Cylinder & Stepper
          TouchAdjustmentScroller(
            currentAmount = amountValue,
            onAmountChanged = { updateAmount(it) },
            accentColor = accentColor
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Title Auto-Suggest Input
      AutoSuggestTextField(
        value = title,
        onValueChange = { title = it },
        onSuggestionSelected = { suggestion ->
          // Match category if available
          val matchedCategory = categories.find { it.name.equals(suggestion.categoryName, ignoreCase = true) }
          if (matchedCategory != null) {
            selectedCategoryId = matchedCategory.id
            selectedType = if (matchedCategory.isExpense) TransactionType.EXPENSE else TransactionType.INCOME
          }
          if (suggestion.defaultAmount != null && amountValue == 0.0) {
            updateAmount(suggestion.defaultAmount)
          }
        },
        recentSavedTitles = recentTitles
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Category Selector
      Text(
        text = "Category",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      val currentCategories = categories.filter {
        if (selectedType == TransactionType.EXPENSE) it.isExpense else !it.isExpense
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        currentCategories.forEach { category ->
          val isSelected = selectedCategoryId == category.id
          val catColor = try {
            Color(android.graphics.Color.parseColor(category.colorHex))
          } catch (_: Exception) {
            MaterialTheme.colorScheme.primary
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, catColor) else null,
            modifier = Modifier
              .clickable { selectedCategoryId = category.id }
              .testTag("cat_chip_${category.name.replace(" ", "_")}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .background(catColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = CategoryIconHelper.getIcon(category.iconName),
                  contentDescription = null,
                  tint = catColor,
                  modifier = Modifier.size(14.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Account Selector
      Text(
        text = "Account / Payment Method",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        accounts.forEach { account ->
          val isSelected = selectedAccountId == account.id
          FilterChip(
            selected = isSelected,
            onClick = { selectedAccountId = account.id },
            label = { Text(account.name) },
            leadingIcon = {
              Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.testTag("account_chip_${account.name.replace(" ", "_")}")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Recurring Options
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Recurring Transaction",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Schedule automated alerts & reminders",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Switch(
          checked = isRecurring,
          onCheckedChange = { isRecurring = it },
          modifier = Modifier.testTag("recurring_switch")
        )
      }

      AnimatedVisibility(visible = isRecurring) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(RecurringInterval.WEEKLY, RecurringInterval.MONTHLY, RecurringInterval.YEARLY).forEach { interval ->
            val isSelected = recurringInterval == interval
            FilterChip(
              selected = isSelected,
              onClick = { recurringInterval = interval },
              label = { Text(interval.name.lowercase().replaceFirstChar { it.uppercase() }) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Optional Note
      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Note (Optional)") },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("transaction_note_input")
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      Button(
        onClick = {
          if (title.isBlank()) {
            title = if (selectedType == TransactionType.EXPENSE) "General Expense" else "General Income"
          }
          if (amountValue <= 0.0) {
            amountValue = 1.0
          }
          onSave(
            title,
            amountValue,
            selectedType,
            selectedCategoryId,
            selectedAccountId,
            note,
            isRecurring,
            recurringInterval
          )
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_transaction_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Save ${if (selectedType == TransactionType.INCOME) "Income" else "Expense"}",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
