package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportedCurrencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
  currentCurrencyCode: String,
  onCurrencySelected: (String) -> Unit,
  securityEnabled: Boolean,
  onToggleSecurity: (Boolean) -> Unit,
  biometricEnabled: Boolean,
  onToggleBiometric: (Boolean) -> Unit,
  notificationsEnabled: Boolean,
  onToggleNotifications: (Boolean) -> Unit,
  onChangePin: (String) -> Unit,
  onExportEncrypted: (passphrase: CharArray, onReady: (Intent) -> Unit, onError: (String) -> Unit) -> Unit,
  onImportEncrypted: (uri: Uri, passphrase: CharArray, onSuccess: (Int) -> Unit, onError: (String) -> Unit) -> Unit,
  onTriggerTestNotification: () -> Unit,
  onClearAllData: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var showExportDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showChangePinDialog by remember { mutableStateOf(false) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showClearDataDialog by remember { mutableStateOf(false) }
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  // File picker launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      selectedFileUri = uri
      showImportDialog = true
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = "Security & Cloud Sync",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
      )
      Text(
        text = "AES-256 encrypted backups with Google Drive & biometric protection",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Encrypted Backup & Google Drive Section
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Encrypted Cloud Backup & Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Strong AES-256-GCM zero-knowledge encryption",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = { showExportDialog = true },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("export_backup_button")
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Export & Drive")
            }

            Button(
              onClick = { filePickerLauncher.launch("*/*") },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              modifier = Modifier
                .weight(1f)
                .testTag("import_backup_button")
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Restore")
            }
          }
        }
      }
    }

    // App Preferences Section
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Base Currency Selector Row
          SettingsActionRow(
            icon = Icons.Default.CurrencyExchange,
            title = "Base Currency",
            subtitle = SupportedCurrencies.find { it.code == currentCurrencyCode }?.let { "${it.code} (${it.symbol} - ${it.name})" } ?: currentCurrencyCode,
            onClick = { showCurrencyDialog = true },
            testTag = "currency_selector_row"
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Notification Toggle Row
          SettingsSwitchRow(
            icon = Icons.Default.Notifications,
            title = "Notifications & Alerts",
            subtitle = "Threshold warnings (80% & 100%) and recurring bill reminders",
            checked = notificationsEnabled,
            onCheckedChange = onToggleNotifications
          )
        }
      }
    }

    // Security & Biometric Section
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Authentication & Lock",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Security Lock Toggle
          SettingsSwitchRow(
            icon = Icons.Default.Lock,
            title = "Passcode Protection",
            subtitle = "Lock app when closed or minimized",
            checked = securityEnabled,
            onCheckedChange = onToggleSecurity
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Biometrics Toggle
          SettingsSwitchRow(
            icon = Icons.Default.Fingerprint,
            title = "Biometric Authentication",
            subtitle = "Unlock via Fingerprint or Face recognition",
            checked = biometricEnabled,
            onCheckedChange = onToggleBiometric
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Change 6-Digit PIN Action
          SettingsActionRow(
            icon = Icons.Default.Key,
            title = "Change 6-Digit PIN",
            subtitle = "Update your master security PIN fallback",
            onClick = { showChangePinDialog = true },
            testTag = "change_pin_row"
          )
        }
      }
    }

    // Data Management & Clean Reset
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Clean all pre-data or wipe database records",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showClearDataDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("clear_all_data_button")
          ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear All Pre-Data & Transactions", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // GDPR & Privacy Notice
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Policy,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "GDPR & Privacy Sovereignty",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "M3Note operates 100% offline-first. Your financial entries, balances, and reports never leave your device unencrypted. Backups exported to Google Drive use client-side AES-256-GCM encryption with password key derivation (PBKDF2), ensuring zero-knowledge privacy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 17.sp
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(48.dp))
    }
  }

  // Export Passphrase Dialog
  if (showExportDialog) {
    var passphraseInput by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    var exportError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Encrypted Backup") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Enter a strong password to encrypt your database using AES-256-GCM before sharing to Google Drive:",
            style = MaterialTheme.typography.bodySmall
          )
          OutlinedTextField(
            value = passphraseInput,
            onValueChange = { passphraseInput = it; exportError = null },
            label = { Text("Backup Password") },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("export_passphrase_input")
          )
          OutlinedTextField(
            value = confirmPassphrase,
            onValueChange = { confirmPassphrase = it; exportError = null },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("export_confirm_input")
          )
          if (exportError != null) {
            Text(text = exportError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (passphraseInput.length < 4) {
              exportError = "Password must be at least 4 characters."
              return@Button
            }
            if (passphraseInput != confirmPassphrase) {
              exportError = "Passwords do not match."
              return@Button
            }
            onExportEncrypted(
              passphraseInput.toCharArray(),
              { shareIntent ->
                showExportDialog = false
                context.startActivity(Intent.createChooser(shareIntent, "Share or Save to Google Drive"))
              },
              { err -> exportError = err }
            )
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("confirm_export_button")
        ) {
          Text("Encrypt & Share")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Import Dialog
  if (showImportDialog && selectedFileUri != null) {
    var importPassphrase by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showImportDialog = false },
      title = { Text("Restore Encrypted Backup") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Enter the password you used when creating this myMoneymanager backup to decrypt and restore your data:",
            style = MaterialTheme.typography.bodySmall
          )
          OutlinedTextField(
            value = importPassphrase,
            onValueChange = { importPassphrase = it; importError = null },
            label = { Text("Decryption Password") },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("import_passphrase_input")
          )
          if (importError != null) {
            Text(text = importError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (importPassphrase.isBlank()) {
              importError = "Please enter password."
              return@Button
            }
            onImportEncrypted(
              selectedFileUri!!,
              importPassphrase.toCharArray(),
              { count ->
                showImportDialog = false
              },
              { err -> importError = err }
            )
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("confirm_import_button")
        ) {
          Text("Decrypt & Restore")
        }
      },
      dismissButton = {
        TextButton(onClick = { showImportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Change 6-Digit PIN Dialog
  if (showChangePinDialog) {
    var newPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showChangePinDialog = false },
      title = { Text("Set New 6-Digit PIN") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Enter a 6-digit numeric PIN for fallback app unlocking:",
            style = MaterialTheme.typography.bodySmall
          )
          OutlinedTextField(
            value = newPinInput,
            onValueChange = {
              if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                newPinInput = it
                pinError = null
              }
            },
            label = { Text("New 6-Digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
          )
          if (pinError != null) {
            Text(text = pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPinInput.length != 6) {
              pinError = "PIN must be exactly 6 digits."
              return@Button
            }
            onChangePin(newPinInput)
            showChangePinDialog = false
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("save_new_pin_button")
        ) {
          Text("Save PIN")
        }
      },
      dismissButton = {
        TextButton(onClick = { showChangePinDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Currency Selection Dialog
  if (showCurrencyDialog) {
    var currencySearchQuery by remember { mutableStateOf("") }
    val filteredCurrencies = remember(currencySearchQuery) {
      if (currencySearchQuery.isBlank()) {
        SupportedCurrencies
      } else {
        val q = currencySearchQuery.trim().lowercase()
        SupportedCurrencies.filter {
          it.code.lowercase().contains(q) ||
            it.name.lowercase().contains(q) ||
            it.symbol.lowercase().contains(q)
        }
      }
    }

    AlertDialog(
      onDismissRequest = { showCurrencyDialog = false },
      title = { Text("Select Base Currency (${SupportedCurrencies.size} Available)") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = currencySearchQuery,
            onValueChange = { currencySearchQuery = it },
            placeholder = { Text("Search code, symbol, or country...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
              if (currencySearchQuery.isNotEmpty()) {
                IconButton(onClick = { currencySearchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
              }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("currency_search_input")
          )

          LazyColumn(
            modifier = Modifier.heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(filteredCurrencies.size) { index ->
              val curr = filteredCurrencies[index]
              val isSelected = curr.code == currentCurrencyCode
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onCurrencySelected(curr.code)
                    showCurrencyDialog = false
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

            if (filteredCurrencies.isEmpty()) {
              item {
                Text(
                  text = "No matching currencies found.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(vertical = 16.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCurrencyDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // Clear All Data Confirmation Dialog
  if (showClearDataDialog) {
    AlertDialog(
      onDismissRequest = { showClearDataDialog = false },
      icon = {
        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
      },
      title = { Text("Wipe All Data?") },
      text = {
        Text(
          text = "This will erase all recorded transactions, budget limits, recurring alerts, and reset account balances to 0.0. This action cannot be undone unless you have an exported backup.",
          style = MaterialTheme.typography.bodyMedium
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showClearDataDialog = false
            onClearAllData()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          modifier = Modifier.testTag("confirm_wipe_data_button")
        ) {
          Text("Yes, Wipe All Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDataDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun SettingsSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
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
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}

@Composable
private fun SettingsActionRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp)
      .testTag(testTag),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
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
}
