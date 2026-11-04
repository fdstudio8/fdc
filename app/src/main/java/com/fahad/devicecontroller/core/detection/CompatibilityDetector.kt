package com.fahad.devicecontroller.core.detection

import android.content.Context
import android.os.Build
import com.fahad.devicecontroller.core.model.DeviceCapabilityReport
import com.fahad.devicecontroller.core.model.OemBrand
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.dpc.DevicePolicyBridge
import com.fahad.devicecontroller.oem.OemExtensionManager

/**
 * 7-Step Dynamic Compatibility & Capability Detection Engine.
 * Audits runtime environment on startup and determines supported feature set with zero false claims.
 */
class CompatibilityDetector(
    private val context: Context,
    private val policyBridge: DevicePolicyBridge,
    private val oemManager: OemExtensionManager
) {

    /**
     * Executes the comprehensive hardware and API audit.
     */
    fun generateReport(): DeviceCapabilityReport {
        // Step 1: Detect Android version & API level
        val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
        val apiLevel = Build.VERSION.SDK_INT
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH ?: "Unknown"
        } else {
            "Pre-M"
        }
        val buildFingerprint = Build.FINGERPRINT ?: "Unknown"

        // Step 2: Detect Manufacturer & Brand
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val brand = oemManager.detectedBrand
        val model = Build.MODEL ?: "Unknown"

        // Step 3: Detect Device Owner & Admin Status
        val isDeviceOwner = policyBridge.isDeviceOwner()
        val isProfileOwner = policyBridge.isProfileOwner()
        val isAdminActive = policyBridge.isAdminActive()

        // Step 4: Detect Supported Management APIs
        val isAppHidingSupported = isDeviceOwner && apiLevel >= Build.VERSION_CODES.LOLLIPOP
        val isAppSuspensionSupported = isDeviceOwner && apiLevel >= Build.VERSION_CODES.N
        val isLockTaskSupported = isDeviceOwner && apiLevel >= Build.VERSION_CODES.LOLLIPOP
        val isUninstallBlockSupported = (isDeviceOwner || isProfileOwner) && apiLevel >= Build.VERSION_CODES.LOLLIPOP
        val isFactoryResetBlockSupported = isDeviceOwner && apiLevel >= Build.VERSION_CODES.LOLLIPOP

        // Step 5: Detect Available Enterprise Extensions
        val hasKnoxSupport = oemManager.hasActiveOemExtension() && brand == OemBrand.SAMSUNG
        val knoxApiLevel = if (hasKnoxSupport) 35 else null

        // Step 6: Resolve Operating Mode
        val operatingMode = when {
            isDeviceOwner && oemManager.hasActiveOemExtension() -> OperatingMode.OEM_EXTENSION
            isDeviceOwner -> OperatingMode.DEVICE_OWNER
            else -> OperatingMode.STANDARD
        }

        // Step 7: Compile Diagnostics
        val oemNotes = oemManager.getDiagnosticSummary()

        return DeviceCapabilityReport(
            androidVersion = androidVersion,
            apiLevel = apiLevel,
            securityPatch = securityPatch,
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            buildFingerprint = buildFingerprint,
            operatingMode = operatingMode,
            isDeviceOwner = isDeviceOwner,
            isProfileOwner = isProfileOwner,
            isAdminActive = isAdminActive,
            hasKnoxSupport = hasKnoxSupport,
            knoxApiLevel = knoxApiLevel,
            isAppHidingSupported = isAppHidingSupported,
            isAppSuspensionSupported = isAppSuspensionSupported,
            isLockTaskSupported = isLockTaskSupported,
            isUninstallBlockSupported = isUninstallBlockSupported,
            isFactoryResetBlockSupported = isFactoryResetBlockSupported,
            oemNotes = oemNotes
        )
    }
}

