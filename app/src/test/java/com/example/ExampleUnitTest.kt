package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAes256GcmCryptoAlgorithm() {
    val samplePayload = "{\"test\": \"myMoneymanager Financial Record\", \"amount\": 124.50}"
    val plainBytes = samplePayload.toByteArray(StandardCharsets.UTF_8)
    val password = "StrongPassword2026!".toCharArray()

    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    val iv = ByteArray(12)
    random.nextBytes(iv)

    // Key derivation via PBKDF2WithHmacSHA256
    val spec = PBEKeySpec(password, salt, 65536, 256)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val secretKeyBytes = factory.generateSecret(spec).encoded
    val secretKey = SecretKeySpec(secretKeyBytes, "AES")

    // Encrypt
    val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
    val gcmSpec = GCMParameterSpec(128, iv)
    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
    val cipherBytes = encryptCipher.doFinal(plainBytes)

    assertNotNull(cipherBytes)
    assertTrue(cipherBytes.isNotEmpty())

    // Decrypt
    val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
    decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
    val decryptedBytes = decryptCipher.doFinal(cipherBytes)
    val recoveredString = String(decryptedBytes, StandardCharsets.UTF_8)

    assertEquals(samplePayload, recoveredString)
  }
}
