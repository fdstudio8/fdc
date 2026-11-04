package com.fahad.devicecontroller.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.enrollment.EnrollmentWorkflowManager
import com.fahad.devicecontroller.ui.components.AppleCard
import com.fahad.devicecontroller.ui.components.AppleSegmentedControl
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Official Android Enterprise Enrollment Guide.
 * Step-by-step guidance for ADB provisioning and Out-of-Box QR setup.
 */
@Composable
fun EnrollmentGuideScreen(
    workflowManager: EnrollmentWorkflowManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val context = LocalContext.current

    var selectedMethodIndex by remember { mutableStateOf(0) }
    val methods = listOf("ADB (PC Setup)", "QR Code (Out-of-Box)")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Back Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.blue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Device Enrollment",
                    style = AppleTypography.Title1.copy(color = colors.primaryLabel)
                )
            }
        }

        // Method Selector
        item {
            AppleSegmentedControl(
                items = methods,
                selectedIndex = selectedMethodIndex,
                onItemSelected = { selectedMethodIndex = it }
            )
        }

        if (selectedMethodIndex == 0) {
            // Method 1: ADB Command Provisioning
            item {
                AppleCard {
                    Text(
                        text = "METHOD 1: ADB COMMAND PROVISIONING",
                        style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Provision Device Owner privileges via USB Debugging from a computer without factory resetting your phone.",
                        style = AppleTypography.Body.copy(color = colors.primaryLabel)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Mandatory Pre-condition:",
                        style = AppleTypography.Headline.copy(color = colors.orange)
                    )
                    Text(
                        text = "Navigate to Android Settings -> Accounts and remove all Google, Samsung, or Xiaomi accounts. You can sign back into them after provisioning is complete.",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Run this command in terminal:",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.tertiaryBackground)
                            .border(0.5.dp, colors.glassBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = workflowManager.adbCommand,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = colors.primaryLabel
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ADB Command", workflowManager.adbCommand)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.blue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy ADB Command", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // Method 2: QR Code Out-of-Box
            item {
                AppleCard {
                    Text(
                        text = "METHOD 2: OUT-OF-THE-BOX QR CODE",
                        style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Provision newly unboxed or factory-reset devices directly from the initial Android welcome screen.",
                        style = AppleTypography.Body.copy(color = colors.primaryLabel)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "1. Tap the Welcome Screen 6 times rapidly.\n2. The camera will launch to scan an enterprise QR code.\n3. Scan the generated Android Enterprise bundle below.",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel, lineHeight = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val qrPayload = remember {
                        workflowManager.generateQrProvisioningPayload(
                            apkDownloadUrl = "https://github.com/fahad/fdc/releases/download/v2.0.0/fdc.apk",
                            apkSha256Checksum = "4a58b29c017d84f..."
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.tertiaryBackground)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = qrPayload,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = colors.primaryLabel
                        )
                    }
                }
            }
        }

        // Integrity Assurance Notice
        item {
            AppleCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = colors.mint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Android Enterprise Standards",
                        style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Android Enterprise enforces strict platform security. By design, no application can silently declare itself a Device Owner without user authorization via ADB or initial out-of-box provisioning. This ensures your device is never compromised by unauthorized software.",
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
        }
    }
}

