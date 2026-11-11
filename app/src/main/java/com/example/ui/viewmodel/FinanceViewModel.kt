package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.CategoryEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.RecurringAlertEntity
import com.example.data.model.RecurringInterval
import com.example.data.model.SupportedCurrencies
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.ui.components.CategorySlice
import com.example.ui.components.MonthlyTrendItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BudgetProgressItem(
  val budget: MonthlyBudgetEntity,
  val category: CategoryEntity?,
  val spent: Double,
  val limit: Double,
  val percentage: Int,
  val isOverBudget: Boolean,
  val isWarning: Boolean
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application, viewModelScope)
  val repository = FinanceRepository(application, database)

  val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeAlerts: StateFlow<List<RecurringAlertEntity>> = repository.activeAlerts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentTitles: StateFlow<List<String>> = repository.recentTitles
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Current selected month-year "YYYY-MM"
  private val _selectedMonthYear = MutableStateFlow(getCurrentMonthYear())
  val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

  val monthlyBudgets: StateFlow<List<MonthlyBudgetEntity>> = _selectedMonthYear
    .combine(allTransactions) { monthYear, _ -> monthYear }
    .combine(repository.getBudgetsForMonth(getCurrentMonthYear())) { _, budgets -> budgets }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Base Currency
  private val _baseCurrency = MutableStateFlow(repository.securityPrefs.getBaseCurrency())
  val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

  val currencySymbol: String
    get() = SupportedCurrencies.find { it.code == _baseCurrency.value }?.symbol ?: "$"

  // Security Lock State
  private val _isSetupDone = MutableStateFlow(repository.securityPrefs.isSetupDone())
  val isSetupDone: StateFlow<Boolean> = _isSetupDone.asStateFlow()

  private val _isUnlocked = MutableStateFlow(
    if (!repository.securityPrefs.isSetupDone()) false else !repository.securityPrefs.isSecurityEnabled()
  )
  val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

  private val _securityEnabled = MutableStateFlow(repository.securityPrefs.isSecurityEnabled())
  val securityEnabled: StateFlow<Boolean> = _securityEnabled.asStateFlow()

  private val _biometricEnabled = MutableStateFlow(repository.securityPrefs.isBiometricEnabled())
  val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

  private val _notificationsEnabled = MutableStateFlow(repository.securityPrefs.isNotificationsEnabled())
  val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

  // Transaction Filters
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _filterType = MutableStateFlow<TransactionType?>(null)
  val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

  private val _filterCategoryId = MutableStateFlow<Long?>(null)
  val filterCategoryId: StateFlow<Long?> = _filterCategoryId.asStateFlow()

  // Message toast / banner
  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  fun clearUserMessage() {
    _userMessage.value = null
  }

  fun showMessage(msg: String) {
    _userMessage.value = msg
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setFilterType(type: TransactionType?) {
    _filterType.value = type
  }

  fun setFilterCategoryId(id: Long?) {
    _filterCategoryId.value = id
  }

  fun setSelectedMonthYear(monthYear: String) {
    _selectedMonthYear.value = monthYear
  }

  fun changeMonth(offsetMonths: Int) {
    val current = _selectedMonthYear.value
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val date = sdf.parse(current) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.add(Calendar.MONTH, offsetMonths)
    _selectedMonthYear.value = sdf.format(cal.time)
  }

  // Security Operations
  fun unlockWithPin(pin: String): Boolean {
    val success = repository.securityPrefs.verifyPin(pin)
    if (success) {
      _isUnlocked.value = true
    }
    return success
  }

  fun unlockWithBiometric() {
    _isUnlocked.value = true
  }

  var skipNextLock = false

  fun lockApp() {
    if (_securityEnabled.value) {
      _isUnlocked.value = false
    }
  }

  fun completeInitialSecuritySetup(pin: String, enableBiometric: Boolean) {
    repository.securityPrefs.setPin(pin)
    repository.securityPrefs.setBiometricEnabled(enableBiometric)
    repository.securityPrefs.setSecurityEnabled(true)
    repository.securityPrefs.setSetupDone(true)
    _isSetupDone.value = true
    _isUnlocked.value = true
    _securityEnabled.value = true
    _biometricEnabled.value = enableBiometric
    showMessage("Security setup complete. Welcome to M3Note!")
  }

  fun skipInitialSecuritySetup() {
    repository.securityPrefs.setSecurityEnabled(false)
    repository.securityPrefs.setSetupDone(true)
    _isSetupDone.value = true
    _isUnlocked.value = true
    _securityEnabled.value = false
    showMessage("Welcome to M3Note! Security can be configured anytime in Vault settings.")
  }

  fun setNewPin(newPin: String) {
    repository.securityPrefs.setPin(newPin)
    showMessage("6-digit PIN successfully updated!")
  }

  fun toggleSecurity(enabled: Boolean) {
    if (enabled && !repository.securityPrefs.hasPin()) {
      repository.securityPrefs.setSecurityEnabled(false)
      _securityEnabled.value = false
      showMessage("Please set a PIN before enabling security lock.")
      return
    }
    repository.securityPrefs.setSecurityEnabled(enabled)
    _securityEnabled.value = enabled
    if (!enabled) {
      _isUnlocked.value = true
    }
  }

  fun toggleBiometric(enabled: Boolean) {
    repository.securityPrefs.setBiometricEnabled(enabled)
    _biometricEnabled.value = enabled
  }

  fun toggleNotifications(enabled: Boolean) {
    repository.securityPrefs.setNotificationsEnabled(enabled)
    _notificationsEnabled.value = enabled
  }

  fun setBaseCurrency(code: String) {
    repository.securityPrefs.setBaseCurrency(code)
    _baseCurrency.value = code
    showMessage("Base currency set to $code")
  }

  // Transactions CRUD
  fun addTransaction(
    title: String,
    amount: Double,
    type: TransactionType,
    categoryId: Long,
    accountId: Long,
    note: String = "",
    isRecurring: Boolean = false,
    interval: RecurringInterval = RecurringInterval.NONE,
    timestamp: Long = System.currentTimeMillis()
  ) {
    viewModelScope.launch {
      val transaction = TransactionEntity(
        title = title.trim(),
        amount = amount,
        type = type,
        categoryId = categoryId,
        accountId = accountId,
        currencyCode = _baseCurrency.value,
        timestamp = timestamp,
        note = note.trim(),
        isRecurring = isRecurring,
        recurringInterval = interval
      )
      repository.insertTransaction(transaction)

      if (isRecurring && interval != RecurringInterval.NONE) {
        val nextDue = calculateNextDueDate(timestamp, interval)
        repository.insertAlert(
          RecurringAlertEntity(
            transactionTitle = title,
            amount = amount,
            type = type,
            categoryId = categoryId,
            interval = interval,
            nextDueDate = nextDue,
            isActive = true
          )
        )
      }
      showMessage("${if (type == TransactionType.INCOME) "Income" else "Expense"} added successfully!")
    }
  }

  fun deleteTransaction(transaction: TransactionEntity) {
    viewModelScope.launch {
      repository.deleteTransaction(transaction)
      showMessage("Transaction removed.")
    }
  }

  // Budget CRUD
  fun setBudgetForCategory(categoryId: Long, limit: Double) {
    viewModelScope.launch {
      val budget = MonthlyBudgetEntity(
        monthYear = _selectedMonthYear.value,
        categoryId = categoryId,
        budgetLimit = limit,
        currencyCode = _baseCurrency.value
      )
      repository.insertBudget(budget)
      showMessage("Budget goal saved.")
    }
  }

  fun deleteBudget(budget: MonthlyBudgetEntity) {
    viewModelScope.launch {
      repository.deleteBudget(budget)
      showMessage("Budget removed.")
    }
  }

  // Backup & Encrypted Google Drive Export/Import
  fun exportEncryptedBackup(
    passphrase: CharArray,
    onReady: (Intent) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val uri = repository.createEncryptedBackupFile(passphrase)
        val shareIntent = repository.createShareBackupIntent(uri)
        onReady(shareIntent)
      } catch (e: Exception) {
        onError(e.message ?: "Failed to generate encrypted backup.")
      }
    }
  }

  fun importEncryptedBackup(
    fileUri: Uri,
    passphrase: CharArray,
    onSuccess: (Int) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val count = repository.restoreEncryptedBackup(fileUri, passphrase)
        showMessage("Restored $count records successfully!")
        onSuccess(count)
      } catch (e: Exception) {
        onError(e.message ?: "Failed to decrypt backup. Please check your password.")
      }
    }
  }

  // Computed Analytical Trends
  fun getCategorySlices(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    targetMonthYear: String
  ): Pair<Double, List<CategorySlice>> {
    val monthExpenses = transactions.filter {
      it.type == TransactionType.EXPENSE &&
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.timestamp)) == targetMonthYear
    }
    val totalExpense = monthExpenses.sumOf { it.amount }
    if (totalExpense <= 0.0) return Pair(0.0, emptyList())

    val grouped = monthExpenses.groupBy { it.categoryId }
    val slices = grouped.mapNotNull { (catId, transList) ->
      val cat = categories.find { it.id == catId } ?: return@mapNotNull null
      val sum = transList.sumOf { it.amount }
      val pct = (sum / totalExpense).toFloat()
      val color = parseColorSafe(cat.colorHex)
      CategorySlice(category = cat, amount = sum, percentage = pct, color = color)
    }.sortedByDescending { it.amount }

    return Pair(totalExpense, slices)
  }

  fun getHistoricalTrends(transactions: List<TransactionEntity>): List<MonthlyTrendItem> {
    val sdfKey = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val sdfLabel = SimpleDateFormat("MMM", Locale.getDefault())

    val cal = Calendar.getInstance()
    val result = mutableListOf<MonthlyTrendItem>()

    // Last 6 months
    for (i in 5 downTo 0) {
      val c = Calendar.getInstance()
      c.add(Calendar.MONTH, -i)
      val key = sdfKey.format(c.time)
      val label = sdfLabel.format(c.time)

      val monthTrans = transactions.filter { sdfKey.format(Date(it.timestamp)) == key }
      val income = monthTrans.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
      val expense = monthTrans.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

      result.add(MonthlyTrendItem(monthLabel = label, income = income, expense = expense))
    }
    return result
  }

  fun getBudgetProgress(
    budgets: List<MonthlyBudgetEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    monthYear: String
  ): List<BudgetProgressItem> {
    val activeBudgets = budgets.filter { it.monthYear == monthYear }
    val monthTrans = transactions.filter {
      it.type == TransactionType.EXPENSE &&
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.timestamp)) == monthYear
    }

    return activeBudgets.map { budget ->
      val cat = categories.find { it.id == budget.categoryId }
      val spent = monthTrans.filter { it.categoryId == budget.categoryId }.sumOf { it.amount }
      val pct = if (budget.budgetLimit > 0) ((spent / budget.budgetLimit) * 100).toInt() else 0
      BudgetProgressItem(
        budget = budget,
        category = cat,
        spent = spent,
        limit = budget.budgetLimit,
        percentage = pct,
        isOverBudget = pct >= 100,
        isWarning = pct in 75..99
      )
    }.sortedByDescending { it.percentage }
  }

  private fun calculateNextDueDate(fromTimestamp: Long, interval: RecurringInterval): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = fromTimestamp
    when (interval) {
      RecurringInterval.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
      RecurringInterval.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
      RecurringInterval.MONTHLY -> cal.add(Calendar.MONTH, 1)
      RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
      RecurringInterval.NONE -> {}
    }
    return cal.timeInMillis
  }

  private fun parseColorSafe(hex: String): Color {
    return try {
      Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
      Color(0xFF1A73E8)
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
      showMessage("All data has been wiped clean.")
    }
  }

  companion object {
    fun getCurrentMonthYear(): String {
      return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
  }
}
