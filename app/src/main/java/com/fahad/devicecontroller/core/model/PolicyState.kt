package com.fahad.devicecontroller.core.model

import android.graphics.drawable.Drawable

/**
 * Representation of an installed app and its management restrictions.
 */
data class ManagedApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isHidden: Boolean = false,
    val isSuspended: Boolean = false,
    val isUninstallBlocked: Boolean = false,
    val icon: Drawable? = null
)

/**
 * Global system-level policies governed by FDC.
 */
data class GlobalPolicyState(
    val isFactoryResetBlocked: Boolean = false,
    val isSafeBootBlocked: Boolean = false,
    val isAddUserBlocked: Boolean = false,
    val isUnknownSourcesBlocked: Boolean = false,
    val isCameraBlocked: Boolean = false,
    val isLockTaskActive: Boolean = false,
    val activeLockTaskPackages: Set<String> = emptySet(),
    val totalHiddenApps: Int = 0,
    val totalSuspendedApps: Int = 0
)

/**
 * Policy status badge indicator state.
 */
enum class PolicyEnforcementStatus {
    ENFORCED_ACTIVE,
    PENDING_ELEVATION,
    REQUIRES_DEVICE_OWNER,
    UNSUPPORTED_ON_HARDWARE
}

