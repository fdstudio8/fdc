package com.fahad.devicecontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.core.model.PolicyEnforcementStatus
import com.fahad.devicecontroller.core.policy.PersistentPolicyManager
import com.fahad.devicecontroller.core.remote.GitHubRemoteConfigManager
import com.fahad.devicecontroller.ui.components.AppleCard
import com.fahad.devicecontroller.ui.components.AppleStatusBadge
import com.fahad.devicecontroller.ui.components.AppleSwitch
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors
import kotlinx.coroutines.launch

/**
 * Permanent Device Controller & Persistent Lockdown Engine.
 * Replaces temporary study timers with permanent, 24/7 governance that stays
 * enforced FOREVER ("Hamesha ke liye") across device reboots and battery drain.
 * Integrated with MAMP PRO Server & GitHub JSON Dynamic Configuration.
 */
@Composable
fun PermanentControllerScreen(
    operatingMode: OperatingMode,
    persistentPolicyManager: PersistentPolicyManager,
    remoteConfigManager: GitHubRemoteConfigManager,
    onStartPermanentKiosk: () -> Unit,
    onStopPermanentKiosk: () -> Unit,
    onNavigateToPolicyManager: () -> Unit,
    onRequireAdminAuth: (() -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val coroutineScope = rememberCoroutineScope()
    val config by persistentPolicyManager.config.collectAsState()
    val remoteState by remoteConfigManager.state.collectAsState()

    var showEditUrlDialog by remember { mutableStateOf(false) }
    var editUrlInput by remember { mutableStateOf(remoteState.githubJsonUrl) }

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
                    text = "Permanent Controller",
                    style = AppleTypography.Title1.copy(color = colors.primaryLabel)
                )
            }
        }

        // Permanent Status Hero Card
        item {
            AppleCard(
                backgroundColor = if (config.isPermanentLockdownEnabled)
                    colors.mint.copy(alpha = 0.10f)
                else
                    null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = if (config.isPermanentLockdownEnabled) colors.mint else colors.purple,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Always-On Governance",
                                style = AppleTypography.Title2.copy(color = colors.primaryLabel)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (config.isPermanentLockdownEnabled)
                                "ACTIVE 24/7 — Policies stay enforced forever across reboots"
                            else
                                "Ready — Toggle below to lock all configurations permanently",
                            style = AppleTypography.Footnote.copy(
                                color = if (config.isPermanentLockdownEnabled) colors.mint else colors.secondaryLabel,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    AppleStatusBadge(
                        status = if (config.isPermanentLockdownEnabled)
                            PolicyEnforcementStatus.ENFORCED_ACTIVE
                        else
                            PolicyEnforcementStatus.PENDING_ELEVATION,
                        customLabel = if (config.isPermanentLockdownEnabled) "Locked 24/7" else "Standby"
                    )
                }
            }
        }

        // Local MAMP PRO Server Sync Card (100% Offline / Local Network)
        item {
            AppleCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = colors.blue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local MAMP PRO Server",
                            style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                        )
                        Text(
                            text = "100% Offline / Local Network (No Cloud)",
                            style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                        )
                    }
                    AppleStatusBadge(
                        status = if (remoteState.lastSyncStatus.contains("Synced", ignoreCase = true))
                            PolicyEnforcementStatus.ENFORCED_ACTIVE
                        else
                            PolicyEnforcementStatus.PENDING_ELEVATION,
                        customLabel = "v${remoteState.configVersion}"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Mac Local Server Endpoint:",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Text(
                    text = remoteState.adminServerUrl,
                    style = AppleTypography.Subhead.copy(fontFamily = FontFamily.Monospace, color = colors.blue)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sync Status: ${remoteState.lastSyncStatus}",
                    style = AppleTypography.Caption.copy(
                        color = if (remoteState.lastSyncStatus.contains("Synced", ignoreCase = true)) colors.mint else colors.orange,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                remoteConfigManager.syncFromGitHub()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.blue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (remoteState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (remoteState.isSyncing) "Syncing..." else "Sync with Local Server", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            onRequireAdminAuth {
                                editUrlInput = remoteState.adminServerUrl
                                showEditUrlDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Edit IP", color = colors.primaryLabel)
                    }
                }
            }
        }

        // Master 24/7 Controls
        item {
            AppleCard {
                Text(
                    text = "PERMANENT SYSTEM LOCKS",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Master Permanent Mode Toggle
                PermanentToggleRow(
                    title = "Permanent Strict Lock (Always-On)",
                    description = "Locks all policy settings permanently. Never expires on a timer.",
                    isChecked = config.isPermanentLockdownEnabled,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { enable ->
                        onRequireAdminAuth {
                            persistentPolicyManager.setPermanentLockdown(enable)
                        }
                    }
                )

                HorizontalDivider(color = colors.glassBorder, modifier = Modifier.padding(vertical = 4.dp))

                // Permanent Kiosk Lock
                PermanentToggleRow(
                    title = "Permanent Kiosk Mode",
                    description = "Pins device to FDC permanently. Home and Status Bar locked forever.",
                    isChecked = config.isPermanentKioskEnabled,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { enable ->
                        onRequireAdminAuth {
                            persistentPolicyManager.setPermanentKiosk(enable)
                            if (enable) onStartPermanentKiosk() else onStopPermanentKiosk()
                        }
                    }
                )

                HorizontalDivider(color = colors.glassBorder, modifier = Modifier.padding(vertical = 4.dp))

                // Anti-Tamper Shield
                PermanentToggleRow(
                    title = "Permanent Anti-Tamper Shield",
                    description = "Permanently disables Force Stop, Clear Data & Uninstall in Settings.",
                    isChecked = config.isAntiTamperShieldEnabled,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { enable ->
                        onRequireAdminAuth {
                            persistentPolicyManager.setAntiTamperShield(enable)
                        }
                    }
                )

                HorizontalDivider(color = colors.glassBorder, modifier = Modifier.padding(vertical = 4.dp))

                // Permanent Factory Reset Block
                PermanentToggleRow(
                    title = "Permanent Factory Reset Block",
                    description = "Permanently disables Settings-based device erasure.",
                    isChecked = config.isFactoryResetBlocked,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { enable ->
                        onRequireAdminAuth {
                            persistentPolicyManager.setFactoryResetBlocked(enable)
                        }
                    }
                )
            }
        }

        // Permanent Application Restrictions Card
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PERMANENT APP RESTRICTIONS",
                            style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${config.suspendedPackages.size} Suspended • ${config.hiddenPackages.size} Hidden",
                            style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                        )
                    }

                    Button(
                        onClick = onNavigateToPolicyManager,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.blue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Manage Apps", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apps selected in Policy Center stay permanently suspended or hidden forever across phone reboots until you change them with your Admin PIN or Master Key.",
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
        }

        // Reboot Resilience Guarantee Card
        item {
            AppleCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = colors.mint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Reboot Resilience: Guaranteed",
                        style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FDC registers a native Android BOOT_COMPLETED receiver (FdcBootReceiver). Even if the phone battery runs out or the phone is rebooted, all restrictions are instantly restored before the user can unlock the screen.",
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
        }
    }

    // Edit Local Server Endpoint Dialog
    if (showEditUrlDialog) {
        AlertDialog(
            onDismissRequest = { showEditUrlDialog = false },
            title = { Text("Configure Local Mac Server", style = AppleTypography.Title2) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your Mac MAMP PRO Server URL on the local Wi-Fi (e.g. http://192.168.31.105/fdc-admin):",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )
                    OutlinedTextField(
                        value = editUrlInput,
                        onValueChange = { editUrlInput = it },
                        placeholder = { Text("http://192.168.31.105/fdc-admin") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        remoteConfigManager.setGitHubJsonUrl(editUrlInput)
                        showEditUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.blue)
                ) {
                    Text("Save Endpoint", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUrlDialog = false }) {
                    Text("Cancel", color = colors.secondaryLabel)
                }
            }
        )
    }
}

@Composable
private fun PermanentToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalAppleColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
        AppleSwitch(
            checked = isChecked,
            enabled = isEnabled,
            onCheckedChange = onCheckedChange
        )
    }
}
