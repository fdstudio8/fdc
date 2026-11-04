package com.fahad.devicecontroller.core.model

/**
 * Audit report detailing the runtime environment, active operating mode,
 * and individual policy support states.
 */
data class DeviceCapabilityReport(
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val manufacturer: String,
    val brand: OemBrand,
    val model: String,
    val buildFingerprint: String,
    val operatingMode: OperatingMode,
    val isDeviceOwner: Boolean,
    val isProfileOwner: Boolean,
    val isAdminActive: Boolean,
    val hasKnoxSupport: Boolean,
    val knoxApiLevel: Int?,
    val isAppHidingSupported: Boolean,
    val isAppSuspensionSupported: Boolean,
    val isLockTaskSupported: Boolean,
    val isUninstallBlockSupported: Boolean,
    val isFactoryResetBlockSupported: Boolean,
    val oemNotes: String,
    val timestamp: Long = System.currentTimeMillis()
)

