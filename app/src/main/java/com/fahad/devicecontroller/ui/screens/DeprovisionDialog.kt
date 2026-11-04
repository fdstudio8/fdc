package com.fahad.devicecontroller.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.fahad.devicecontroller.dpc.DevicePolicyBridge
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Clean & Authorized Deprovisioning Confirmation Dialog.
 * Reverts all enforced policies and drops Device Owner privilege cleanly.
 */
@Composable
fun DeprovisionDialog(
    policyBridge: DevicePolicyBridge,
    onDeprovisionComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppleColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = colors.red
            )
        },
        title = {
            Text(
                text = "Deprovision Device Controller?",
                style = AppleTypography.Title2.copy(color = colors.primaryLabel)
            )
        },
        text = {
            Text(
                text = "This action will:\n• Restore all hidden and suspended applications.\n• Clear all factory reset & safe boot restrictions.\n• Revoke Android Enterprise Device Owner status.\n\nThe device will return to Standard Mode.",
                style = AppleTypography.Body.copy(color = colors.secondaryLabel)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    policyBridge.clearDeviceOwner()
                    onDeprovisionComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.red)
            ) {
                Text("Confirm Deprovision", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.primaryLabel)
            }
        }
    )
}

