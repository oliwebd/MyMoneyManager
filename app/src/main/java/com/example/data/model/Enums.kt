package com.example.data.model

enum class TransactionType {
  EXPENSE,
  INCOME
}

enum class RecurringInterval {
  NONE,
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY
}

enum class AccountType {
  CASH,
  BANK,
  CARD,
  SAVINGS,
  WALLET
}

data class CurrencyItem(
  val code: String,
  val symbol: String,
  val name: String
)

val SupportedCurrencies = listOf(
  CurrencyItem("USD", "$", "US Dollar"),
  CurrencyItem("EUR", "€", "Euro"),
  CurrencyItem("GBP", "£", "British Pound"),
  CurrencyItem("JPY", "¥", "Japanese Yen"),
  CurrencyItem("CAD", "CA$", "Canadian Dollar"),
  CurrencyItem("AUD", "AU$", "Australian Dollar"),
  CurrencyItem("INR", "₹", "Indian Rupee"),
  CurrencyItem("CHF", "CHF", "Swiss Franc"),
  CurrencyItem("CNY", "¥", "Chinese Yuan"),
  CurrencyItem("SGD", "S$", "Singapore Dollar"),
  CurrencyItem("AED", "AED", "UAE Dirham")
)
