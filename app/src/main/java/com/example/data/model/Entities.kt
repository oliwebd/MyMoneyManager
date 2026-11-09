package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val amount: Double,
  val type: TransactionType,
  val categoryId: Long,
  val accountId: Long,
  val currencyCode: String = "USD",
  val timestamp: Long = System.currentTimeMillis(),
  val note: String = "",
  val isRecurring: Boolean = false,
  val recurringInterval: RecurringInterval = RecurringInterval.NONE,
  val tags: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val iconName: String,
  val colorHex: String,
  val isExpense: Boolean = true,
  val isDefault: Boolean = true
)

@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val monthYear: String, // format "YYYY-MM" e.g. "2026-11"
  val categoryId: Long,
  val budgetLimit: Double,
  val currencyCode: String = "USD"
)

@Entity(tableName = "accounts")
data class AccountEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val type: AccountType,
  val balance: Double,
  val currencyCode: String = "USD",
  val colorHex: String = "#1A73E8"
)

@Entity(tableName = "recurring_alerts")
data class RecurringAlertEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val transactionTitle: String,
  val amount: Double,
  val type: TransactionType,
  val categoryId: Long,
  val interval: RecurringInterval,
  val nextDueDate: Long,
  val isNotified: Boolean = false,
  val isActive: Boolean = true
)
