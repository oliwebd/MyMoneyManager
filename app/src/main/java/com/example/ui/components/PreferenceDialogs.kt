package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SupportedCurrencies
import com.example.data.model.SupportedLanguages
import com.example.ui.i18n.LocalAppStrings

@Composable
fun CurrencyPickerDialog(
  currentCurrencyCode: String,
  onSelect: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  var query by remember { mutableStateOf("") }
  val filtered = remember(query) {
    if (query.isBlank()) {
      SupportedCurrencies
    } else {
      val q = query.trim().lowercase()
      SupportedCurrencies.filter {
        it.code.lowercase().contains(q) ||
          it.name.lowercase().contains(q) ||
          it.symbol.lowercase().contains(q)
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("${strings.selectCurrency} (${SupportedCurrencies.size})") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(strings.searchPlaceholder) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { query = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("currency_search_input")
        )

        LazyColumn(
          modifier = Modifier.heightIn(max = 360.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(filtered.size) { index ->
            val curr = filtered[index]
            val isSelected = curr.code == currentCurrencyCode
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelect(curr.code)
                  onDismiss()
                }
                .testTag("currency_item_${curr.code}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${curr.code} • ${curr.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
                Text(
                  text = curr.symbol,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

@Composable
fun LanguagePickerDialog(
  currentLanguageCode: String,
  onSelect: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  var query by remember { mutableStateOf("") }
  val filtered = remember(query) {
    if (query.isBlank()) {
      SupportedLanguages
    } else {
      val q = query.trim().lowercase()
      SupportedLanguages.filter {
        it.code.lowercase().contains(q) ||
          it.nativeName.lowercase().contains(q) ||
          it.englishName.lowercase().contains(q)
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("${strings.selectLanguage} (${SupportedLanguages.size})") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(strings.searchPlaceholder) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { query = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("language_search_input")
        )

        LazyColumn(
          modifier = Modifier.heightIn(max = 360.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(filtered.size) { index ->
            val lang = filtered[index]
            val isSelected = lang.code == currentLanguageCode
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelect(lang.code)
                  onDismiss()
                }
                .testTag("language_item_${lang.code}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = lang.nativeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (lang.nativeName != lang.englishName) {
                    Text(
                      text = lang.englishName,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                Text(
                  text = lang.code.uppercase(),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}
