package com.fahad.devicecontroller.dpc

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import com.fahad.devicecontroller.FdcApplication
import com.fahad.devicecontroller.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Universal Android Enterprise Device Administration & Device Owner Broadcast Receiver.
 * Receives lifecycle events for DPC provisioning, lock task mode, and admin status changes.
 *
 * Implements FULL ZERO-TOUCH AUTO-SETUP:
 * When enrolled via 6-tap QR code, Android Enterprise automatically calls onProfileProvisioningComplete.
 * This receiver instantly applies all permanent restrictions, locks down uninstallation,
 * blocks Settings App Info, and syncs from GitHub without requiring any manual setup!
 */
class FdcDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "FDC Device Administration activated.")
        Toast.makeText(context, "FDC: Device Administration Activated", Toast.LENGTH_SHORT).show()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "FDC Android Enterprise Provisioning Completed. Starting FULL AUTO-SETUP...")

        try {
            val app = context.applicationContext as? FdcApplication ?: return
            val policyBridge = app.policyBridge
            val persistentPolicyManager = app.persistentPolicyManager
            val remoteConfigManager = app.gitHubRemoteConfigManager

            // Extract optional extras from the QR Code provisioning bundle
            val extras: PersistableBundle? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.getParcelableExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE)
            } else {
                null
            }

            val githubUrlFromQr = extras?.getString("github_config_url")
            if (!githubUrlFromQr.isNullOrBlank()) {
                remoteConfigManager.setGitHubJsonUrl(githubUrlFromQr)
            }

            // 1. Instant Permanent Strict Lockdown
            persistentPolicyManager.setPermanentLockdown(true)

            // 2. Instant Anti-Tamper Shield (Force Stop & Clear Data Disabled in Settings)
            persistentPolicyManager.setAntiTamperShield(true)
            policyBridge.enforceTamperResistance(true)

            // 3. Permanent System Safeguards
            persistentPolicyManager.setFactoryResetBlocked(true)
            persistentPolicyManager.setSafeBootBlocked(true)
            persistentPolicyManager.setUnknownSourcesBlocked(true)

            // 4. Lock self-uninstallation immediately
            policyBridge.setUninstallBlocked(context.packageName, true)

            // 5. Kick off immediate asynchronous GitHub policy sync
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    remoteConfigManager.syncFromGitHub()
                    Log.i(TAG, "Full Auto-Setup: GitHub sync completed successfully.")
                } catch (e: Throwable) {
                    Log.w(TAG, "Auto-Setup initial sync deferred: ${e.message}")
                }
            }

            // 6. Automatically launch MainActivity so the user lands straight into the governed system
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(launchIntent)

            Toast.makeText(context, "FDC: Full Auto-Setup Complete (Permanent Mode Active)", Toast.LENGTH_LONG).show()
            Log.i(TAG, "FULL AUTO-SETUP SUCCESSFULLY EXECUTED!")
        } catch (e: Throwable) {
            Log.e(TAG, "Error during automatic provisioning setup", e)
        }
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "CRITICAL SECURITY WARNING: FDC Device Administration is protected. Unauthorized deactivation is blocked. Open FDC and authenticate with your Admin PIN or Master Key to deprovision."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "FDC Device Administration disabled.")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.i(TAG, "Entering Kiosk Lock Task Mode with package: $pkg")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.i(TAG, "Exiting Kiosk Lock Task Mode.")
    }

    companion object {
        private const val TAG = "FdcDeviceAdmin"

        /**
         * Returns the canonical ComponentName for this DPC receiver.
         */
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, FdcDeviceAdminReceiver::class.java)
        }
    }
}
