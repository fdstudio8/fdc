package com.fahad.devicecontroller.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fahad.devicecontroller.core.security.AdminAuthManager
import com.fahad.devicecontroller.ui.components.ApplePinKeypad
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors
import kotlinx.coroutines.delay

enum class AuthMode {
    PIN,
    MASTER_KEY,
    RESET_PIN
}

/**
 * Modal Administrator Authentication Dialog with Apple fluid motion,
 * Anti-Bypass Lockout enforcement, and Cryptographic Master Key Override.
 */
@Composable
fun AdminAuthDialog(
    authManager: AdminAuthManager,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppleColors.current

    var authMode by remember { mutableStateOf(AuthMode.PIN) }
    var pinBuffer by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }

    var masterKeyInput by remember { mutableStateOf("") }
    var isMasterKeyError by remember { mutableStateOf(false) }

    var newPinInput by remember { mutableStateOf("") }

    var lockoutSeconds by remember { mutableStateOf(authManager.remainingLockoutSeconds()) }

    // Lockout countdown timer
    LaunchedEffect(lockoutSeconds) {
        while (lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds = authManager.remainingLockoutSeconds()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.secondaryBackground)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (authMode == AuthMode.PIN) Icons.Default.Shield else Icons.Default.Key,
                            contentDescription = null,
                            tint = if (authMode == AuthMode.PIN) colors.blue else colors.purple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (authMode) {
                                AuthMode.PIN -> "Admin Access"
                                AuthMode.MASTER_KEY -> "Master Key Override"
                                AuthMode.RESET_PIN -> "Set New PIN"
                            },
                            style = AppleTypography.Title2.copy(color = colors.primaryLabel)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle / Anti-Bypass Status
                Text(
                    text = when {
                        authMode == AuthMode.MASTER_KEY ->
                            "Enter the emergency Master Key to override lockouts & reset PIN"
                        authMode == AuthMode.RESET_PIN ->
                            "Enter a new 4-digit Administrator PIN"
                        lockoutSeconds > 0 ->
                            "Anti-Tamper Lockout Active: Wait ${lockoutSeconds}s or use Master Key"
                        else ->
                            "Enter PIN to authenticate. Protected against bypass."
                    },
                    style = AppleTypography.Footnote.copy(
                        color = if (lockoutSeconds > 0 && authMode == AuthMode.PIN) colors.red else colors.secondaryLabel
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode 1: 4-Digit PIN Keypad
                if (authMode == AuthMode.PIN) {
                    if (lockoutSeconds == 0L) {
                        ApplePinKeypad(
                            pinLength = 4,
                            currentPin = pinBuffer,
                            isError = isPinError,
                            onPinDigitEntered = { digit ->
                                if (pinBuffer.length < 4) {
                                    isPinError = false
                                    val newPin = pinBuffer + digit
                                    pinBuffer = newPin

                                    if (newPin.length == 4) {
                                        if (authManager.verifyPin(newPin)) {
                                            onSuccess()
                                        } else {
                                            isPinError = true
                                            pinBuffer = ""
                                            lockoutSeconds = authManager.remainingLockoutSeconds()
                                        }
                                    }
                                }
                            },
                            onPinDigitDeleted = {
                                if (pinBuffer.isNotEmpty()) {
                                    isPinError = false
                                    pinBuffer = pinBuffer.dropLast(1)
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Locked Out (${lockoutSeconds}s)",
                                    style = AppleTypography.Title2.copy(color = colors.red)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Anti-tamper rate limit triggered",
                                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Master Key Fallback Button
                    TextButton(
                        onClick = {
                            authMode = AuthMode.MASTER_KEY
                            masterKeyInput = ""
                            isMasterKeyError = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = colors.purple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Emergency Master Key",
                            color = colors.purple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Mode 2: Emergency Master Key Entry
                if (authMode == AuthMode.MASTER_KEY) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = masterKeyInput,
                            onValueChange = {
                                masterKeyInput = it
                                isMasterKeyError = false
                            },
                            placeholder = { Text("FDC-MASTER-XXXX-XXXX") },
                            isError = isMasterKeyError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.purple,
                                unfocusedBorderColor = colors.glassBorder
                            ),
                            singleLine = true
                        )

                        if (isMasterKeyError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invalid Master Key. Please verify.",
                                color = colors.red,
                                style = AppleTypography.Caption
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.tertiaryBackground)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Default Master Key: FDC-MASTER-9988-7766",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.secondaryLabel
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    authMode = AuthMode.PIN
                                    pinBuffer = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back to PIN", color = colors.primaryLabel)
                            }

                            Button(
                                onClick = {
                                    if (authManager.verifyMasterKey(masterKeyInput)) {
                                        authMode = AuthMode.RESET_PIN
                                    } else {
                                        isMasterKeyError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Verify & Unlock", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Mode 3: Reset PIN using Master Key
                if (authMode == AuthMode.RESET_PIN) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Master Key Verified! Choose an action:",
                            style = AppleTypography.Headline.copy(color = colors.mint)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                            placeholder = { Text("Enter 4-digit PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (newPinInput.length == 4) {
                                    authManager.resetPinWithMasterKey(masterKeyInput, newPinInput)
                                    onSuccess()
                                }
                            },
                            enabled = newPinInput.length == 4,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.mint),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save New PIN & Authenticate", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { onSuccess() }
                        ) {
                            Text("Authenticate Directly", color = colors.blue)
                        }
                    }
                }
            }
        }
    }
}
