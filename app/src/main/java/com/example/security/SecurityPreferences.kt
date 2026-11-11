package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SecurityPreferences(
  private val prefs: SharedPreferences
) {
  constructor(context: Context) : this(
    context.getSharedPreferences("m3note_security_prefs", Context.MODE_PRIVATE)
  )

  companion object {
    private const val KEY_SETUP_DONE = "initial_setup_done"
    private const val KEY_SECURITY_ENABLED = "security_enabled"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_PIN_ALGORITHM = "pin_algorithm"
    private const val KEY_FAILED_ATTEMPTS = "pin_failed_attempts"
    private const val KEY_LOCKOUT_UNTIL = "pin_lockout_until_ms"
    private const val KEY_BASE_CURRENCY = "base_currency"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    private const val ALGORITHM_PBKDF2 = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 600_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
  }

  fun isSetupDone(): Boolean = prefs.getBoolean(KEY_SETUP_DONE, false)

  fun setSetupDone(done: Boolean) {
    prefs.edit().putBoolean(KEY_SETUP_DONE, done).apply()
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

  fun hasPin(): Boolean {
    val hash = prefs.getString(KEY_PIN_HASH, null)
    return !hash.isNullOrBlank()
  }

  fun isLockedOut(): Boolean {
    val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
    return lockoutUntil > 0L && System.currentTimeMillis() < lockoutUntil
  }

  fun getRemainingLockoutSeconds(): Long {
    val diff = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L) - System.currentTimeMillis()
    return if (diff > 0) (diff + 999) / 1000 else 0L
  }

  fun getFailedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

  fun resetLockout() {
    onAuthSuccess()
  }

  fun setPin(pin: String) {
    val salt = generateSalt()
    val hash = hashPinPbkdf2(pin, salt)
    prefs.edit()
      .putString(KEY_PIN_SALT, bytesToHex(salt))
      .putString(KEY_PIN_HASH, bytesToHex(hash))
      .putString(KEY_PIN_ALGORITHM, ALGORITHM_PBKDF2)
      .putInt(KEY_FAILED_ATTEMPTS, 0)
      .putLong(KEY_LOCKOUT_UNTIL, 0L)
      .apply()
  }

  fun verifyPin(pin: String): Boolean {
    if (isLockedOut()) {
      return false
    }

    val savedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false
    val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
    val algorithm = prefs.getString(KEY_PIN_ALGORITHM, null)

    if (algorithm == ALGORITHM_PBKDF2) {
      val saltBytes = hexToBytes(savedSalt)
      val expectedHash = hexToBytes(savedHash)
      val computedHash = hashPinPbkdf2(pin, saltBytes)
      val matched = MessageDigest.isEqual(computedHash, expectedHash)
      if (matched) {
        onAuthSuccess()
        return true
      } else {
        onAuthFailure()
        return false
      }
    } else {
      val expectedHash = hexToBytes(savedHash)
      val computedHash = hashPinLegacy(pin, savedSalt)
      val matched = MessageDigest.isEqual(computedHash, expectedHash)
      if (matched) {
        setPin(pin)
        return true
      } else {
        onAuthFailure()
        return false
      }
    }
  }

  private fun onAuthSuccess() {
    prefs.edit()
      .putInt(KEY_FAILED_ATTEMPTS, 0)
      .putLong(KEY_LOCKOUT_UNTIL, 0L)
      .apply()
  }

  private fun onAuthFailure() {
    val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
    var lockoutUntil = 0L
    if (attempts >= 3) {
      val exponent = (attempts - 3).coerceAtMost(14)
      val lockoutSeconds = 5L * (1L shl exponent)
      lockoutUntil = System.currentTimeMillis() + (lockoutSeconds * 1000L)
    }
    prefs.edit()
      .putInt(KEY_FAILED_ATTEMPTS, attempts)
      .putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
      .apply()
  }

  private fun generateSalt(): ByteArray {
    val random = SecureRandom()
    val saltBytes = ByteArray(SALT_LENGTH_BYTES)
    random.nextBytes(saltBytes)
    return saltBytes
  }

  private fun hashPinPbkdf2(pin: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
    val factory = SecretKeyFactory.getInstance(ALGORITHM_PBKDF2)
    return factory.generateSecret(spec).encoded
  }

  private fun hashPinLegacy(pin: String, salt: String): ByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(salt.toByteArray())
    return md.digest(pin.toByteArray())
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

  private fun hexToBytes(hex: String): ByteArray {
    if (hex.length % 2 != 0) return ByteArray(0)
    val len = hex.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
      val high = Character.digit(hex[i], 16)
      val low = Character.digit(hex[i + 1], 16)
      if (high == -1 || low == -1) return ByteArray(0)
      data[i / 2] = ((high shl 4) or low).toByte()
      i += 2
    }
    return data
  }
}
