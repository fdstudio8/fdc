package com.fahad.devicecontroller

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.fahad.devicecontroller.core.model.DeviceCapabilityReport
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.feature.study.StudyTimerManager
import com.fahad.devicecontroller.ui.components.AppleTopBar
import com.fahad.devicecontroller.ui.screens.*
import com.fahad.devicecontroller.ui.theme.AppleTheme
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

enum class AppScreen {
    HOME,
    POLICY,
    CONTROLLER,
    COMPATIBILITY,
    ENROLLMENT
}

class MainActivity : ComponentActivity() {

    private lateinit var app: FdcApplication
    private lateinit var studyTimerManager: StudyTimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        app = application as FdcApplication
        studyTimerManager = StudyTimerManager(lifecycleScope)

        val triggerAdminAuth = intent.getBooleanExtra(EXTRA_TRIGGER_ADMIN_AUTH, false)

        setContent {
            AppleTheme {
                val colors = LocalAppleColors.current
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var showAdminAuthDialog by remember { mutableStateOf(triggerAdminAuth) }
                var showDeprovisionDialog by remember { mutableStateOf(false) }
                var pendingAdminAction by remember { mutableStateOf<(() -> Unit)?>(null) }

                // Live capability audit
                var capabilityReport by remember {
                    mutableStateOf(app.compatibilityDetector.generateReport())
                }

                fun refreshReport() {
                    capabilityReport = app.compatibilityDetector.generateReport()
                }

                fun requestAdminAuth(action: () -> Unit) {
                    if (app.adminAuthManager.hasActiveSession()) {
                        action()
                    } else {
                        pendingAdminAction = action
                        showAdminAuthDialog = true
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppleTopBar(
                            operatingMode = capabilityReport.operatingMode,
                            onAdminClick = { showAdminAuthDialog = true }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = colors.glassSurface,
                            tonalElevation = 2.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.HOME,
                                onClick = { currentScreen = AppScreen.HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.POLICY,
                                onClick = { currentScreen = AppScreen.POLICY },
                                icon = { Icon(Icons.Default.Security, contentDescription = "Policies") },
                                label = { Text("Policies") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CONTROLLER,
                                onClick = { currentScreen = AppScreen.CONTROLLER },
                                icon = { Icon(Icons.Default.AllInclusive, contentDescription = "Controller") },
                                label = { Text("Controller") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.COMPATIBILITY,
                                onClick = { currentScreen = AppScreen.COMPATIBILITY },
                                icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
                                label = { Text("Report") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(
                                report = capabilityReport,
                                onNavigateToPolicyManager = { currentScreen = AppScreen.POLICY },
                                onNavigateToStudyMode = { currentScreen = AppScreen.CONTROLLER },
                                onNavigateToCompatibility = { currentScreen = AppScreen.COMPATIBILITY },
                                onNavigateToEnrollment = { currentScreen = AppScreen.ENROLLMENT }
                            )
                            AppScreen.POLICY -> PolicyManagerScreen(
                                operatingMode = capabilityReport.operatingMode,
                                policyBridge = app.policyBridge,
                                inventoryManager = app.appInventoryManager,
                                onRequireAdminAuth = { action -> requestAdminAuth(action) },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                            AppScreen.CONTROLLER -> PermanentControllerScreen(
                                operatingMode = capabilityReport.operatingMode,
                                persistentPolicyManager = app.persistentPolicyManager,
                                remoteConfigManager = app.gitHubRemoteConfigManager,
                                onStartPermanentKiosk = { startLockTaskMode() },
                                onStopPermanentKiosk = { stopLockTaskMode() },
                                onNavigateToPolicyManager = { currentScreen = AppScreen.POLICY },
                                onRequireAdminAuth = { action -> requestAdminAuth(action) },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                            AppScreen.COMPATIBILITY -> CompatibilityReportScreen(
                                report = capabilityReport,
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                            AppScreen.ENROLLMENT -> EnrollmentGuideScreen(
                                workflowManager = app.enrollmentWorkflowManager,
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }
                    }

                    // Administrator Authentication Modal
                    if (showAdminAuthDialog) {
                        AdminAuthDialog(
                            authManager = app.adminAuthManager,
                            onSuccess = {
                                showAdminAuthDialog = false
                                pendingAdminAction?.invoke()
                                pendingAdminAction = null
                                Toast.makeText(this@MainActivity, "Admin Authenticated", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = {
                                showAdminAuthDialog = false
                                pendingAdminAction = null
                            }
                        )
                    }

                    // Deprovisioning Modal
                    if (showDeprovisionDialog) {
                        DeprovisionDialog(
                            policyBridge = app.policyBridge,
                            onDeprovisionComplete = {
                                showDeprovisionDialog = false
                                refreshReport()
                                Toast.makeText(this@MainActivity, "Device Deprovisioned", Toast.LENGTH_LONG).show()
                            },
                            onDismiss = { showDeprovisionDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_TRIGGER_ADMIN_AUTH, false)) {
            // Triggered via Quick Settings Tile or Dialer Secret Code
            Toast.makeText(this, "Admin entry requested via system shortcut", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLockTaskMode() {
        try {
            if (app.policyBridge.isDeviceOwner()) {
                app.policyBridge.setLockTaskPackages(arrayOf(packageName))
            }
            startLockTask()
            Toast.makeText(this, "Kiosk Mode Activated", Toast.LENGTH_SHORT).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Kiosk activation failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLockTaskMode() {
        try {
            stopLockTask()
            Toast.makeText(this, "Kiosk Mode Exited", Toast.LENGTH_SHORT).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Kiosk exit failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_TRIGGER_ADMIN_AUTH = "com.fahad.devicecontroller.extra.TRIGGER_ADMIN_AUTH"
    }
}

