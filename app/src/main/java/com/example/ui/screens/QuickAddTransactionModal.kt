package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconHelper
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.expenseColors
import com.example.ui.theme.incomeColors

internal fun sanitizeQuickAddAmountInput(input: String): String {
  val sb = StringBuilder()
  var hasDot = false
  var decimals = 0
  for (ch in input) {
    if (ch.isDigit()) {
      if (hasDot) {
        if (decimals < 2) {
          sb.append(ch)
          decimals++
        }
      } else {
        sb.append(ch)
      }
    } else if (ch == '.' && !hasDot) {
      if (sb.isEmpty()) sb.append('0')
      sb.append('.')
      hasDot = true
    }
  }
  return sb.toString()
}

internal fun parseQuickAddAmount(input: String): Double? {
  val parsed = input.trim().toDoubleOrNull() ?: return null
  return if (parsed > 0.0 && parsed.isFinite()) parsed else null
}

internal fun resolveQuickAddTitle(
  titleInput: String,
  selectedCategory: CategoryEntity?,
  type: TransactionType
): String {
  val trimmed = titleInput.trim()
  if (trimmed.isNotEmpty()) return trimmed
  if (selectedCategory != null && selectedCategory.name.isNotBlank()) return selectedCategory.name
  return if (type == TransactionType.EXPENSE) "General Expense" else "General Income"
}

@Composable
fun QuickAddTransactionModal(
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
    accountId: Long
  ) -> Unit
) {
  val strings = LocalAppStrings.current
  var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
  var amountText by remember { mutableStateOf("") }
  var title by remember { mutableStateOf("") }
  var showAmountError by remember { mutableStateOf(false) }

  val filteredCategories = remember(categories, selectedType) {
    categories.filter {
      if (selectedType == TransactionType.EXPENSE) it.isExpense else !it.isExpense
    }
  }

  var selectedCategoryId by remember(filteredCategories) {
    mutableLongStateOf(filteredCategories.firstOrNull()?.id ?: 1L)
  }

  var selectedAccountId by remember(accounts) {
    val defaultAcc = accounts.find { it.name.contains("Bank", ignoreCase = true) } ?: accounts.firstOrNull()
    mutableLongStateOf(defaultAcc?.id ?: 1L)
  }

  val accentColor = if (selectedType == TransactionType.EXPENSE) expenseColors().main else incomeColors().main
  val solidTypeColor = if (selectedType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
  val amountFocusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    amountFocusRequester.requestFocus()
  }

  fun submit() {
    val parsedAmount = parseQuickAddAmount(amountText)
    if (parsedAmount == null) {
      showAmountError = true
      return
    }
    val chosenCategory = filteredCategories.find { it.id == selectedCategoryId } ?: filteredCategories.firstOrNull()
    val resolvedTitle = resolveQuickAddTitle(title, chosenCategory, selectedType)
    onSave(
      resolvedTitle,
      parsedAmount,
      selectedType,
      chosenCategory?.id ?: selectedCategoryId,
      selectedAccountId
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      shadowElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .widthIn(max = 420.dp)
        .testTag("quick_add_modal")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.14f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.quickAdd,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("quick_add_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = strings.cancel,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Compact Type Toggle (Expense / Income)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val isExpense = selectedType == TransactionType.EXPENSE
          Box(
            modifier = Modifier
              .weight(1f)
              .height(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isExpense) ExpenseRed else Color.Transparent)
              .clickable { selectedType = TransactionType.EXPENSE }
              .testTag("quick_add_type_expense"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = strings.expense,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          val isIncome = selectedType == TransactionType.INCOME
          Box(
            modifier = Modifier
              .weight(1f)
              .height(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isIncome) IncomeGreen else Color.Transparent)
              .clickable { selectedType = TransactionType.INCOME }
              .testTag("quick_add_type_income"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = strings.income,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Amount Input
        OutlinedTextField(
          value = amountText,
          onValueChange = { raw ->
            amountText = sanitizeQuickAddAmountInput(raw)
            if (showAmountError && parseQuickAddAmount(amountText) != null) {
              showAmountError = false
            }
          },
          label = { Text(strings.amountLabel) },
          placeholder = { Text("0.00") },
          prefix = {
            Text(
              text = "$currencySymbol ",
              fontWeight = FontWeight.Bold,
              color = accentColor
            )
          },
          isError = showAmountError,
          supportingText = if (showAmountError) {
            { Text("Enter an amount greater than 0") }
          } else null,
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(amountFocusRequester)
            .testTag("quick_add_amount_input")
        )

        // Title Input
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title (optional)") },
          placeholder = {
            val catHint = filteredCategories.find { it.id == selectedCategoryId }?.name
            Text(catHint ?: "Coffee, Groceries, Salary...")
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { submit() }),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_add_title_input")
        )

        // Quick Recent Title Chips (when title is empty)
        if (title.isEmpty() && recentTitles.isNotEmpty()) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(recentTitles.take(5)) { suggestion ->
              SuggestionChip(
                onClick = { title = suggestion },
                label = {
                  Text(
                    text = suggestion,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall
                  )
                },
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }

        // Compact Category Selector Row
        if (filteredCategories.isNotEmpty()) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("quick_add_category_list")
          ) {
            items(filteredCategories, key = { it.id }) { category ->
              val isSelected = selectedCategoryId == category.id
              FilterChip(
                selected = isSelected,
                onClick = { selectedCategoryId = category.id },
                leadingIcon = {
                  Icon(
                    imageVector = CategoryIconHelper.getIcon(category.iconName),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                },
                label = {
                  Text(
                    text = category.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = accentColor.copy(alpha = 0.16f),
                  selectedLabelColor = accentColor,
                  selectedLeadingIconColor = accentColor
                ),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }

        // Compact Account Selector Row
        if (accounts.isNotEmpty()) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
          ) {
            accounts.forEach { account ->
              val isSelected = selectedAccountId == account.id
              val isBank = account.name.contains("Bank", ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = { selectedAccountId = account.id },
                leadingIcon = {
                  Icon(
                    imageVector = if (isBank) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                },
                label = {
                  Text(
                    text = account.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = accentColor.copy(alpha = 0.16f),
                  selectedLabelColor = accentColor,
                  selectedLeadingIconColor = accentColor
                ),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }

        // Actions
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("quick_add_cancel_button")
          ) {
            Text(strings.cancel)
          }

          Button(
            onClick = { submit() },
            colors = ButtonDefaults.buttonColors(
              containerColor = solidTypeColor,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1.4f)
              .height(48.dp)
              .testTag("quick_add_save_button")
          ) {
            Text(
              text = strings.saveEntry,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
