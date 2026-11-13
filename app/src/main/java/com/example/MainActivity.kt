package com.example

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.view.WindowManager
import android.widget.Toast
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.SupportedLanguages
import com.example.data.model.TransactionEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.appStringsFor
import com.example.ui.screens.AddTransactionSheet
import com.example.ui.screens.AuthLockScreen
import com.example.ui.screens.BackupSettingsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InitialSecuritySetupScreen
import com.example.ui.screens.QuickAddTransactionModal
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyMoneyManagerTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

enum class AppScreen {
  DASHBOARD,
  TRANSACTIONS,
  BUDGETS,
  SETTINGS
}

class MainActivity : FragmentActivity() {

  private val viewModel: FinanceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    enableEdgeToEdge()

    setContent {
      MyMoneyManagerTheme {
        MyMoneyManagerApp(
          onLaunchBiometric = { onBiometricSuccess, onBiometricFailure ->
            showBiometricPrompt(onBiometricSuccess, onBiometricFailure)
          }
        )
      }
    }
  }

  override fun onStop() {
    super.onStop()
    if (!isChangingConfigurations) viewModel.lockApp()
  }

  companion object {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val BIOMETRIC_KEY_ALIAS = "m3note_biometric_auth_key"
  }

  private fun getOrCreateBiometricKey(): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null)
    val existingKey = keyStore.getKey(BIOMETRIC_KEY_ALIAS, null) as? SecretKey
    if (existingKey != null) {
      return existingKey
    }

    val keyGenerator = KeyGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_AES,
      ANDROID_KEYSTORE
    )
    val spec = KeyGenParameterSpec.Builder(
      BIOMETRIC_KEY_ALIAS,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
      .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
      .setUserAuthenticationRequired(true)
      .build()
    keyGenerator.init(spec)
    return keyGenerator.generateKey()
  }

  private fun createBiometricCryptoObject(): BiometricPrompt.CryptoObject {
    val transformation = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    val cipher = Cipher.getInstance(transformation)
    try {
      val secretKey = getOrCreateBiometricKey()
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      return BiometricPrompt.CryptoObject(cipher)
    } catch (e: KeyPermanentlyInvalidatedException) {
      val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
      keyStore.load(null)
      keyStore.deleteEntry(BIOMETRIC_KEY_ALIAS)
      val newKey = getOrCreateBiometricKey()
      cipher.init(Cipher.ENCRYPT_MODE, newKey)
      return BiometricPrompt.CryptoObject(cipher)
    }
  }

  private fun showBiometricPrompt(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
  ) {
    val biometricManager = BiometricManager.from(this)
    val canAuth = biometricManager.canAuthenticate(
      BiometricManager.Authenticators.BIOMETRIC_STRONG
    )

    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
      onFailure("Biometric authentication not configured or unavailable on this device.")
      return
    }

    val cryptoObject = try {
      createBiometricCryptoObject()
    } catch (e: Exception) {
      onFailure("Failed to initialize biometric security key: ${e.message}")
      return
    }

    val executor = ContextCompat.getMainExecutor(this)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("Unlock M3Note")
      .setSubtitle("Confirm your biometric identity to access your financial records")
      .setNegativeButtonText("Use 6-Digit PIN")
      .build()

    val biometricPrompt = BiometricPrompt(
      this,
      executor,
      object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
          super.onAuthenticationSucceeded(result)
          // The Keystore key requires user auth, so doFinal only succeeds after a real biometric
          val verified = runCatching { result.cryptoObject?.cipher?.doFinal(ByteArray(1)) }.getOrNull() != null
          if (verified) onSuccess() else onFailure("Biometric authentication verification failed.")
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
          super.onAuthenticationError(errorCode, errString)
          onFailure(errString.toString())
        }

        override fun onAuthenticationFailed() {
          super.onAuthenticationFailed()
        }
      }
    )

    biometricPrompt.authenticate(promptInfo, cryptoObject)
  }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MyMoneyManagerApp(
  onLaunchBiometric: (onSuccess: () -> Unit, onFailure: (String) -> Unit) -> Unit,
  viewModel: FinanceViewModel = viewModel()
) {
  val isSetupDone by viewModel.isSetupDone.collectAsStateWithLifecycle()
  val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
  val securityEnabled by viewModel.securityEnabled.collectAsStateWithLifecycle()
  val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
  val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
  val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
  val recentTitles by viewModel.recentTitles.collectAsStateWithLifecycle()
  val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
  val baseCurrency by viewModel.baseCurrency.collectAsStateWithLifecycle()
  val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
  val monthlyBudgets by viewModel.monthlyBudgets.collectAsStateWithLifecycle()
  val strings = remember(appLanguage) { appStringsFor(appLanguage) }
  val layoutDirection = remember(appLanguage) {
    if (SupportedLanguages.find { it.code == appLanguage }?.isRtl == true) LayoutDirection.Rtl else LayoutDirection.Ltr
  }

  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val filterType by viewModel.filterType.collectAsStateWithLifecycle()
  val filterCategoryId by viewModel.filterCategoryId.collectAsStateWithLifecycle()
  val filterDateRange by viewModel.filterDateRange.collectAsStateWithLifecycle()

  var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
  var showAddSheet by remember { mutableStateOf(false) }
  var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
  var showQuickAddModal by remember { mutableStateOf(false) }
  val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  // Lives above the lock gate so a restore file pick survives the app locking
  var pickedImportUri by remember { mutableStateOf<Uri?>(null) }
  val importFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { pickedImportUri = it }

  // Android 13+ Notification permission request
  val notificationLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    viewModel.toggleNotifications(isGranted)
  }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && notificationsEnabled) {
      notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  // Display user message snackbar
  LaunchedEffect(userMessage) {
    userMessage?.let {
      coroutineScope.launch {
        snackbarHostState.showSnackbar(it)
        viewModel.clearUserMessage()
      }
    }
  }

  // Back handler for sub-tabs
  BackHandler(enabled = isUnlocked && currentScreen != AppScreen.DASHBOARD) {
    currentScreen = AppScreen.DASHBOARD
  }

  CompositionLocalProvider(
    LocalAppStrings provides strings,
    LocalLayoutDirection provides layoutDirection
  ) {
  // Initial Security Setup Screen (PIN / Fingerprint / Currency / Language setup on first run)
  if (!isSetupDone) {
    InitialSecuritySetupScreen(
      initialCurrencyCode = baseCurrency,
      initialLanguageCode = appLanguage,
      onLanguageSelected = { viewModel.setAppLanguage(it) },
      onSetupComplete = { pin, enableBiometric, currencyCode, languageCode ->
        viewModel.completeInitialSecuritySetup(pin, enableBiometric, currencyCode, languageCode)
      },
      onSkipSetup = { currencyCode, languageCode ->
        viewModel.skipInitialSecuritySetup(currencyCode, languageCode)
      },
      isBiometricAvailable = true
    )
    return@CompositionLocalProvider
  }

  // Locked Screen
  if (!isUnlocked && securityEnabled) {
    AuthLockScreen(
      onUnlockWithPin = { pin ->
        viewModel.unlockWithPin(pin)
      },
      onBiometricRequested = {
        if (biometricEnabled) {
          onLaunchBiometric(
            { viewModel.unlockWithBiometric() },
            { err -> viewModel.showMessage(err) }
          )
        }
      },
      isBiometricAvailable = biometricEnabled
    )
    return@CompositionLocalProvider
  }

  // Analytical data
  val (monthExpense, categorySlices) = remember(transactions, categories, selectedMonthYear) {
    viewModel.getCategorySlices(transactions, categories, selectedMonthYear)
  }
  val historicalTrends = remember(transactions) {
    viewModel.getHistoricalTrends(transactions)
  }
  val budgetProgressList = remember(monthlyBudgets, categories, transactions, selectedMonthYear) {
    viewModel.getBudgetProgress(monthlyBudgets, categories, transactions, selectedMonthYear)
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        NavigationBarItem(
          selected = currentScreen == AppScreen.DASHBOARD,
          onClick = { currentScreen = AppScreen.DASHBOARD },
          icon = { Icon(Icons.Default.PieChart, contentDescription = strings.navDashboard) },
          label = { Text(strings.navDashboard) },
          modifier = Modifier.testTag("nav_tab_dashboard")
        )
        NavigationBarItem(
          selected = currentScreen == AppScreen.TRANSACTIONS,
          onClick = { currentScreen = AppScreen.TRANSACTIONS },
          icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = strings.navTransactions) },
          label = { Text(strings.navTransactions) },
          modifier = Modifier.testTag("nav_tab_transactions")
        )
        NavigationBarItem(
          selected = currentScreen == AppScreen.BUDGETS,
          onClick = { currentScreen = AppScreen.BUDGETS },
          icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = strings.navBudgets) },
          label = { Text(strings.navBudgets) },
          modifier = Modifier.testTag("nav_tab_budgets")
        )
        NavigationBarItem(
          selected = currentScreen == AppScreen.SETTINGS,
          onClick = { currentScreen = AppScreen.SETTINGS },
          icon = { Icon(Icons.Default.Shield, contentDescription = strings.navVault) },
          label = { Text(strings.navVault) },
          modifier = Modifier.testTag("nav_tab_settings")
        )
      }
    },
    floatingActionButton = {
      if (currentScreen == AppScreen.DASHBOARD) {
        FloatingActionButton(
          onClick = { showQuickAddModal = true },
          shape = RoundedCornerShape(16.dp),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("dashboard_fab_add")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = strings.quickAdd,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .consumeWindowInsets(innerPadding)
    ) {
      when (currentScreen) {
        AppScreen.DASHBOARD -> {
          DashboardScreen(
            selectedMonthYear = selectedMonthYear,
            onPrevMonth = { viewModel.changeMonth(-1) },
            onNextMonth = { viewModel.changeMonth(1) },
            transactions = transactions,
            categories = categories,
            accounts = accounts,
            activeAlerts = activeAlerts,
            categorySlices = categorySlices,
            totalMonthExpense = monthExpense,
            historicalTrends = historicalTrends,
            currencySymbol = viewModel.currencySymbol,
            baseCurrencyCode = baseCurrency,
            onOpenAddTransaction = { showAddSheet = true },
            onNavigateToTransactions = { currentScreen = AppScreen.TRANSACTIONS },
            onNavigateToBudgets = { currentScreen = AppScreen.BUDGETS },
            onNavigateToBackup = { currentScreen = AppScreen.SETTINGS },
            onLockApp = { viewModel.lockApp() }
          )
        }

        AppScreen.TRANSACTIONS -> {
          TransactionsScreen(
            transactions = transactions,
            categories = categories,
            currencySymbol = viewModel.currencySymbol,
            searchQuery = searchQuery,
            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
            selectedTypeFilter = filterType,
            onTypeFilterChanged = { viewModel.setFilterType(it) },
            selectedCategoryId = filterCategoryId,
            onCategoryFilterChanged = { viewModel.setFilterCategoryId(it) },
            selectedDateRange = filterDateRange,
            onDateRangeFilterChanged = { viewModel.setFilterDateRange(it) },
            onDeleteTransaction = { viewModel.deleteTransaction(it) },
            onEditTransaction = { transactionToEdit = it },
            onOpenAddTransaction = { showAddSheet = true }
          )
        }

        AppScreen.BUDGETS -> {
          BudgetsScreen(
            monthYear = selectedMonthYear,
            budgetProgressList = budgetProgressList,
            categories = categories,
            currencySymbol = viewModel.currencySymbol,
            onSaveBudget = { catId, limit -> viewModel.setBudgetForCategory(catId, limit) },
            onDeleteBudget = { viewModel.deleteBudget(it) },
            onTriggerTestNotification = {
              viewModel.repository.notificationHelper.sendBudgetAlert(
                categoryName = "Groceries",
                percent = 85,
                spent = 382.50,
                limit = 450.00,
                currencySymbol = viewModel.currencySymbol
              )
              viewModel.showMessage("Budget alert notification sent!")
            }
          )
        }

        AppScreen.SETTINGS -> {
          BackupSettingsScreen(
            currentCurrencyCode = baseCurrency,
            onCurrencySelected = { viewModel.setBaseCurrency(it) },
            currentLanguageCode = appLanguage,
            onLanguageSelected = { viewModel.setAppLanguage(it) },
            categories = categories,
            onSaveCategory = { viewModel.saveCategory(it) },
            accounts = accounts,
            onSaveAccount = { viewModel.saveAccount(it) },
            securityEnabled = securityEnabled,
            onToggleSecurity = { on, pin -> viewModel.toggleSecurity(on, pin) },
            biometricEnabled = biometricEnabled,
            onToggleBiometric = { viewModel.toggleBiometric(it) },
            notificationsEnabled = notificationsEnabled,
            onToggleNotifications = { viewModel.toggleNotifications(it) },
            onChangePin = { newPin, currentPin -> viewModel.setNewPin(newPin, currentPin) },
            onExportEncrypted = { pass, onReady, onError ->
              viewModel.exportEncryptedBackup(pass, onReady, onError)
            },
            onImportEncrypted = { uri, pass, onSuccess, onError ->
              viewModel.importEncryptedBackup(uri, pass, onSuccess, onError)
            },
            onTriggerTestNotification = {
              viewModel.repository.notificationHelper.sendRecurringBillReminder(
                title = "Internet Fiber Bill",
                amount = 65.0,
                currencySymbol = viewModel.currencySymbol,
                daysUntilDue = 3
              )
              viewModel.showMessage("Recurring reminder notification sent!")
            },
            onClearAllData = {
              viewModel.clearAllData()
            },
            onPickFile = { importFileLauncher.launch("*/*") },
            pickedUri = pickedImportUri,
            onPickedUriHandled = { pickedImportUri = null }
          )
        }
      }

      // Quick Add Transaction Modal (Dashboard FAB)
      if (showQuickAddModal) {
        QuickAddTransactionModal(
          categories = categories,
          accounts = accounts,
          recentTitles = recentTitles,
          currencySymbol = viewModel.currencySymbol,
          onDismiss = { showQuickAddModal = false },
          onSave = { title, amount, type, categoryId, accountId ->
            viewModel.addTransaction(
              title = title,
              amount = amount,
              type = type,
              categoryId = categoryId,
              accountId = accountId
            )
            showQuickAddModal = false
          }
        )
      }

      // Add Transaction Sheet Modal
      if (showAddSheet || transactionToEdit != null) {
        AddTransactionSheet(
          sheetState = addSheetState,
          categories = categories,
          accounts = accounts,
          recentTitles = recentTitles,
          currencySymbol = viewModel.currencySymbol,
          initialTransaction = transactionToEdit,
          onDismiss = {
            showAddSheet = false
            transactionToEdit = null
          },
          onSave = { title, amount, type, categoryId, accountId, note, isRecurring, interval, timestamp ->
            val editing = transactionToEdit
            if (editing != null) {
              viewModel.updateTransaction(
                old = editing,
                title = title,
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                timestamp = timestamp
              )
            } else {
              viewModel.addTransaction(
                title = title,
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                isRecurring = isRecurring,
                interval = interval,
                timestamp = timestamp
              )
            }
            coroutineScope.launch {
              addSheetState.hide()
              showAddSheet = false
              transactionToEdit = null
            }
          }
        )
      }
    }
  }
  }
}
