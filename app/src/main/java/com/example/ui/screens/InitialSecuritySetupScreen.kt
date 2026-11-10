package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InitialSecuritySetupScreen(
  onSetupComplete: (pin: String, enableBiometric: Boolean) -> Unit,
  onSkipSetup: () -> Unit,
  isBiometricAvailable: Boolean = true,
  modifier: Modifier = Modifier
) {
  var step by remember { mutableStateOf(1) } // 1 = Enter PIN, 2 = Confirm PIN
  var firstPin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var enableFingerprint by remember { mutableStateOf(isBiometricAvailable) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val currentPin = if (step == 1) firstPin else confirmPin

  fun handleDigitInput(digit: String) {
    errorMessage = null
    if (step == 1) {
      if (firstPin.length < 6) {
        val next = firstPin + digit
        firstPin = next
        if (next.length == 6) {
          step = 2
        }
      }
    } else {
      if (confirmPin.length < 6) {
        val next = confirmPin + digit
        confirmPin = next
        if (next.length == 6) {
          if (next == firstPin) {
            onSetupComplete(next, enableFingerprint)
          } else {
            errorMessage = "PINs do not match. Please re-enter."
            confirmPin = ""
            step = 1
            firstPin = ""
          }
        }
      }
    }
  }

  fun handleBackspace() {
    errorMessage = null
    if (step == 1) {
      if (firstPin.isNotEmpty()) {
        firstPin = firstPin.dropLast(1)
      }
    } else {
      if (confirmPin.isNotEmpty()) {
        confirmPin = confirmPin.dropLast(1)
      } else {
        step = 1
      }
    }
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Brand Header
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Welcome to M3Note",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (step == 1) "Set a 6-Digit Master PIN" else "Confirm Your 6-Digit PIN",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = if (step == 1) {
            "Protect your personal income, expenses, and financial vaults."
          } else {
            "Repeat the same PIN to confirm security."
          },
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6 PIN Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(6) { index ->
            val filled = index < currentPin.length
            Box(
              modifier = Modifier
                .size(18.dp)
                .background(
                  color = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = CircleShape
                )
                .border(
                  width = 2.dp,
                  color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                  shape = CircleShape
                )
            )
          }
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fingerprint / Biometric Toggle
        if (isBiometricAvailable) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Fingerprint Unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "Fast biometric access on app open",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Switch(
                checked = enableFingerprint,
                onCheckedChange = { enableFingerprint = it },
                modifier = Modifier.testTag("setup_fingerprint_switch")
              )
            }
          }
        }
      }

      // Numeric Keypad
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("BACK", "0", "CLEAR")
        )

        for (row in rows) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            for (key in row) {
              when (key) {
                "BACK" -> {
                  Box(
                    modifier = Modifier
                      .size(68.dp)
                      .clip(CircleShape)
                      .clickable { handleBackspace() }
                      .testTag("setup_key_backspace"),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Backspace,
                      contentDescription = "Backspace",
                      tint = MaterialTheme.colorScheme.onBackground,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
                "CLEAR" -> {
                  Box(
                    modifier = Modifier
                      .size(68.dp)
                      .clip(CircleShape)
                      .clickable {
                        firstPin = ""
                        confirmPin = ""
                        step = 1
                        errorMessage = null
                      }
                      .testTag("setup_key_clear"),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "Reset",
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                else -> {
                  Box(
                    modifier = Modifier
                      .size(68.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .clickable { handleDigitInput(key) }
                      .testTag("setup_key_$key"),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = key,
                      style = MaterialTheme.typography.headlineMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onBackground
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
          onClick = onSkipSetup,
          modifier = Modifier.testTag("skip_security_setup_button")
        ) {
          Text(
            text = "Skip for Now (Enable Later in Vault)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
