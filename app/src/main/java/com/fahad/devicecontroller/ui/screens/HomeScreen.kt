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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.DeviceCapabilityReport
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.ui.components.AppleCard
import com.fahad.devicecontroller.ui.components.AppleModeBadge
import com.fahad.devicecontroller.ui.components.AppleStatusBadge
import com.fahad.devicecontroller.core.model.PolicyEnforcementStatus
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * FDC Main Dashboard.
 * Displays high-level governance status, hardware capability overview, and quick navigations.
 */
@Composable
fun HomeScreen(
    report: DeviceCapabilityReport,
    onNavigateToPolicyManager: () -> Unit,
    onNavigateToStudyMode: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToEnrollment: () -> Unit,
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
        // Operational Status Card
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device Status",
                            style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.operatingMode.title,
                            style = AppleTypography.Title2.copy(color = colors.primaryLabel)
                        )
                    }
                    AppleStatusBadge(
                        status = if (report.isDeviceOwner)
                            PolicyEnforcementStatus.ENFORCED_ACTIVE
                        else
                            PolicyEnforcementStatus.REQUIRES_DEVICE_OWNER
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = report.operatingMode.description,
                    style = AppleTypography.Subhead.copy(color = colors.secondaryLabel)
                )

                if (!report.isDeviceOwner) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToEnrollment,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.blue),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Enrollment Instructions", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Hardware & Platform Snapshot
        item {
            AppleCard(onClick = onNavigateToCompatibility) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = colors.blue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${report.brand.displayName} ${report.model}",
                            style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                        )
                        Text(
                            text = "Android ${report.androidVersion} (API ${report.apiLevel}) • ${report.brand.skinName}",
                            style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = colors.tertiaryLabel
                    )
                }
            }
        }

        // Quick Navigation Grid / Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Permanent Controller Card
                AppleCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStudyMode
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = colors.purple,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Controller",
                        style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                    )
                    Text(
                        text = "Permanent 24/7 Lock",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )
                }

                // Policy Center Card
                AppleCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPolicyManager
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = colors.mint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Policies",
                        style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                    )
                    Text(
                        text = "Apps & Restrictions",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )
                }
            }
        }

        // OEM Extension Diagnostics Card
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = colors.purple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Manufacturer Extension",
                        style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = report.oemNotes,
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
        }
    }
}

