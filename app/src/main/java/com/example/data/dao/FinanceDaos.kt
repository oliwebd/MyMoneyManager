package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.RecurringAlertEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY timestamp DESC")
  fun getTransactionsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 200")
  suspend fun getAllTransactionsSnapshot(): List<TransactionEntity>

  @Query("SELECT DISTINCT title FROM transactions ORDER BY timestamp DESC LIMIT 30")
  fun getRecentTitles(): Flow<List<String>>

  @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
  suspend fun getTransactionById(id: Long): TransactionEntity?

  @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
  fun getTransactionsByType(type: com.example.data.model.TransactionType): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<TransactionEntity>)

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Delete
  suspend fun deleteTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Query("DELETE FROM transactions")
  suspend fun clearAll()
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories ORDER BY isExpense ASC, name ASC")
  fun getAllCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories")
  suspend fun getAllCategoriesSnapshot(): List<CategoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryEntity>)

  @Update
  suspend fun updateCategory(category: CategoryEntity)

  @Delete
  suspend fun deleteCategory(category: CategoryEntity)

  @Query("DELETE FROM categories")
  suspend fun clearAll()
}

@Dao
interface MonthlyBudgetDao {
  @Query("SELECT * FROM monthly_budgets WHERE monthYear = :monthYear")
  fun getBudgetsForMonth(monthYear: String): Flow<List<MonthlyBudgetEntity>>

  @Query("SELECT * FROM monthly_budgets")
  suspend fun getAllBudgetsSnapshot(): List<MonthlyBudgetEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudget(budget: MonthlyBudgetEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudgets(budgets: List<MonthlyBudgetEntity>)

  @Update
  suspend fun updateBudget(budget: MonthlyBudgetEntity)

  @Delete
  suspend fun deleteBudget(budget: MonthlyBudgetEntity)

  @Query("DELETE FROM monthly_budgets WHERE id = :id")
  suspend fun deleteBudgetById(id: Long)

  @Query("SELECT * FROM monthly_budgets WHERE monthYear = :monthYear AND categoryId = :categoryId LIMIT 1")
  suspend fun getBudgetForCategory(monthYear: String, categoryId: Long): MonthlyBudgetEntity?

  @Query("DELETE FROM monthly_budgets")
  suspend fun clearAll()
}

@Dao
interface AccountDao {
  @Query("SELECT * FROM accounts ORDER BY id ASC")
  fun getAllAccounts(): Flow<List<AccountEntity>>

  @Query("SELECT * FROM accounts")
  suspend fun getAllAccountsSnapshot(): List<AccountEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccount(account: AccountEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccounts(accounts: List<AccountEntity>)

  @Update
  suspend fun updateAccount(account: AccountEntity)

  @Query("UPDATE accounts SET balance = balance + :amountDelta WHERE id = :accountId")
  suspend fun updateBalance(accountId: Long, amountDelta: Double)

  @Delete
  suspend fun deleteAccount(account: AccountEntity)

  @Query("DELETE FROM accounts")
  suspend fun clearAll()
}

@Dao
interface RecurringAlertDao {
  @Query("SELECT * FROM recurring_alerts WHERE isActive = 1 ORDER BY nextDueDate ASC")
  fun getActiveAlerts(): Flow<List<RecurringAlertEntity>>

  @Query("SELECT * FROM recurring_alerts")
  suspend fun getAllAlertsSnapshot(): List<RecurringAlertEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlert(alert: RecurringAlertEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlerts(alerts: List<RecurringAlertEntity>)

  @Update
  suspend fun updateAlert(alert: RecurringAlertEntity)

  @Delete
  suspend fun deleteAlert(alert: RecurringAlertEntity)

  @Query("DELETE FROM recurring_alerts")
  suspend fun clearAll()
}
