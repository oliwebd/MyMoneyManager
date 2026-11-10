package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.MonthlyBudgetDao
import com.example.data.dao.RecurringAlertDao
import com.example.data.dao.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.CategoryEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.RecurringAlertEntity
import com.example.data.model.RecurringInterval
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
  entities = [
    TransactionEntity::class,
    CategoryEntity::class,
    MonthlyBudgetEntity::class,
    AccountEntity::class,
    RecurringAlertEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun categoryDao(): CategoryDao
  abstract fun monthlyBudgetDao(): MonthlyBudgetDao
  abstract fun accountDao(): AccountDao
  abstract fun recurringAlertDao(): RecurringAlertDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "m3note_db"
        )
          .addCallback(AppDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class AppDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: AppDatabase) {
      val catDao = database.categoryDao()
      val accDao = database.accountDao()

      // Seed Default Categories (Clean structure for user to assign)
      val categories = listOf(
        CategoryEntity(id = 1, name = "Groceries", iconName = "shopping_cart", colorHex = "#4CAF50", isExpense = true),
        CategoryEntity(id = 2, name = "Dining & Food", iconName = "restaurant", colorHex = "#FF9800", isExpense = true),
        CategoryEntity(id = 3, name = "Transport", iconName = "directions_car", colorHex = "#2196F3", isExpense = true),
        CategoryEntity(id = 4, name = "Housing & Rent", iconName = "home", colorHex = "#9C27B0", isExpense = true),
        CategoryEntity(id = 5, name = "Utilities & Bills", iconName = "bolt", colorHex = "#00BCD4", isExpense = true),
        CategoryEntity(id = 6, name = "Entertainment", iconName = "movie", colorHex = "#E91E63", isExpense = true),
        CategoryEntity(id = 7, name = "Healthcare", iconName = "local_hospital", colorHex = "#F44336", isExpense = true),
        CategoryEntity(id = 8, name = "Shopping", iconName = "shopping_bag", colorHex = "#3F51B5", isExpense = true),
        CategoryEntity(id = 9, name = "Salary", iconName = "payments", colorHex = "#0F9D58", isExpense = false),
        CategoryEntity(id = 10, name = "Freelance", iconName = "work", colorHex = "#00897B", isExpense = false),
        CategoryEntity(id = 11, name = "Investments", iconName = "trending_up", colorHex = "#1A73E8", isExpense = false),
        CategoryEntity(id = 12, name = "Other Income", iconName = "account_balance_wallet", colorHex = "#673AB7", isExpense = false)
      )
      catDao.insertCategories(categories)

      // Seed Clean Accounts with 0.0 balance
      val accounts = listOf(
        AccountEntity(id = 1, name = "Cash Wallet", type = AccountType.CASH, balance = 0.0, colorHex = "#0F9D58"),
        AccountEntity(id = 2, name = "Main Bank Checking", type = AccountType.BANK, balance = 0.0, colorHex = "#1A73E8"),
        AccountEntity(id = 3, name = "Credit Card", type = AccountType.CARD, balance = 0.0, colorHex = "#EA4335"),
        AccountEntity(id = 4, name = "Savings", type = AccountType.SAVINGS, balance = 0.0, colorHex = "#007B83")
      )
      accDao.insertAccounts(accounts)
      // Pre-data cleaned: Zero initial transactions, zero initial budgets, zero dummy alerts.
    }
  }
}
