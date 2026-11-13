package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.CategoryEntity
import com.example.ui.i18n.LocalAppStrings

private val editorColors = listOf(
  "#4CAF50", "#FF9800", "#2196F3", "#9C27B0", "#00BCD4", "#E91E63",
  "#F44336", "#3F51B5", "#0F9D58", "#00897B", "#1A73E8", "#673AB7"
)

private fun AccountType.label() = name.lowercase().replaceFirstChar { it.uppercase() }

private fun parseColorOrDefault(hex: String): Color {
  return try {
    Color(android.graphics.Color.parseColor(hex))
  } catch (_: Exception) {
    Color(0xFF1A73E8)
  }
}

@Composable
fun CategoriesDialog(
  categories: List<CategoryEntity>,
  onSave: (CategoryEntity) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  var adding by remember { mutableStateOf(false) }
  var editing by remember { mutableStateOf<CategoryEntity?>(null) }

  EditableListDialog(
    title = strings.categoriesTitle,
    onAdd = { adding = true },
    onDismiss = onDismiss
  ) {
    items(categories, key = { it.id }) { category ->
      EditableListRow(
        icon = CategoryIconHelper.getIcon(category.iconName),
        color = parseColorOrDefault(category.colorHex),
        title = category.name,
        subtitle = if (category.isExpense) strings.expense else strings.income,
        onClick = { editing = category },
        testTag = "category_row_${category.id}"
      )
    }
  }

  if (adding || editing != null) {
    CategoryEditorDialog(
      initial = editing,
      onSave = {
        onSave(it)
        adding = false
        editing = null
      },
      onDismiss = {
        adding = false
        editing = null
      }
    )
  }
}

@Composable
fun AccountsDialog(
  accounts: List<AccountEntity>,
  onSave: (AccountEntity) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  var adding by remember { mutableStateOf(false) }
  var editing by remember { mutableStateOf<AccountEntity?>(null) }

  EditableListDialog(
    title = strings.accountsTitle,
    onAdd = { adding = true },
    onDismiss = onDismiss
  ) {
    items(accounts, key = { it.id }) { account ->
      EditableListRow(
        icon = if (account.type == AccountType.BANK) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
        color = parseColorOrDefault(account.colorHex),
        title = account.name,
        subtitle = account.type.label(),
        onClick = { editing = account },
        testTag = "account_row_${account.id}"
      )
    }
  }

  if (adding || editing != null) {
    AccountEditorDialog(
      initial = editing,
      onSave = {
        onSave(it)
        adding = false
        editing = null
      },
      onDismiss = {
        adding = false
        editing = null
      }
    )
  }
}

@Composable
private fun EditableListDialog(
  title: String,
  onAdd: () -> Unit,
  onDismiss: () -> Unit,
  content: LazyListScope.() -> Unit
) {
  val strings = LocalAppStrings.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = { LazyColumn(modifier = Modifier.heightIn(max = 360.dp), content = content) },
    confirmButton = {
      TextButton(onClick = onAdd, modifier = Modifier.testTag("add_item_button")) {
        Text(strings.add)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.close)
      }
    }
  )
}

@Composable
private fun EditableListRow(
  icon: ImageVector,
  color: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun CategoryEditorDialog(
  initial: CategoryEntity?,
  onSave: (CategoryEntity) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  var name by remember { mutableStateOf(initial?.name ?: "") }
  var isExpense by remember { mutableStateOf(true) }
  var iconName by remember { mutableStateOf(initial?.iconName ?: "category") }
  var colorHex by remember { mutableStateOf(initial?.colorHex ?: editorColors.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) strings.newCategory else strings.editCategory) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(strings.nameLabel) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("category_name_input")
        )

        if (initial == null) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = isExpense,
              onClick = { isExpense = true },
              label = { Text(strings.expense) }
            )
            FilterChip(
              selected = !isExpense,
              onClick = { isExpense = false },
              label = { Text(strings.income) }
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
          CategoryIconHelper.iconNames.forEach { icon ->
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                  if (icon == iconName) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                  } else Modifier
                )
                .clickable { iconName = icon },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = CategoryIconHelper.getIcon(icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }

        ColorChoiceRow(selected = colorHex, onSelect = { colorHex = it })
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onSave(
            initial?.copy(name = name.trim(), iconName = iconName, colorHex = colorHex)
              ?: CategoryEntity(
                name = name.trim(),
                iconName = iconName,
                colorHex = colorHex,
                isExpense = isExpense,
                isDefault = false
              )
          )
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("save_category_button")
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

@Composable
private fun AccountEditorDialog(
  initial: AccountEntity?,
  onSave: (AccountEntity) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  var name by remember { mutableStateOf(initial?.name ?: "") }
  var type by remember { mutableStateOf(initial?.type ?: AccountType.CASH) }
  var colorHex by remember { mutableStateOf(initial?.colorHex ?: editorColors.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) strings.newAccount else strings.editAccount) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(strings.nameLabel) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("account_name_input")
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
          AccountType.entries.forEach { option ->
            FilterChip(
              selected = type == option,
              onClick = { type = option },
              label = { Text(option.label()) }
            )
          }
        }

        ColorChoiceRow(selected = colorHex, onSelect = { colorHex = it })
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onSave(
            initial?.copy(name = name.trim(), type = type, colorHex = colorHex)
              ?: AccountEntity(name = name.trim(), type = type, balance = 0.0, colorHex = colorHex)
          )
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("save_account_button")
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

@Composable
private fun ColorChoiceRow(selected: String, onSelect: (String) -> Unit) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.horizontalScroll(rememberScrollState())
  ) {
    editorColors.forEach { hex ->
      Box(
        modifier = Modifier
          .size(32.dp)
          .background(parseColorOrDefault(hex), CircleShape)
          .then(
            if (hex == selected) {
              Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            } else Modifier
          )
          .clickable { onSelect(hex) }
      )
    }
  }
}
