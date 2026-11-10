package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
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

@OptIn(ExperimentalMaterial3Api::class)
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
  var isTypingDirectly by remember { mutableStateOf(false) }

  var selectedCategoryId by remember(categories, selectedType) {
    val filtered = categories.filter { if (selectedType == TransactionType.EXPENSE) it.isExpense else !it.isExpense }
    mutableLongStateOf(filtered.firstOrNull()?.id ?: 1L)
  }
  var selectedAccountId by remember(accounts) {
    // Default to Main Bank Checking or first account to match screenshot
    val defaultAcc = accounts.find { it.name.contains("Bank", ignoreCase = true) } ?: accounts.firstOrNull()
    mutableLongStateOf(defaultAcc?.id ?: 1L)
  }
  var note by remember { mutableStateOf("") }
  var isRecurring by remember { mutableStateOf(false) }
  var recurringInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }

  // Neon theme colors matching user screenshot
  val neonRed = Color(0xFFFF334B)
  val neonGreen = Color(0xFF00E676)
  val accentColor = if (selectedType == TransactionType.EXPENSE) neonRed else neonGreen

  fun updateAmount(newVal: Double) {
    val clamped = maxOf(0.0, newVal)
    amountValue = clamped
    amountText = String.format("%.2f", clamped)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF0F1015),
    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    dragHandle = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // 1. TOP HEADER - Image 2: Amount ($) & Close (X)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Amount ($currencySymbol)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 22.sp
          )
        }

        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF26272E))
            .clickable(onClick = onDismiss)
            .testTag("close_sheet_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Type Selector Tab (Expense / Income)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF1E1F26))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val isExpense = selectedType == TransactionType.EXPENSE
        Box(
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isExpense) neonRed else Color.Transparent)
            .clickable { selectedType = TransactionType.EXPENSE }
            .testTag("type_expense_tab"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Expense",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          }
        }

        val isIncome = selectedType == TransactionType.INCOME
        Box(
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isIncome) neonGreen else Color.Transparent)
            .clickable { selectedType = TransactionType.INCOME }
            .testTag("type_income_tab"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Income",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. GIANT AMOUNT DISPLAY CARD - Image 2
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF181920))
          .border(
            width = 1.8.dp,
            color = accentColor,
            shape = RoundedCornerShape(26.dp)
          )
          .padding(vertical = 24.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Large Red Currency Symbol
          Text(
            text = currencySymbol,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = accentColor
          )

          Spacer(modifier = Modifier.width(14.dp))

          // Vertical divider line
          Box(
            modifier = Modifier
              .width(1.5.dp)
              .height(48.dp)
              .background(Color(0x33FFFFFF))
          )

          Spacer(modifier = Modifier.width(14.dp))

          // Editable Amount Field
          BasicTextField(
            value = amountText,
            onValueChange = { input ->
              val clean = input.filter { it.isDigit() || it == '.' }
              amountText = clean
              amountValue = clean.toDoubleOrNull() ?: 0.0
            },
            textStyle = TextStyle(
              fontSize = 48.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Start
            ),
            singleLine = true,
            cursorBrush = SolidColor(accentColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f, fill = false)
              .testTag("amount_input_field")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. STEPPERS & TOUCH CYLINDER PANEL - Image 2
      TouchAdjustmentScroller(
        currentAmount = amountValue,
        onAmountChanged = { updateAmount(it) },
        accentColor = accentColor
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Title Auto-Suggest Input
      AutoSuggestTextField(
        value = title,
        onValueChange = { title = it },
        onSuggestionSelected = { suggestion ->
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

      Spacer(modifier = Modifier.height(18.dp))

      // 4. CATEGORY SECTION - Image 1 (2-Column Grid with Vibrant Icons and Neon Selection)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.GridView,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Category",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 19.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      val currentCategories = categories.filter {
        if (selectedType == TransactionType.EXPENSE) it.isExpense else !it.isExpense
      }

      // 2-Column Grid of Category Pills
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        currentCategories.chunked(2).forEach { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            pair.forEach { category ->
              val isSelected = selectedCategoryId == category.id
              val rawColor = try {
                Color(android.graphics.Color.parseColor(category.colorHex))
              } catch (_: Exception) {
                accentColor
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .clip(RoundedCornerShape(26.dp))
                  .background(
                    if (isSelected) accentColor.copy(alpha = 0.20f) else Color(0xFF202127)
                  )
                  .then(
                    if (isSelected) {
                      Modifier.border(
                        width = 1.8.dp,
                        color = accentColor,
                        shape = RoundedCornerShape(26.dp)
                      )
                    } else Modifier
                  )
                  .clickable { selectedCategoryId = category.id }
                  .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Icon(
                    imageVector = CategoryIconHelper.getIcon(category.iconName),
                    contentDescription = null,
                    tint = if (isSelected) accentColor else rawColor,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    maxLines = 1
                  )
                }
              }
            }
            if (pair.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 5. ACCOUNT / PAYMENT METHOD - Image 1
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16171D)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CreditCard,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Account / Payment Method",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 16.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            accounts.forEach { account ->
              val isSelected = selectedAccountId == account.id
              val isBank = account.name.contains("Bank", ignoreCase = true)
              val accIcon = if (isBank) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet

              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
              ) {
                // Pill Container
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                      if (isSelected) accentColor.copy(alpha = 0.20f) else Color(0xFF24252D)
                    )
                    .then(
                      if (isSelected) {
                        Modifier.border(
                          width = 1.8.dp,
                          color = accentColor,
                          shape = RoundedCornerShape(24.dp)
                        )
                      } else Modifier
                    )
                    .clickable { selectedAccountId = account.id }
                    .padding(horizontal = 12.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                  ) {
                    Icon(
                      imageVector = accIcon,
                      contentDescription = null,
                      tint = if (isSelected) accentColor else Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = account.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = Color.White,
                      fontSize = 12.5.sp,
                      maxLines = 1
                    )
                  }
                }

                // Checkmark badge anchored top-right if selected (matches Image 1)
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.TopEnd)
                      .offset(x = 2.dp, y = (-2).dp)
                      .size(18.dp)
                      .clip(CircleShape)
                      .background(accentColor),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(12.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 6. RECURRING TRANSACTION - Image 1
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16171D)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Recurring Transaction",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 15.sp
                )
                Text(
                  text = "Schedule automated alerts & reminders",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF8E8E98),
                  fontSize = 12.sp
                )
              }
            }

            Switch(
              checked = isRecurring,
              onCheckedChange = { isRecurring = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color(0xFF8E8E98),
                uncheckedTrackColor = Color(0xFF2C2D35)
              ),
              modifier = Modifier.testTag("recurring_switch")
            )
          }

          AnimatedVisibility(visible = isRecurring) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf(RecurringInterval.WEEKLY, RecurringInterval.MONTHLY, RecurringInterval.YEARLY).forEach { interval ->
                val isSelected = recurringInterval == interval
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) accentColor else Color(0xFF24252D))
                    .clickable { recurringInterval = interval },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = interval.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 7. NOTE (OPTIONAL) - Image 1
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16171D)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = Color(0xFF8E8E98),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Box(modifier = Modifier.weight(1f)) {
            if (note.isEmpty()) {
              Text(
                text = "Note (Optional)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E98)
              )
            }
            BasicTextField(
              value = note,
              onValueChange = { note = it },
              textStyle = TextStyle(
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal
              ),
              cursorBrush = SolidColor(accentColor),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("transaction_note_input")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // 8. SAVE EXPENSE / SAVE INCOME BUTTON - Image 1 (Full width glowing red capsule)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(
            if (selectedType == TransactionType.EXPENSE) {
              Brush.horizontalGradient(listOf(Color(0xFFFF2A4A), Color(0xFFFF483D)))
            } else {
              Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF00E676)))
            }
          )
          .clickable {
            val finalTitle = if (title.isBlank()) {
              val cat = categories.find { it.id == selectedCategoryId }
              cat?.name ?: if (selectedType == TransactionType.EXPENSE) "General Expense" else "General Income"
            } else title

            val finalAmount = if (amountValue <= 0.0) 1.0 else amountValue

            onSave(
              finalTitle,
              finalAmount,
              selectedType,
              selectedCategoryId,
              selectedAccountId,
              note,
              isRecurring,
              recurringInterval
            )
          }
          .testTag("save_transaction_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Save ${if (selectedType == TransactionType.INCOME) "Income" else "Expense"}",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 17.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
