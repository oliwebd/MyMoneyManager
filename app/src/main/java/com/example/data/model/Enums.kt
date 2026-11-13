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
  // Top Global Currencies
  CurrencyItem("USD", "$", "US Dollar"),
  CurrencyItem("EUR", "€", "Euro"),
  CurrencyItem("GBP", "£", "British Pound"),
  CurrencyItem("JPY", "¥", "Japanese Yen"),
  CurrencyItem("CHF", "CHF", "Swiss Franc"),
  CurrencyItem("CAD", "CA$", "Canadian Dollar"),
  CurrencyItem("AUD", "AU$", "Australian Dollar"),
  CurrencyItem("CNY", "¥", "Chinese Yuan"),
  CurrencyItem("INR", "₹", "Indian Rupee"),
  CurrencyItem("SGD", "S$", "Singapore Dollar"),
  CurrencyItem("HKD", "HK$", "Hong Kong Dollar"),
  CurrencyItem("NZD", "NZ$", "New Zealand Dollar"),
  CurrencyItem("KRW", "₩", "South Korean Won"),
  CurrencyItem("BRL", "R$", "Brazilian Real"),
  CurrencyItem("MXN", "Mex$", "Mexican Peso"),

  // South & East Asia
  CurrencyItem("BDT", "৳", "Bangladeshi Taka"),
  CurrencyItem("IDR", "Rp", "Indonesian Rupiah"),
  CurrencyItem("MYR", "RM", "Malaysian Ringgit"),
  CurrencyItem("PHP", "₱", "Philippine Peso"),
  CurrencyItem("PKR", "₨", "Pakistani Rupee"),
  CurrencyItem("THB", "฿", "Thai Baht"),
  CurrencyItem("VND", "₫", "Vietnamese Dong"),
  CurrencyItem("TWD", "NT$", "New Taiwan Dollar"),
  CurrencyItem("LKR", "Rs", "Sri Lankan Rupee"),
  CurrencyItem("NPR", "रू", "Nepalese Rupee"),

  // Europe & Nordics
  CurrencyItem("SEK", "kr", "Swedish Krona"),
  CurrencyItem("NOK", "kr", "Norwegian Krone"),
  CurrencyItem("DKK", "kr", "Danish Krone"),
  CurrencyItem("PLN", "zł", "Polish Zloty"),
  CurrencyItem("CZK", "Kč", "Czech Koruna"),
  CurrencyItem("HUF", "Ft", "Hungarian Forint"),
  CurrencyItem("RON", "lei", "Romanian Leu"),
  CurrencyItem("BGN", "лв", "Bulgarian Lev"),
  CurrencyItem("ISK", "kr", "Icelandic Krona"),
  CurrencyItem("TRY", "₺", "Turkish Lira"),
  CurrencyItem("UAH", "₴", "Ukrainian Hryvnia"),
  CurrencyItem("RUB", "₽", "Russian Ruble"),

  // Middle East
  CurrencyItem("AED", "AED", "UAE Dirham"),
  CurrencyItem("SAR", "SAR", "Saudi Riyal"),
  CurrencyItem("QAR", "QAR", "Qatari Riyal"),
  CurrencyItem("KWD", "KD", "Kuwaiti Dinar"),
  CurrencyItem("BHD", "BD", "Bahraini Dinar"),
  CurrencyItem("OMR", "OMR", "Omani Rial"),
  CurrencyItem("ILS", "₪", "Israeli New Shekel"),
  CurrencyItem("JOD", "JD", "Jordanian Dinar"),

  // Africa
  CurrencyItem("ZAR", "R", "South African Rand"),
  CurrencyItem("NGN", "₦", "Nigerian Naira"),
  CurrencyItem("EGP", "E£", "Egyptian Pound"),
  CurrencyItem("KES", "KSh", "Kenyan Shilling"),
  CurrencyItem("GHS", "GH₵", "Ghanaian Cedi"),
  CurrencyItem("MAD", "MAD", "Moroccan Dirham"),
  CurrencyItem("TZS", "TSh", "Tanzanian Shilling"),
  CurrencyItem("UGX", "USh", "Ugandan Shilling"),

  // Latin America
  CurrencyItem("ARS", "AR$", "Argentine Peso"),
  CurrencyItem("CLP", "CL$", "Chilean Peso"),
  CurrencyItem("COP", "COL$", "Colombian Peso"),
  CurrencyItem("PEN", "S/.", "Peruvian Sol")
)

data class LanguageItem(
  val code: String,
  val nativeName: String,
  val englishName: String,
  val isRtl: Boolean = false
)

val SupportedLanguages = listOf(
  LanguageItem("en", "English", "English"),
  LanguageItem("es", "Español", "Spanish"),
  LanguageItem("fr", "Français", "French"),
  LanguageItem("de", "Deutsch", "German"),
  LanguageItem("it", "Italiano", "Italian"),
  LanguageItem("pt", "Português", "Portuguese"),
  LanguageItem("nl", "Nederlands", "Dutch"),
  LanguageItem("pl", "Polski", "Polish"),
  LanguageItem("ru", "Русский", "Russian"),
  LanguageItem("uk", "Українська", "Ukrainian"),
  LanguageItem("tr", "Türkçe", "Turkish"),
  LanguageItem("ar", "العربية", "Arabic", isRtl = true),
  LanguageItem("fa", "فارسی", "Persian", isRtl = true),
  LanguageItem("ur", "اردو", "Urdu", isRtl = true),
  LanguageItem("hi", "हिन्दी", "Hindi"),
  LanguageItem("bn", "বাংলা", "Bengali"),
  LanguageItem("zh", "简体中文", "Chinese (Simplified)"),
  LanguageItem("ja", "日本語", "Japanese"),
  LanguageItem("ko", "한국어", "Korean"),
  LanguageItem("id", "Bahasa Indonesia", "Indonesian"),
  LanguageItem("ms", "Bahasa Melayu", "Malay"),
  LanguageItem("vi", "Tiếng Việt", "Vietnamese"),
  LanguageItem("th", "ไทย", "Thai")
)

