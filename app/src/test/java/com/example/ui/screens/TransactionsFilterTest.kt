package com.example.ui.screens

import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TransactionsFilterTest {

  private val categories = listOf(
    CategoryEntity(id = 1L, name = "Dining & Food", iconName = "restaurant", colorHex = "#FF9800", isExpense = true),
    CategoryEntity(id = 2L, name = "Groceries", iconName = "shopping_cart", colorHex = "#4CAF50", isExpense = true),
    CategoryEntity(id = 3L, name = "Salary", iconName = "payments", colorHex = "#0F9D58", isExpense = false)
  )

  private fun localMillis(year: Int, monthZeroBased: Int, day: Int, hour: Int = 12): Long =
    Calendar.getInstance().apply {
      set(year, monthZeroBased, day, hour, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

  private val sampleTransactions = listOf(
    TransactionEntity(
      id = 1L,
      title = "Morning Espresso",
      amount = 4.50,
      type = TransactionType.EXPENSE,
      categoryId = 1L,
      accountId = 1L,
      timestamp = localMillis(2026, Calendar.NOVEMBER, 10, 9),
      note = "Cafe"
    ),
    TransactionEntity(
      id = 2L,
      title = "Weekly Market",
      amount = 65.00,
      type = TransactionType.EXPENSE,
      categoryId = 2L,
      accountId = 1L,
      timestamp = localMillis(2026, Calendar.NOVEMBER, 12, 18),
      note = "Fresh produce"
    ),
    TransactionEntity(
      id = 3L,
      title = "November Paycheck",
      amount = 3200.00,
      type = TransactionType.INCOME,
      categoryId = 3L,
      accountId = 2L,
      timestamp = localMillis(2026, Calendar.NOVEMBER, 13, 10),
      note = ""
    )
  )

  @Test
  fun testFilterByTitle() {
    val result = filterTransactions(
      transactions = sampleTransactions,
      categories = categories,
      searchQuery = "espresso",
      selectedTypeFilter = null,
      selectedCategoryId = null,
      selectedDateRange = null
    )
    assertEquals(1, result.size)
    assertEquals(1L, result.first().id)
  }

  @Test
  fun testFilterByCategoryNameInSearchBar() {
    val result = filterTransactions(
      transactions = sampleTransactions,
      categories = categories,
      searchQuery = "groceries",
      selectedTypeFilter = null,
      selectedCategoryId = null,
      selectedDateRange = null
    )
    assertEquals(1, result.size)
    assertEquals(2L, result.first().id)
  }

  @Test
  fun testFilterByDateStringInSearchBar() {
    val result = filterTransactions(
      transactions = sampleTransactions,
      categories = categories,
      searchQuery = "2026-11-13",
      selectedTypeFilter = null,
      selectedCategoryId = null,
      selectedDateRange = null
    )
    assertEquals(1, result.size)
    assertEquals(3L, result.first().id)
  }

  @Test
  fun testFilterByDateRangeWithUtcToLocalConversion() {
    val utcStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
      clear()
      set(2026, Calendar.NOVEMBER, 12)
    }.timeInMillis
    val utcEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
      clear()
      set(2026, Calendar.NOVEMBER, 13)
    }.timeInMillis

    val bounds = utcDateRangeToLocalBounds(utcStart, utcEnd)
    val result = filterTransactions(
      transactions = sampleTransactions,
      categories = categories,
      searchQuery = "",
      selectedTypeFilter = null,
      selectedCategoryId = null,
      selectedDateRange = bounds
    )
    assertEquals(listOf(2L, 3L), result.map { it.id })
    assertEquals(utcStart, localMillisToUtcDateMillis(bounds.first))
  }
}
