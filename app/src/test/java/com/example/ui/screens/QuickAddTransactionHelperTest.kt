package com.example.ui.screens

import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickAddTransactionHelperTest {

  private val foodCategory = CategoryEntity(
    id = 1L,
    name = "Dining & Food",
    iconName = "restaurant",
    colorHex = "#FF9800",
    isExpense = true
  )

  @Test
  fun testSanitizeQuickAddAmountInput() {
    assertEquals("12.34", sanitizeQuickAddAmountInput("12.345"))
    assertEquals("12.34", sanitizeQuickAddAmountInput("1a2.3.4"))
    assertEquals("0.5", sanitizeQuickAddAmountInput(".5"))
    assertEquals("", sanitizeQuickAddAmountInput("abc"))
  }

  @Test
  fun testParseQuickAddAmount() {
    assertNull(parseQuickAddAmount(""))
    assertNull(parseQuickAddAmount("0"))
    assertNull(parseQuickAddAmount("0.00"))
    assertNull(parseQuickAddAmount("."))
    assertEquals(15.5, parseQuickAddAmount("15.50")!!, 0.001)
  }

  @Test
  fun testResolveQuickAddTitle() {
    assertEquals("Coffee", resolveQuickAddTitle("  Coffee  ", foodCategory, TransactionType.EXPENSE))
    assertEquals("Dining & Food", resolveQuickAddTitle("   ", foodCategory, TransactionType.EXPENSE))
    assertEquals("General Income", resolveQuickAddTitle("", null, TransactionType.INCOME))
    assertEquals("General Expense", resolveQuickAddTitle("", null, TransactionType.EXPENSE))
  }
}
