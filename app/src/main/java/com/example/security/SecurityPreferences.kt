package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("pocketsense_security_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_SECURITY_ENABLED = "security_enabled"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_BASE_CURRENCY = "base_currency"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val DEFAULT_PIN = "123456" // Default 6-digit PIN for first-time convenience
  }

  init {
    // If not set yet, initialize default 6-digit PIN 123456 so user can test immediately
    if (getPinHash() == null) {
      setPin(DEFAULT_PIN)
    }
  }

  fun isSecurityEnabled(): Boolean = prefs.getBoolean(KEY_SECURITY_ENABLED, true)

  fun setSecurityEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_SECURITY_ENABLED, enabled).apply()
  }

  fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

  fun setBiometricEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
  }

  fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

  fun setNotificationsEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
  }

  fun getBaseCurrency(): String = prefs.getString(KEY_BASE_CURRENCY, "USD") ?: "USD"

  fun setBaseCurrency(currencyCode: String) {
    prefs.edit().putString(KEY_BASE_CURRENCY, currencyCode).apply()
  }

  fun setPin(pin: String) {
    val salt = generateSalt()
    val hash = hashPin(pin, salt)
    prefs.edit()
      .putString(KEY_PIN_SALT, salt)
      .putString(KEY_PIN_HASH, hash)
      .apply()
  }

  fun verifyPin(pin: String): Boolean {
    val savedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false
    val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
    val inputHash = hashPin(pin, savedSalt)
    return savedHash == inputHash
  }

  private fun getPinHash(): String? = prefs.getString(KEY_PIN_HASH, null)

  private fun generateSalt(): String {
    val random = SecureRandom()
    val saltBytes = ByteArray(16)
    random.nextBytes(saltBytes)
    return bytesToHex(saltBytes)
  }

  private fun hashPin(pin: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(salt.toByteArray())
    val digest = md.digest(pin.toByteArray())
    return bytesToHex(digest)
  }

  private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    val hexArray = "0123456789ABCDEF".toCharArray()
    for (j in bytes.indices) {
      val v = bytes[j].toInt() and 0xFF
      hexChars[j * 2] = hexArray[v ushr 4]
      hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
  }
}
