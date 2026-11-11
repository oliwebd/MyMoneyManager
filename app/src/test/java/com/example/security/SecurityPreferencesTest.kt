package com.example.security

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

class SecurityPreferencesTest {

  private lateinit var fakePrefs: FakeSharedPreferences
  private lateinit var securityPrefs: SecurityPreferences

  @Before
  fun setUp() {
    fakePrefs = FakeSharedPreferences()
    securityPrefs = SecurityPreferences(fakePrefs)
  }

  @Test
  fun testSetAndVerifyPinPbkdf2() {
    assertFalse(securityPrefs.hasPin())
    securityPrefs.setPin("4826")
    assertTrue(securityPrefs.hasPin())

    // Correct PIN should succeed
    assertTrue(securityPrefs.verifyPin("4826"))
    assertEquals(0, securityPrefs.getFailedAttempts())
    assertFalse(securityPrefs.isLockedOut())

    // Incorrect PIN should fail and increment attempt count
    assertFalse(securityPrefs.verifyPin("0000"))
    assertEquals(1, securityPrefs.getFailedAttempts())
  }

  @Test
  fun testFailedAttemptsAndExponentialLockout() {
    securityPrefs.setPin("1234")

    assertFalse(securityPrefs.verifyPin("9999"))
    assertEquals(1, securityPrefs.getFailedAttempts())
    assertFalse(securityPrefs.isLockedOut())

    assertFalse(securityPrefs.verifyPin("9998"))
    assertEquals(2, securityPrefs.getFailedAttempts())
    assertFalse(securityPrefs.isLockedOut())

    // Third failure triggers lockout (5 seconds)
    assertFalse(securityPrefs.verifyPin("9997"))
    assertEquals(3, securityPrefs.getFailedAttempts())
    assertTrue(securityPrefs.isLockedOut())
    assertTrue(securityPrefs.getRemainingLockoutSeconds() > 0)

    // While locked out, even the correct PIN must be rejected
    assertFalse(securityPrefs.verifyPin("1234"))

    // Resetting lockout allows correct PIN again
    securityPrefs.resetLockout()
    assertFalse(securityPrefs.isLockedOut())
    assertTrue(securityPrefs.verifyPin("1234"))
  }

  @Test
  fun testLegacySha256Migration() {
    val legacySalt = "0123456789ABCDEF0123456789ABCDEF"
    val pin = "654321"

    val md = MessageDigest.getInstance("SHA-256")
    md.update(legacySalt.toByteArray())
    val digest = md.digest(pin.toByteArray())

    val hexChars = CharArray(digest.size * 2)
    val hexArray = "0123456789ABCDEF".toCharArray()
    for (j in digest.indices) {
      val v = digest[j].toInt() and 0xFF
      hexChars[j * 2] = hexArray[v ushr 4]
      hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    val legacyHashHex = String(hexChars)

    // Save legacy state without pin_algorithm
    fakePrefs.edit()
      .putString("pin_salt", legacySalt)
      .putString("pin_hash", legacyHashHex)
      .apply()

    assertTrue(securityPrefs.hasPin())

    // Wrong PIN should fail
    assertFalse(securityPrefs.verifyPin("000000"))

    // Correct PIN should succeed and migrate to PBKDF2WithHmacSHA256
    assertTrue(securityPrefs.verifyPin(pin))

    val updatedAlgo = fakePrefs.getString("pin_algorithm", null)
    assertEquals("PBKDF2WithHmacSHA256", updatedAlgo)

    // Verify subsequent login uses new PBKDF2 credentials
    assertTrue(securityPrefs.verifyPin(pin))
  }

  private class FakeSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = HashMap(map)
    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
      @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String> ?: defValues)
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor(map)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private class FakeEditor(private val target: MutableMap<String, Any?>) : SharedPreferences.Editor {
      private val pending = mutableMapOf<String, Any?>()
      private val removed = mutableSetOf<String>()
      private var clearFlag = false

      override fun putString(key: String?, value: String?): SharedPreferences.Editor {
        if (key != null) pending[key] = value
        return this
      }
      override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
        if (key != null) pending[key] = values
        return this
      }
      override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
        if (key != null) pending[key] = value
        return this
      }
      override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
        if (key != null) pending[key] = value
        return this
      }
      override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
        if (key != null) pending[key] = value
        return this
      }
      override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
        if (key != null) pending[key] = value
        return this
      }
      override fun remove(key: String?): SharedPreferences.Editor {
        if (key != null) removed.add(key)
        return this
      }
      override fun clear(): SharedPreferences.Editor {
        clearFlag = true
        return this
      }
      override fun commit(): Boolean {
        apply()
        return true
      }
      override fun apply() {
        if (clearFlag) target.clear()
        removed.forEach { target.remove(it) }
        target.putAll(pending)
      }
    }
  }
}
