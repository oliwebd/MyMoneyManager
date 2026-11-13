package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SupportedCurrencies
import com.example.data.model.SupportedLanguages
import com.example.ui.components.AccountsDialog
import com.example.ui.components.CategoriesDialog
import com.example.ui.components.CurrencyPickerDialog
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
  currentCurrencyCode: String,
  onCurrencySelected: (String) -> Unit,
  currentLanguageCode: String = "en",
  onLanguageSelected: (String) -> Unit = {},
  categories: List<CategoryEntity> = emptyList(),
  onSaveCategory: (CategoryEntity) -> Unit = {},
  accounts: List<AccountEntity> = emptyList(),
  onSaveAccount: (AccountEntity) -> Unit = {},
  securityEnabled: Boolean,
  onToggleSecurity: (Boolean, String) -> Boolean,
  biometricEnabled: Boolean,
  onToggleBiometric: (Boolean) -> Unit,
  notificationsEnabled: Boolean,
  onToggleNotifications: (Boolean) -> Unit,
  onChangePin: (newPin: String, currentPin: String) -> Boolean,
  onExportEncrypted: (passphrase: CharArray, onReady: (Intent) -> Unit, onError: (String) -> Unit) -> Unit,
  onImportEncrypted: (uri: Uri, passphrase: CharArray, onSuccess: (Int) -> Unit, onError: (String) -> Unit) -> Unit,
  onTriggerTestNotification: () -> Unit,
  onClearAllData: () -> Unit = {},
  onPickFile: () -> Unit = {},
  pickedUri: Uri? = null,
  onPickedUriHandled: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current

  var showExportDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showChangePinDialog by remember { mutableStateOf(false) }
  var pinConfirm by remember { mutableStateOf<((String) -> Boolean)?>(null) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var showCategoriesDialog by remember { mutableStateOf(false) }
  var showAccountsDialog by remember { mutableStateOf(false) }
  var showClearDataDialog by remember { mutableStateOf(false) }
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(pickedUri) {
    pickedUri?.let {
      selectedFileUri = it
      showImportDialog = true
      onPickedUriHandled()
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
        text = strings.navVault,
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
        shape = RoundedCornerShape(24.dp),
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
                text = strings.encryptedBackup,
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
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("export_backup_button")
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.backup)
            }

            Button(
              onClick = onPickFile,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              modifier = Modifier
                .weight(1f)
                .testTag("import_backup_button")
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.restore)
            }
          }
        }
      }
    }

    // App Preferences Section
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = strings.preferences,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Base Currency Selector Row
          SettingsActionRow(
            icon = Icons.Default.CurrencyExchange,
            title = strings.baseCurrency,
            subtitle = SupportedCurrencies.find { it.code == currentCurrencyCode }?.let { "${it.code} (${it.symbol} - ${it.name})" } ?: currentCurrencyCode,
            onClick = { showCurrencyDialog = true },
            testTag = "currency_selector_row"
          )

          Spacer(modifier = Modifier.height(8.dp))

          // App Language Selector Row
          SettingsActionRow(
            icon = Icons.Default.Language,
            title = strings.appLanguage,
            subtitle = SupportedLanguages.find { it.code == currentLanguageCode }?.let { "${it.nativeName} (${it.englishName})" } ?: currentLanguageCode,
            onClick = { showLanguageDialog = true },
            testTag = "language_selector_row"
          )

          Spacer(modifier = Modifier.height(8.dp))

          SettingsActionRow(
            icon = Icons.Default.Category,
            title = strings.categoriesTitle,
            subtitle = strings.categoriesSubtitle,
            onClick = { showCategoriesDialog = true },
            testTag = "categories_row"
          )

          Spacer(modifier = Modifier.height(8.dp))

          SettingsActionRow(
            icon = Icons.Default.AccountBalance,
            title = strings.accountsTitle,
            subtitle = strings.accountsSubtitle,
            onClick = { showAccountsDialog = true },
            testTag = "accounts_row"
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Notification Toggle Row
          SettingsSwitchRow(
            icon = Icons.Default.Notifications,
            title = strings.notificationsAlerts,
            subtitle = strings.notificationsSubtitle,
            checked = notificationsEnabled,
            onCheckedChange = onToggleNotifications
          )
        }
      }
    }

    // Security & Biometric Section
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = strings.authAndLock,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Security Lock Toggle
          SettingsSwitchRow(
            icon = Icons.Default.Lock,
            title = strings.passcodeProtection,
            subtitle = strings.passcodeSubtitle,
            checked = securityEnabled,
            onCheckedChange = { on ->
              if (on || !securityEnabled) onToggleSecurity(on, "") else pinConfirm = { onToggleSecurity(false, it) }
            }
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Biometrics Toggle
          SettingsSwitchRow(
            icon = Icons.Default.Fingerprint,
            title = strings.biometricAuth,
            subtitle = strings.biometricSubtitle,
            checked = biometricEnabled,
            onCheckedChange = onToggleBiometric
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Change 6-Digit PIN Action
          SettingsActionRow(
            icon = Icons.Default.Key,
            title = strings.changePin,
            subtitle = strings.changePinSubtitle,
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
    var currentPinInput by remember { mutableStateOf("") }
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
          if (securityEnabled) {
            PinField(currentPinInput, "Current PIN", "current_pin_input") {
              currentPinInput = it
              pinError = null
            }
          }
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
            if (onChangePin(newPinInput, currentPinInput)) {
              showChangePinDialog = false
            } else {
              pinError = "Incorrect current PIN or too many attempts."
            }
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

  pinConfirm?.let { ConfirmPinDialog(onDismiss = { pinConfirm = null }, onConfirm = it) }

  // Currency Selection Dialog
  if (showCurrencyDialog) {
    CurrencyPickerDialog(
      currentCurrencyCode = currentCurrencyCode,
      onSelect = onCurrencySelected,
      onDismiss = { showCurrencyDialog = false }
    )
  }

  // Language Selection Dialog
  if (showCategoriesDialog) {
    CategoriesDialog(
      categories = categories,
      onSave = onSaveCategory,
      onDismiss = { showCategoriesDialog = false }
    )
  }

  if (showAccountsDialog) {
    AccountsDialog(
      accounts = accounts,
      onSave = onSaveAccount,
      onDismiss = { showAccountsDialog = false }
    )
  }

  if (showLanguageDialog) {
    LanguagePickerDialog(
      currentLanguageCode = currentLanguageCode,
      onSelect = onLanguageSelected,
      onDismiss = { showLanguageDialog = false }
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
private fun PinField(value: String, label: String, tag: String, onValueChange: (String) -> Unit) {
  OutlinedTextField(
    value = value,
    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) onValueChange(it) },
    label = { Text(label) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    visualTransformation = PasswordVisualTransformation(),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth().testTag(tag)
  )
}

@Composable
private fun ConfirmPinDialog(onDismiss: () -> Unit, onConfirm: (String) -> Boolean) {
  var pin by remember { mutableStateOf("") }
  var failed by remember { mutableStateOf(false) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Confirm Current PIN") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PinField(pin, "Current PIN", "confirm_pin_input") {
          pin = it
          failed = false
        }
        if (failed) {
          Text("Incorrect PIN or too many attempts.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (onConfirm(pin)) onDismiss() else failed = true }) { Text("Confirm") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
  )
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
