package com.fahad.devicecontroller.ui.screens

import android.os.UserManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.ManagedApp
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.core.model.PolicyEnforcementStatus
import com.fahad.devicecontroller.dpc.DevicePolicyBridge
import com.fahad.devicecontroller.feature.inventory.AppInventoryManager
import com.fahad.devicecontroller.ui.components.AppleCard
import com.fahad.devicecontroller.ui.components.AppleStatusBadge
import com.fahad.devicecontroller.ui.components.AppleSwitch
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Enterprise Policy Center & Application Restriction Manager.
 */
@Composable
fun PolicyManagerScreen(
    operatingMode: OperatingMode,
    policyBridge: DevicePolicyBridge,
    inventoryManager: AppInventoryManager,
    onRequireAdminAuth: (() -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val installedApps by inventoryManager.installedApps.collectAsState()
    val isLoading by inventoryManager.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // System-level restriction states (read verified state from hardware)
    var isFactoryResetBlocked by remember {
        mutableStateOf(policyBridge.hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET))
    }
    var isSafeBootBlocked by remember {
        mutableStateOf(policyBridge.hasUserRestriction(UserManager.DISALLOW_SAFE_BOOT))
    }
    var isUnknownSourcesBlocked by remember {
        mutableStateOf(policyBridge.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES))
    }
    var isTamperResistant by remember {
        mutableStateOf(policyBridge.isTamperResistanceActive())
    }

    LaunchedEffect(Unit) {
        inventoryManager.refreshApps()
    }

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
                    text = "Policy Center",
                    style = AppleTypography.Title1.copy(color = colors.primaryLabel)
                )
            }
        }

        // Mode Notice Banner if not in Device Owner mode
        if (operatingMode == OperatingMode.STANDARD) {
            item {
                AppleCard(backgroundColor = colors.orange.copy(alpha = 0.1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.orange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Standard Mode (Unmanaged)",
                            style = AppleTypography.Headline.copy(color = colors.orange)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "System policy controls require Device Owner enrollment. Controls below are disabled to truthfully reflect device enforcement status.",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )
                }
            }
        }

        // Anti-Bypass & Tamper Resistance Card
        item {
            AppleCard(
                backgroundColor = if (isTamperResistant) colors.mint.copy(alpha = 0.08f) else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isTamperResistant) colors.mint else colors.purple,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Anti-Bypass Lock (Tamper-Proof)",
                                style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Locks uninstallation, blocks Settings App Info (force-stop & clear data), prevents clock manipulation, and blocks factory reset.",
                            style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                        )
                    }

                    AppleSwitch(
                        checked = isTamperResistant,
                        enabled = operatingMode != OperatingMode.STANDARD,
                        onCheckedChange = { enable ->
                            onRequireAdminAuth {
                                val success = policyBridge.enforceTamperResistance(enable)
                                if (success) {
                                    isTamperResistant = enable
                                    isFactoryResetBlocked = enable
                                    isSafeBootBlocked = enable
                                }
                            }
                        }
                    )
                }
            }
        }

        // System Safeguards Section
        item {
            AppleCard {
                Text(
                    text = "SYSTEM SAFEGUARDS",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))

                PolicyToggleRow(
                    title = "Block Factory Reset",
                    description = "Prevents wiping device via Settings -> System",
                    isChecked = isFactoryResetBlocked,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { checked ->
                        onRequireAdminAuth {
                            val success = policyBridge.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, checked)
                            if (success) isFactoryResetBlocked = checked
                        }
                    }
                )

                PolicyToggleRow(
                    title = "Block Safe Boot",
                    description = "Restricts rebooting device into Safe Mode",
                    isChecked = isSafeBootBlocked,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { checked ->
                        onRequireAdminAuth {
                            val success = policyBridge.setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, checked)
                            if (success) isSafeBootBlocked = checked
                        }
                    }
                )

                PolicyToggleRow(
                    title = "Block Unknown Sources",
                    description = "Prevents sideloading APKs from browser or files",
                    isChecked = isUnknownSourcesBlocked,
                    isEnabled = operatingMode != OperatingMode.STANDARD,
                    onCheckedChange = { checked ->
                        onRequireAdminAuth {
                            val success = policyBridge.setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, checked)
                            if (success) isUnknownSourcesBlocked = checked
                        }
                    }
                )
            }
        }

        // App Management Search & Header
        item {
            AppleCard {
                Text(
                    text = "APPLICATION INVENTORY & RESTRICTIONS",
                    style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed packages...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = colors.secondaryLabel)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.blue,
                        unfocusedBorderColor = colors.glassBorder
                    ),
                    singleLine = true
                )
            }
        }

        // App List
        val filteredApps = installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.blue)
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                AppRestrictionCard(
                    app = app,
                    isDeviceOwner = operatingMode != OperatingMode.STANDARD,
                    onToggleHide = {
                        onRequireAdminAuth {
                            inventoryManager.toggleAppHidden(app.packageName, !app.isHidden)
                        }
                    },
                    onToggleSuspend = {
                        onRequireAdminAuth {
                            inventoryManager.toggleAppSuspended(app.packageName, !app.isSuspended)
                        }
                    },
                    onToggleUninstallBlock = {
                        onRequireAdminAuth {
                            inventoryManager.toggleUninstallBlocked(app.packageName, !app.isUninstallBlocked)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PolicyToggleRow(
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
                style = AppleTypography.Headline.copy(color = colors.primaryLabel)
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

@Composable
private fun AppRestrictionCard(
    app: ManagedApp,
    isDeviceOwner: Boolean,
    onToggleHide: () -> Unit,
    onToggleSuspend: () -> Unit,
    onToggleUninstallBlock: () -> Unit
) {
    val colors = LocalAppleColors.current

    AppleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                )
                Text(
                    text = app.packageName,
                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                )
            }
            if (app.isSystemApp) {
                AppleStatusBadge(
                    status = PolicyEnforcementStatus.ENFORCED_ACTIVE,
                    customLabel = "System"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Restriction Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hide App Button
            FilterChip(
                selected = app.isHidden,
                onClick = onToggleHide,
                enabled = isDeviceOwner,
                label = { Text(if (app.isHidden) "Hidden" else "Hide") },
                leadingIcon = {
                    Icon(
                        imageVector = if (app.isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.purple.copy(alpha = 0.2f),
                    selectedLabelColor = colors.purple
                )
            )

            // Suspend App Button
            FilterChip(
                selected = app.isSuspended,
                onClick = onToggleSuspend,
                enabled = isDeviceOwner,
                label = { Text(if (app.isSuspended) "Suspended" else "Suspend") },
                leadingIcon = {
                    Icon(
                        imageVector = if (app.isSuspended) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.orange.copy(alpha = 0.2f),
                    selectedLabelColor = colors.orange
                )
            )

            // Block Uninstall Button
            FilterChip(
                selected = app.isUninstallBlocked,
                onClick = onToggleUninstallBlock,
                enabled = isDeviceOwner,
                label = { Text(if (app.isUninstallBlocked) "Locked" else "Lock") },
                leadingIcon = {
                    Icon(
                        imageVector = if (app.isUninstallBlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.mint.copy(alpha = 0.2f),
                    selectedLabelColor = colors.mint
                )
            )
        }
    }
}

