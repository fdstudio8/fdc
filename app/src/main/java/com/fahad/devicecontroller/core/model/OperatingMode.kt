package com.fahad.devicecontroller.core.model

/**
 * Operating modes for Fahad Device Controller (FDC).
 * FDC automatically operates in one of these three distinct tiers depending on runtime privilege.
 */
enum class OperatingMode(
    val title: String,
    val description: String,
    val isEnforcementSupported: Boolean
) {
    /**
     * Mode 1: Installed as normal user APK. No Device Owner privileges.
     * Core productivity, study timer, and notes work normally.
     * System restrictions are disabled and explicitly labeled as unsupported.
     */
    STANDARD(
        title = "Mode 1: Standard Mode",
        description = "Unmanaged APK. System policies and restrictions are disabled.",
        isEnforcementSupported = false
    ),

    /**
     * Mode 2: Provisioned as Android Enterprise Device Owner (AOSP DPC).
     * Full universal enterprise policies active (App Hiding, Suspension, Kiosk Lock Task, Reset Block).
     */
    DEVICE_OWNER(
        title = "Mode 2: Device Owner Mode",
        description = "Fully managed via Android Enterprise DPC. System policies actively enforced.",
        isEnforcementSupported = true
    ),

    /**
     * Mode 3: Device Owner enrolled AND manufacturer-specific enterprise extensions active.
     * Includes Samsung Knox (dynamically loaded via reflection) or vendor power bridges.
     */
    OEM_EXTENSION(
        title = "Mode 3: OEM Extension Mode",
        description = "Device Owner + Vendor Hardware Extension (Knox / HyperOS / ColorOS) active.",
        isEnforcementSupported = true
    )
}

