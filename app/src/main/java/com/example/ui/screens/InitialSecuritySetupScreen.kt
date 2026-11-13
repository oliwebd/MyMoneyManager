package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.SupportedCurrencies
import com.example.data.model.SupportedLanguages
import com.example.ui.components.CurrencyPickerDialog
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.i18n.LocalAppStrings

@Composable
fun InitialSecuritySetupScreen(
  initialCurrencyCode: String,
  initialLanguageCode: String,
  onLanguageSelected: (String) -> Unit,
  onSetupComplete: (pin: String, enableBiometric: Boolean, currencyCode: String, languageCode: String) -> Unit,
  onSkipSetup: (currencyCode: String, languageCode: String) -> Unit,
  isBiometricAvailable: Boolean = true,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  var step by remember { mutableStateOf(1) } // 1 = Enter PIN, 2 = Confirm PIN
  var firstPin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var enableFingerprint by remember { mutableStateOf(isBiometricAvailable) }
  var selectedCurrency by remember(initialCurrencyCode) { mutableStateOf(initialCurrencyCode) }
  var selectedLanguage by remember(initialLanguageCode) { mutableStateOf(initialLanguageCode) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val currentPin = if (step == 1) firstPin else confirmPin
  val currencyDisplay = SupportedCurrencies.find { it.code == selectedCurrency }
    ?.let { "${it.code} (${it.symbol})" } ?: selectedCurrency
  val languageDisplay = SupportedLanguages.find { it.code == selectedLanguage }
    ?.nativeName ?: selectedLanguage

  fun handleDigitInput(digit: String) {
    errorMessage = null
    if (step == 1) {
      if (firstPin.length < 6) {
        val next = firstPin + digit
        firstPin = next
        if (next.length == 6) {
          step = 2
        }
      }
    } else {
      if (confirmPin.length < 6) {
        val next = confirmPin + digit
        confirmPin = next
        if (next.length == 6) {
          if (next == firstPin) {
            onSetupComplete(next, enableFingerprint, selectedCurrency, selectedLanguage)
          } else {
            errorMessage = strings.pinMismatchError
            confirmPin = ""
            step = 1
            firstPin = ""
          }
        }
      }
    }
  }

  fun handleBackspace() {
    errorMessage = null
    if (step == 1) {
      if (firstPin.isNotEmpty()) {
        firstPin = firstPin.dropLast(1)
      }
    } else {
      if (confirmPin.isNotEmpty()) {
        confirmPin = confirmPin.dropLast(1)
      } else {
        step = 1
      }
    }
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Brand Header & Setup Preferences
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = strings.welcomeTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (step == 1) strings.setPinTitle else strings.confirmPinTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (step == 1) strings.setPinSubtitle else strings.confirmPinSubtitle,
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Currency & Language Selectors
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { showCurrencyDialog = true }
              .testTag("setup_currency_selector")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CurrencyExchange,
                contentDescription = strings.currencyLabel,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.currencyLabel,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
                Text(
                  text = currencyDisplay,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { showLanguageDialog = true }
              .testTag("setup_language_selector")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Language,
                contentDescription = strings.languageLabel,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.languageLabel,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
                Text(
                  text = languageDisplay,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6 PIN Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(6) { index ->
            val filled = index < currentPin.length
            Box(
              modifier = Modifier
                .size(16.dp)
                .background(
                  color = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = CircleShape
                )
                .border(
                  width = 2.dp,
                  color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                  shape = CircleShape
                )
            )
          }
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fingerprint / Biometric Toggle
        if (isBiometricAvailable) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = strings.fingerprintUnlock,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = strings.fingerprintSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
              Switch(
                checked = enableFingerprint,
                onCheckedChange = { enableFingerprint = it },
                modifier = Modifier.testTag("setup_fingerprint_switch")
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Numeric Keypad
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("BACK", "0", "CLEAR")
        )

        for (row in rows) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            for (key in row) {
              when (key) {
                "BACK" -> {
                  Box(
                    modifier = Modifier
                      .size(62.dp)
                      .clip(CircleShape)
                      .clickable { handleBackspace() }
                      .testTag("setup_key_backspace"),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Backspace,
                      contentDescription = "Backspace",
                      tint = MaterialTheme.colorScheme.onBackground,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                "CLEAR" -> {
                  Box(
                    modifier = Modifier
                      .size(62.dp)
                      .clip(CircleShape)
                      .clickable {
                        firstPin = ""
                        confirmPin = ""
                        step = 1
                        errorMessage = null
                      }
                      .testTag("setup_key_clear"),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = strings.reset,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                else -> {
                  Box(
                    modifier = Modifier
                      .size(62.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .clickable { handleDigitInput(key) }
                      .testTag("setup_key_$key"),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = key,
                      style = MaterialTheme.typography.headlineSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onBackground
                    )
                  }
                }
              }
            }
          }
        }

        TextButton(
          onClick = { onSkipSetup(selectedCurrency, selectedLanguage) },
          modifier = Modifier.testTag("skip_security_setup_button")
        ) {
          Text(
            text = strings.skipSetup,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }

  if (showCurrencyDialog) {
    CurrencyPickerDialog(
      currentCurrencyCode = selectedCurrency,
      onSelect = { selectedCurrency = it },
      onDismiss = { showCurrencyDialog = false }
    )
  }

  if (showLanguageDialog) {
    LanguagePickerDialog(
      currentLanguageCode = selectedLanguage,
      onSelect = { code ->
        selectedLanguage = code
        onLanguageSelected(code)
      },
      onDismiss = { showLanguageDialog = false }
    )
  }
}
