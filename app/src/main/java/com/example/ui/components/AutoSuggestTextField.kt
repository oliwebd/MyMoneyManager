package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SuggestionPreset(
  val title: String,
  val categoryName: String,
  val defaultAmount: Double? = null,
  val isIncome: Boolean = false
)

val ReadyMadeSuggestions = listOf(
  SuggestionPreset("Grocery Shopping", "Groceries", 65.0, false),
  SuggestionPreset("Supermarket Restock", "Groceries", 85.0, false),
  SuggestionPreset("Dining & Restaurant", "Dining & Food", 45.0, false),
  SuggestionPreset("Coffee & Snack", "Dining & Food", 6.50, false),
  SuggestionPreset("Apartment Rent", "Housing & Rent", 1200.0, false),
  SuggestionPreset("Electric & Utility Bill", "Utilities & Bills", 85.0, false),
  SuggestionPreset("Internet Fiber Subscription", "Utilities & Bills", 60.0, false),
  SuggestionPreset("Gas Station Fuel", "Transport", 45.0, false),
  SuggestionPreset("Public Transit Pass", "Transport", 30.0, false),
  SuggestionPreset("Monthly Salary", "Salary", 3500.0, true),
  SuggestionPreset("Freelance Invoice", "Freelance", 500.0, true),
  SuggestionPreset("Investment Dividend", "Investments", 120.0, true),
  SuggestionPreset("Cinema & Movie Night", "Entertainment", 24.0, false),
  SuggestionPreset("Gym Membership", "Healthcare", 40.0, false),
  SuggestionPreset("Online Shopping", "Shopping", 55.0, false)
)

@Composable
fun AutoSuggestTextField(
  value: String,
  onValueChange: (String) -> Unit,
  onSuggestionSelected: (SuggestionPreset) -> Unit,
  recentSavedTitles: List<String>,
  label: String = "Transaction Title",
  placeholder: String = "e.g., Grocery Shopping, Salary",
  modifier: Modifier = Modifier
) {
  // Compute matching suggestions
  val filteredSuggestions = remember(value, recentSavedTitles) {
    if (value.isBlank()) {
      // Show top popular presets when empty
      ReadyMadeSuggestions.take(6)
    } else {
      val query = value.trim().lowercase()
      val fromHistory = recentSavedTitles
        .filter { it.lowercase().contains(query) && !it.equals(value, ignoreCase = true) }
        .map { title ->
          val matchingPreset = ReadyMadeSuggestions.find { it.title.equals(title, ignoreCase = true) }
          matchingPreset ?: SuggestionPreset(title = title, categoryName = "")
        }
      val fromPresets = ReadyMadeSuggestions
        .filter { it.title.lowercase().contains(query) && !it.title.equals(value, ignoreCase = true) }

      (fromHistory + fromPresets).distinctBy { it.title.lowercase() }.take(8)
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next
      ),
      trailingIcon = {
        if (value.isNotEmpty()) {
          IconButton(
            onClick = { onValueChange("") },
            modifier = Modifier.testTag("clear_title_button")
          ) {
            Icon(Icons.Default.Clear, contentDescription = "Clear input")
          }
        }
      },
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("title_input_field")
    )

    // Suggestion chips scrollbar
    AnimatedVisibility(
      visible = filteredSuggestions.isNotEmpty(),
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (value.isBlank()) "Quick Suggestions:" else "Matching Suggestions:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          filteredSuggestions.forEach { suggestion ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
              ),
              modifier = Modifier
                .clickable {
                  onValueChange(suggestion.title)
                  onSuggestionSelected(suggestion)
                }
                .testTag("suggestion_chip_${suggestion.title.replace(" ", "_")}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = suggestion.title,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (suggestion.categoryName.isNotBlank()) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                  ) {
                    Text(
                      text = suggestion.categoryName,
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
