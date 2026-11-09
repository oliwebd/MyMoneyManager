package com.example.data.crypto

import android.content.Context
import android.net.Uri
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.RecurringAlertEntity
import com.example.data.model.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupData(
  val version: Int = 1,
  val exportTimestamp: Long = System.currentTimeMillis(),
  val transactions: List<TransactionEntity>,
  val categories: List<CategoryEntity>,
  val budgets: List<MonthlyBudgetEntity>,
  val accounts: List<AccountEntity>,
  val recurringAlerts: List<RecurringAlertEntity>
)

object CryptoManager {
  private const val MAGIC_HEADER = "MYMONEYMANAGER_ENC_V1"
  private const val LEGACY_HEADER_EXPENSEWISE = "EXPENSEWISE_ENC_V1"
  private const val LEGACY_HEADER_POCKETSENSE = "POCKETSENSE_ENC_V1"
  private const val SALT_LENGTH = 16
  private const val IV_LENGTH = 12
  private const val KEY_LENGTH = 256
  private const val ITERATIONS = 65536
  private const val GCM_TAG_LENGTH = 128

  /**
   * Encrypts the backup data model into AES-256-GCM encrypted byte array using user passphrase.
   */
  fun encryptBackup(data: BackupData, passphrase: CharArray): ByteArray {
    val jsonString = serializeToJson(data)
    val plainBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

    val random = SecureRandom()
    val salt = ByteArray(SALT_LENGTH)
    random.nextBytes(salt)

    val iv = ByteArray(IV_LENGTH)
    random.nextBytes(iv)

    // Key derivation via PBKDF2WithHmacSHA256
    val spec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val secretKeyBytes = factory.generateSecret(spec).encoded
    val secretKey = SecretKeySpec(secretKeyBytes, "AES")

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
    val cipherBytes = cipher.doFinal(plainBytes)

    val headerBytes = MAGIC_HEADER.toByteArray(StandardCharsets.UTF_8)
    val outputStream = ByteArrayOutputStream()
    outputStream.write(headerBytes)
    outputStream.write(salt)
    outputStream.write(iv)
    outputStream.write(cipherBytes)

    return outputStream.toByteArray()
  }

  /**
   * Decrypts AES-256-GCM encrypted backup byte array using user passphrase.
   */
  fun decryptBackup(encryptedBytes: ByteArray, passphrase: CharArray): BackupData {
    val recognizedHeaders = listOf(MAGIC_HEADER, LEGACY_HEADER_EXPENSEWISE, LEGACY_HEADER_POCKETSENSE)
    var matchedHeader: String? = null
    var headerLength = 0

    for (candidate in recognizedHeaders) {
      val candidateBytes = candidate.toByteArray(StandardCharsets.UTF_8)
      if (encryptedBytes.size >= candidateBytes.size + SALT_LENGTH + IV_LENGTH) {
        val filePrefix = String(encryptedBytes.copyOfRange(0, candidateBytes.size), StandardCharsets.UTF_8)
        if (filePrefix == candidate) {
          matchedHeader = candidate
          headerLength = candidateBytes.size
          break
        }
      }
    }

    if (matchedHeader == null) {
      throw IllegalArgumentException("Invalid file format: Not a recognized myMoneymanager encrypted backup.")
    }

    var offset = headerLength
    val salt = encryptedBytes.copyOfRange(offset, offset + SALT_LENGTH)
    offset += SALT_LENGTH

    val iv = encryptedBytes.copyOfRange(offset, offset + IV_LENGTH)
    offset += IV_LENGTH

    val cipherBytes = encryptedBytes.copyOfRange(offset, encryptedBytes.size)

    // Re-derive key
    val spec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val secretKeyBytes = factory.generateSecret(spec).encoded
    val secretKey = SecretKeySpec(secretKeyBytes, "AES")

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

    val decryptedBytes = cipher.doFinal(cipherBytes)
    val jsonString = String(decryptedBytes, StandardCharsets.UTF_8)

    return parseFromJson(jsonString)
  }

  fun readBytesFromUri(context: Context, uri: Uri): ByteArray {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    return inputStream?.use { it.readBytes() } ?: throw IllegalArgumentException("Could not read from file.")
  }

  private fun serializeToJson(data: BackupData): String {
    val root = JSONObject()
    root.put("version", data.version)
    root.put("exportTimestamp", data.exportTimestamp)

    // Transactions
    val transArray = JSONArray()
    for (t in data.transactions) {
      val obj = JSONObject()
      obj.put("id", t.id)
      obj.put("title", t.title)
      obj.put("amount", t.amount)
      obj.put("type", t.type.name)
      obj.put("categoryId", t.categoryId)
      obj.put("accountId", t.accountId)
      obj.put("currencyCode", t.currencyCode)
      obj.put("timestamp", t.timestamp)
      obj.put("note", t.note)
      obj.put("isRecurring", t.isRecurring)
      obj.put("recurringInterval", t.recurringInterval.name)
      obj.put("tags", t.tags)
      transArray.put(obj)
    }
    root.put("transactions", transArray)

    // Categories
    val catArray = JSONArray()
    for (c in data.categories) {
      val obj = JSONObject()
      obj.put("id", c.id)
      obj.put("name", c.name)
      obj.put("iconName", c.iconName)
      obj.put("colorHex", c.colorHex)
      obj.put("isExpense", c.isExpense)
      obj.put("isDefault", c.isDefault)
      catArray.put(obj)
    }
    root.put("categories", catArray)

    // Budgets
    val budgetArray = JSONArray()
    for (b in data.budgets) {
      val obj = JSONObject()
      obj.put("id", b.id)
      obj.put("monthYear", b.monthYear)
      obj.put("categoryId", b.categoryId)
      obj.put("budgetLimit", b.budgetLimit)
      obj.put("currencyCode", b.currencyCode)
      budgetArray.put(obj)
    }
    root.put("budgets", budgetArray)

    // Accounts
    val accArray = JSONArray()
    for (a in data.accounts) {
      val obj = JSONObject()
      obj.put("id", a.id)
      obj.put("name", a.name)
      obj.put("type", a.type.name)
      obj.put("balance", a.balance)
      obj.put("currencyCode", a.currencyCode)
      obj.put("colorHex", a.colorHex)
      accArray.put(obj)
    }
    root.put("accounts", accArray)

    // Recurring
    val recArray = JSONArray()
    for (r in data.recurringAlerts) {
      val obj = JSONObject()
      obj.put("id", r.id)
      obj.put("transactionTitle", r.transactionTitle)
      obj.put("amount", r.amount)
      obj.put("type", r.type.name)
      obj.put("categoryId", r.categoryId)
      obj.put("interval", r.interval.name)
      obj.put("nextDueDate", r.nextDueDate)
      obj.put("isNotified", r.isNotified)
      obj.put("isActive", r.isActive)
      recArray.put(obj)
    }
    root.put("recurringAlerts", recArray)

    return root.toString(2)
  }

  private fun parseFromJson(jsonString: String): BackupData {
    val root = JSONObject(jsonString)
    val version = root.optInt("version", 1)
    val exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis())

    // Transactions
    val transactions = mutableListOf<TransactionEntity>()
    val transArray = root.optJSONArray("transactions") ?: JSONArray()
    for (i in 0 until transArray.length()) {
      val obj = transArray.getJSONObject(i)
      transactions.add(
        TransactionEntity(
          id = obj.optLong("id", 0L),
          title = obj.getString("title"),
          amount = obj.getDouble("amount"),
          type = com.example.data.model.TransactionType.valueOf(obj.getString("type")),
          categoryId = obj.getLong("categoryId"),
          accountId = obj.getLong("accountId"),
          currencyCode = obj.optString("currencyCode", "USD"),
          timestamp = obj.getLong("timestamp"),
          note = obj.optString("note", ""),
          isRecurring = obj.optBoolean("isRecurring", false),
          recurringInterval = com.example.data.model.RecurringInterval.valueOf(obj.optString("recurringInterval", "NONE")),
          tags = obj.optString("tags", "")
        )
      )
    }

    // Categories
    val categories = mutableListOf<CategoryEntity>()
    val catArray = root.optJSONArray("categories") ?: JSONArray()
    for (i in 0 until catArray.length()) {
      val obj = catArray.getJSONObject(i)
      categories.add(
        CategoryEntity(
          id = obj.optLong("id", 0L),
          name = obj.getString("name"),
          iconName = obj.optString("iconName", "category"),
          colorHex = obj.optString("colorHex", "#1A73E8"),
          isExpense = obj.optBoolean("isExpense", true),
          isDefault = obj.optBoolean("isDefault", false)
        )
      )
    }

    // Budgets
    val budgets = mutableListOf<MonthlyBudgetEntity>()
    val budgetArray = root.optJSONArray("budgets") ?: JSONArray()
    for (i in 0 until budgetArray.length()) {
      val obj = budgetArray.getJSONObject(i)
      budgets.add(
        MonthlyBudgetEntity(
          id = obj.optLong("id", 0L),
          monthYear = obj.getString("monthYear"),
          categoryId = obj.getLong("categoryId"),
          budgetLimit = obj.getDouble("budgetLimit"),
          currencyCode = obj.optString("currencyCode", "USD")
        )
      )
    }

    // Accounts
    val accounts = mutableListOf<AccountEntity>()
    val accArray = root.optJSONArray("accounts") ?: JSONArray()
    for (i in 0 until accArray.length()) {
      val obj = accArray.getJSONObject(i)
      accounts.add(
        AccountEntity(
          id = obj.optLong("id", 0L),
          name = obj.getString("name"),
          type = com.example.data.model.AccountType.valueOf(obj.getString("type")),
          balance = obj.getDouble("balance"),
          currencyCode = obj.optString("currencyCode", "USD"),
          colorHex = obj.optString("colorHex", "#1A73E8")
        )
      )
    }

    // Recurring
    val alerts = mutableListOf<RecurringAlertEntity>()
    val alertArray = root.optJSONArray("recurringAlerts") ?: JSONArray()
    for (i in 0 until alertArray.length()) {
      val obj = alertArray.getJSONObject(i)
      alerts.add(
        RecurringAlertEntity(
          id = obj.optLong("id", 0L),
          transactionTitle = obj.getString("transactionTitle"),
          amount = obj.getDouble("amount"),
          type = com.example.data.model.TransactionType.valueOf(obj.getString("type")),
          categoryId = obj.getLong("categoryId"),
          interval = com.example.data.model.RecurringInterval.valueOf(obj.optString("interval", "MONTHLY")),
          nextDueDate = obj.getLong("nextDueDate"),
          isNotified = obj.optBoolean("isNotified", false),
          isActive = obj.optBoolean("isActive", true)
        )
      )
    }

    return BackupData(
      version = version,
      exportTimestamp = exportTimestamp,
      transactions = transactions,
      categories = categories,
      budgets = budgets,
      accounts = accounts,
      recurringAlerts = alerts
    )
  }
}
