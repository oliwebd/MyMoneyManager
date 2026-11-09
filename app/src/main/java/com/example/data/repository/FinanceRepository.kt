package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.crypto.BackupData
import com.example.data.crypto.CryptoManager
import com.example.data.database.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.RecurringAlertEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.notification.NotificationHelper
import com.example.security.SecurityPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(
  private val context: Context,
  private val database: AppDatabase
) {
  val securityPrefs = SecurityPreferences(context)
  val notificationHelper = NotificationHelper(context)

  private val transactionDao = database.transactionDao()
  private val categoryDao = database.categoryDao()
  private val budgetDao = database.monthlyBudgetDao()
  private val accountDao = database.accountDao()
  private val alertDao = database.recurringAlertDao()

  val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
  val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
  val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
  val activeAlerts: Flow<List<RecurringAlertEntity>> = alertDao.getActiveAlerts()
  val recentTitles: Flow<List<String>> = transactionDao.getRecentTitles()

  fun getBudgetsForMonth(monthYear: String): Flow<List<MonthlyBudgetEntity>> {
    return budgetDao.getBudgetsForMonth(monthYear)
  }

  suspend fun insertTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
    val id = transactionDao.insertTransaction(transaction)
    // Update account balance
    val delta = if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
    accountDao.updateBalance(transaction.accountId, delta)

    // Check budget thresholds if it's an expense
    if (transaction.type == TransactionType.EXPENSE && securityPrefs.isNotificationsEnabled()) {
      checkAndTriggerBudgetAlert(transaction.categoryId)
    }
    id
  }

  suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
    transactionDao.deleteTransaction(transaction)
    // Revert account balance
    val revertDelta = if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
    accountDao.updateBalance(transaction.accountId, revertDelta)
  }

  suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
    categoryDao.insertCategory(category)
  }

  suspend fun insertBudget(budget: MonthlyBudgetEntity) = withContext(Dispatchers.IO) {
    budgetDao.insertBudget(budget)
  }

  suspend fun deleteBudget(budget: MonthlyBudgetEntity) = withContext(Dispatchers.IO) {
    budgetDao.deleteBudget(budget)
  }

  suspend fun insertAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
    accountDao.insertAccount(account)
  }

  suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
    accountDao.updateAccount(account)
  }

  suspend fun insertAlert(alert: RecurringAlertEntity) = withContext(Dispatchers.IO) {
    alertDao.insertAlert(alert)
  }

  suspend fun updateAlert(alert: RecurringAlertEntity) = withContext(Dispatchers.IO) {
    alertDao.updateAlert(alert)
  }

  suspend fun deleteAlert(alert: RecurringAlertEntity) = withContext(Dispatchers.IO) {
    alertDao.deleteAlert(alert)
  }

  private suspend fun checkAndTriggerBudgetAlert(categoryId: Long) {
    val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    val budgets = budgetDao.getAllBudgetsSnapshot().filter { it.monthYear == currentMonth && it.categoryId == categoryId }
    if (budgets.isEmpty()) return

    val budget = budgets.first()
    val allTrans = transactionDao.getAllTransactionsSnapshot()
    val spent = allTrans.filter {
      it.type == TransactionType.EXPENSE &&
        it.categoryId == categoryId &&
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.timestamp)) == currentMonth
    }.sumOf { it.amount }

    val percent = ((spent / budget.budgetLimit) * 100).toInt()
    if (percent >= 80) {
      val categories = categoryDao.getAllCategoriesSnapshot()
      val catName = categories.find { it.id == categoryId }?.name ?: "Category"
      val currencySymbol = com.example.data.model.SupportedCurrencies
        .find { it.code == securityPrefs.getBaseCurrency() }?.symbol ?: "$"
      notificationHelper.sendBudgetAlert(catName, percent, spent, budget.budgetLimit, currencySymbol)
    }
  }

  /**
   * Exports an encrypted AES-256-GCM backup file and returns a shareable FileProvider URI.
   */
  suspend fun createEncryptedBackupFile(passphrase: CharArray): Uri = withContext(Dispatchers.IO) {
    val data = BackupData(
      transactions = transactionDao.getAllTransactionsSnapshot(),
      categories = categoryDao.getAllCategoriesSnapshot(),
      budgets = budgetDao.getAllBudgetsSnapshot(),
      accounts = accountDao.getAllAccountsSnapshot(),
      recurringAlerts = alertDao.getAllAlertsSnapshot()
    )

    val encryptedBytes = CryptoManager.encryptBackup(data, passphrase)
    val fileName = "mymoneymanager_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mymoney"
    val cacheDir = File(context.cacheDir, "backups")
    if (!cacheDir.exists()) cacheDir.mkdirs()
    val backupFile = File(cacheDir, fileName)

    FileOutputStream(backupFile).use {
      it.write(encryptedBytes)
    }

    FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      backupFile
    )
  }

  /**
   * Restores database records from an encrypted backup file.
   */
  suspend fun restoreEncryptedBackup(fileUri: Uri, passphrase: CharArray): Int = withContext(Dispatchers.IO) {
    val encryptedBytes = CryptoManager.readBytesFromUri(context, fileUri)
    val backupData = CryptoManager.decryptBackup(encryptedBytes, passphrase)

    // Clear and restore tables
    transactionDao.clearAll()
    categoryDao.clearAll()
    budgetDao.clearAll()
    accountDao.clearAll()
    alertDao.clearAll()

    categoryDao.insertCategories(backupData.categories)
    accountDao.insertAccounts(backupData.accounts)
    budgetDao.insertBudgets(backupData.budgets)
    transactionDao.insertTransactions(backupData.transactions)
    alertDao.insertAlerts(backupData.recurringAlerts)

    backupData.transactions.size
  }

  /**
   * Creates an intent to share the encrypted backup directly with Google Drive or any app.
   */
  fun createShareBackupIntent(fileUri: Uri): Intent {
    return Intent(Intent.ACTION_SEND).apply {
      type = "application/octet-stream"
      putExtra(Intent.EXTRA_STREAM, fileUri)
      putExtra(Intent.EXTRA_SUBJECT, "myMoneymanager Encrypted Backup")
      putExtra(Intent.EXTRA_TEXT, "Here is your AES-256 encrypted myMoneymanager financial backup. Store this safely in Google Drive.")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}
