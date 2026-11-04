package com.fahad.devicecontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.DeviceCapabilityReport
import com.fahad.devicecontroller.core.model.PolicyEnforcementStatus
import com.fahad.devicecontroller.ui.components.AppleCard
import com.fahad.devicecontroller.ui.components.AppleStatusBadge
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Live Device Compatibility & Capability Audit Screen.
 * Demonstrates transparent status for every system policy and hardware extension.
 */
@Composable
fun CompatibilityReportScreen(
    report: DeviceCapabilityReport,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Navigation Back Header
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
                    text = "Compatibility Report",
                    style = AppleTypography.Title1.copy(color = colors.primaryLabel)
                )
            }
        }

        // Section: Hardware & OS Identification
        item {
            AppleCard {
                Text(
                    text = "PLATFORM SPECIFICATIONS",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))
                SpecRow(label = "Manufacturer", value = report.manufacturer)
                SpecRow(label = "Brand Architecture", value = "${report.brand.displayName} (${report.brand.skinName})")
                SpecRow(label = "Model", value = report.model)
                SpecRow(label = "Android Version", value = "${report.androidVersion} (API ${report.apiLevel})")
                SpecRow(label = "Security Patch", value = report.securityPatch)
                SpecRow(label = "Build Fingerprint", value = report.buildFingerprint.take(32) + "...", isMono = true)
            }
        }

        // Section: Android Enterprise Privileges
        item {
            AppleCard {
                Text(
                    text = "ENTERPRISE PRIVILEGE AUDIT",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CapabilityAuditRow(
                    title = "Device Owner Status",
                    description = "Supreme enterprise manager privilege",
                    isSupported = report.isDeviceOwner
                )
                CapabilityAuditRow(
                    title = "Profile Owner Status",
                    description = "Work Profile container manager",
                    isSupported = report.isProfileOwner
                )
                CapabilityAuditRow(
                    title = "Device Admin Active",
                    description = "Standard AOSP DeviceAdminReceiver registered",
                    isSupported = report.isAdminActive
                )
            }
        }

        // Section: Standard Management APIs
        item {
            AppleCard {
                Text(
                    text = "AOSP MANAGEMENT API ENFORCEMENT",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CapabilityAuditRow(
                    title = "Application Hiding",
                    description = "setApplicationHidden API (Android 5.0+)",
                    isSupported = report.isAppHidingSupported
                )
                CapabilityAuditRow(
                    title = "Application Suspension",
                    description = "setPackagesSuspended API (Android 7.0+)",
                    isSupported = report.isAppSuspensionSupported
                )
                CapabilityAuditRow(
                    title = "Lock Task (Kiosk) Mode",
                    description = "setLockTaskPackages API (Android 5.0+)",
                    isSupported = report.isLockTaskSupported
                )
                CapabilityAuditRow(
                    title = "Uninstall Block Protection",
                    description = "setUninstallBlocked API (Android 5.0+)",
                    isSupported = report.isUninstallBlockSupported
                )
                CapabilityAuditRow(
                    title = "Factory Reset Block",
                    description = "UserManager.DISALLOW_FACTORY_RESET",
                    isSupported = report.isFactoryResetBlockSupported
                )
            }
        }

        // Section: Manufacturer-Specific Extensions
        item {
            AppleCard {
                Text(
                    text = "MANUFACTURER EXTENSIONS",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CapabilityAuditRow(
                    title = "Samsung Knox Enterprise SDK",
                    description = if (report.hasKnoxSupport)
                        "Dynamic reflection active: Knox EDM classes accessible"
                    else
                        "Dormant: Device is not Samsung or lacks Knox Enterprise SDK",
                    isSupported = report.hasKnoxSupport
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = report.oemNotes,
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
        }

        // Honest Security Disclosures Card
        item {
            AppleCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.blue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security Boundary Disclosure",
                        style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FDC enforces all supported policies via official Android Enterprise DPC APIs. However, in accordance with Android security architecture, DPC status cannot prevent physical hardware disassembly, motherboard flashing via Qualcomm Emergency Download (EDL), fastboot bootloader unlocks, or hardware service center servicing.",
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, isMono: Boolean = false) {
    val colors = LocalAppleColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppleTypography.Subhead.copy(color = colors.secondaryLabel)
        )
        Text(
            text = value,
            style = AppleTypography.Subhead.copy(
                color = colors.primaryLabel,
                fontWeight = if (isMono) FontWeight.Normal else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun CapabilityAuditRow(
    title: String,
    description: String,
    isSupported: Boolean
) {
    val colors = LocalAppleColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppleTypography.Headline.copy(color = colors.primaryLabel, fontSize = 15.sp)
            )
            Text(
                text = description,
                style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
            )
        }
        AppleStatusBadge(
            status = if (isSupported)
                PolicyEnforcementStatus.ENFORCED_ACTIVE
            else
                PolicyEnforcementStatus.REQUIRES_DEVICE_OWNER,
            customLabel = if (isSupported) "Supported" else "Requires DO"
        )
    }
}

