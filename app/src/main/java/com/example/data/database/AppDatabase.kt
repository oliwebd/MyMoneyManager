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
          "pocketsense_db"
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
      val transDao = database.transactionDao()
      val budgetDao = database.monthlyBudgetDao()
      val alertDao = database.recurringAlertDao()

      // Seed Categories
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

      // Seed Accounts
      val accounts = listOf(
        AccountEntity(id = 1, name = "Cash Wallet", type = AccountType.CASH, balance = 480.0, colorHex = "#0F9D58"),
        AccountEntity(id = 2, name = "Main Bank Checking", type = AccountType.BANK, balance = 3250.0, colorHex = "#1A73E8"),
        AccountEntity(id = 3, name = "Rewards Credit Card", type = AccountType.CARD, balance = -245.50, colorHex = "#EA4335"),
        AccountEntity(id = 4, name = "High-Yield Savings", type = AccountType.SAVINGS, balance = 9400.0, colorHex = "#007B83")
      )
      accDao.insertAccounts(accounts)

      // Current Month format
      val cal = Calendar.getInstance()
      val year = cal.get(Calendar.YEAR)
      val month = cal.get(Calendar.MONTH) + 1
      val currentMonthYear = String.format("%04d-%02d", year, month)

      // Seed Initial Budgets
      val budgets = listOf(
        MonthlyBudgetEntity(id = 1, monthYear = currentMonthYear, categoryId = 1, budgetLimit = 450.0), // Groceries
        MonthlyBudgetEntity(id = 2, monthYear = currentMonthYear, categoryId = 2, budgetLimit = 300.0), // Dining
        MonthlyBudgetEntity(id = 3, monthYear = currentMonthYear, categoryId = 3, budgetLimit = 180.0), // Transport
        MonthlyBudgetEntity(id = 4, monthYear = currentMonthYear, categoryId = 5, budgetLimit = 200.0), // Utilities
        MonthlyBudgetEntity(id = 5, monthYear = currentMonthYear, categoryId = 6, budgetLimit = 120.0)  // Entertainment
      )
      budgetDao.insertBudgets(budgets)

      // Seed ready-made sample transactions for historical charts
      val now = System.currentTimeMillis()
      val dayMillis = 86400000L
      val sampleTransactions = listOf(
        TransactionEntity(title = "Monthly Salary Deposit", amount = 3800.0, type = TransactionType.INCOME, categoryId = 9, accountId = 2, timestamp = now - 14 * dayMillis),
        TransactionEntity(title = "Apartment Rent Payment", amount = 1250.0, type = TransactionType.EXPENSE, categoryId = 4, accountId = 2, timestamp = now - 13 * dayMillis, isRecurring = true, recurringInterval = RecurringInterval.MONTHLY),
        TransactionEntity(title = "Whole Foods Organic Groceries", amount = 145.20, type = TransactionType.EXPENSE, categoryId = 1, accountId = 3, timestamp = now - 10 * dayMillis),
        TransactionEntity(title = "Electricity & Water Bill", amount = 88.50, type = TransactionType.EXPENSE, categoryId = 5, accountId = 2, timestamp = now - 8 * dayMillis),
        TransactionEntity(title = "Sushi Dinner with Friends", amount = 64.00, type = TransactionType.EXPENSE, categoryId = 2, accountId = 3, timestamp = now - 6 * dayMillis),
        TransactionEntity(title = "Freelance Mobile App Design", amount = 750.0, type = TransactionType.INCOME, categoryId = 10, accountId = 2, timestamp = now - 5 * dayMillis),
        TransactionEntity(title = "Subway & Metro Monthly Pass", amount = 75.00, type = TransactionType.EXPENSE, categoryId = 3, accountId = 1, timestamp = now - 4 * dayMillis),
        TransactionEntity(title = "Coffee & Bagel Breakfast", amount = 8.50, type = TransactionType.EXPENSE, categoryId = 2, accountId = 1, timestamp = now - 2 * dayMillis),
        TransactionEntity(title = "Supermarket Pantry Restock", amount = 94.30, type = TransactionType.EXPENSE, categoryId = 1, accountId = 3, timestamp = now - 1 * dayMillis),
        TransactionEntity(title = "Movie Night Streaming Service", amount = 17.99, type = TransactionType.EXPENSE, categoryId = 6, accountId = 3, timestamp = now - 12 * 3600000L, isRecurring = true, recurringInterval = RecurringInterval.MONTHLY)
      )
      transDao.insertTransactions(sampleTransactions)

      // Recurring alert
      val alert = RecurringAlertEntity(
        transactionTitle = "Internet Fiber Bill",
        amount = 65.0,
        type = TransactionType.EXPENSE,
        categoryId = 5,
        interval = RecurringInterval.MONTHLY,
        nextDueDate = now + 4 * dayMillis,
        isActive = true
      )
      alertDao.insertAlert(alert)
    }
  }
}
