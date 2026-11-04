package com.fahad.devicecontroller.dpc

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.UserManager
import android.util.Log

/**
 * Universal Android Enterprise Device Policy Bridge.
 * Exposes core AOSP DPC operations, Anti-Bypass tamper resistance,
 * hardware verification, and graceful error isolation.
 */
class DevicePolicyBridge(private val context: Context) {

    private val dpm: DevicePolicyManager? =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

    private val userManager: UserManager? =
        context.getSystemService(Context.USER_SERVICE) as? UserManager

    val adminComponent: ComponentName = FdcDeviceAdminReceiver.getComponentName(context)

    /**
     * Checks if FDC is actively enrolled as the Device Owner.
     */
    fun isDeviceOwner(): Boolean {
        return try {
            dpm?.isDeviceOwnerApp(context.packageName) == true
        } catch (e: Throwable) {
            Log.e(TAG, "Error checking device owner status", e)
            false
        }
    }

    /**
     * Checks if FDC is actively enrolled as a Profile Owner (Managed Work Profile).
     */
    fun isProfileOwner(): Boolean {
        return try {
            dpm?.isProfileOwnerApp(context.packageName) == true
        } catch (e: Throwable) {
            Log.e(TAG, "Error checking profile owner status", e)
            false
        }
    }

    /**
     * Checks if FDC is an active device administrator.
     */
    fun isAdminActive(): Boolean {
        return try {
            dpm?.isAdminActive(adminComponent) == true
        } catch (e: Throwable) {
            Log.e(TAG, "Error checking active admin status", e)
            false
        }
    }

    /**
     * Hides or unhides an application package (removes from launcher).
     * Supported in Device Owner mode.
     */
    fun setApplicationHidden(packageName: String, hidden: Boolean): Boolean {
        if (!isDeviceOwner()) return false
        return try {
            val success = dpm?.setApplicationHidden(adminComponent, packageName, hidden) == true
            val verified = isApplicationHidden(packageName) == hidden
            success && verified
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to setApplicationHidden for $packageName", e)
            false
        }
    }

    /**
     * Verifies if an application package is currently hidden.
     */
    fun isApplicationHidden(packageName: String): Boolean {
        return try {
            dpm?.isApplicationHidden(adminComponent, packageName) == true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Suspends or unsuspends packages. Suspended packages cannot be launched and appear grayed out.
     */
    fun setPackagesSuspended(packageNames: Array<String>, suspended: Boolean): Boolean {
        if (!isDeviceOwner()) return false
        return try {
            val failedPackages = dpm?.setPackagesSuspended(adminComponent, packageNames, suspended)
            failedPackages != null && failedPackages.isEmpty()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to setPackagesSuspended", e)
            false
        }
    }

    /**
     * Verifies if a package is suspended.
     */
    fun isPackageSuspended(packageName: String): Boolean {
        return try {
            dpm?.isPackageSuspended(adminComponent, packageName) == true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Blocks or allows uninstallation of an application package.
     */
    fun setUninstallBlocked(packageName: String, blocked: Boolean): Boolean {
        if (!isDeviceOwner() && !isProfileOwner()) return false
        return try {
            dpm?.setUninstallBlocked(adminComponent, packageName, blocked)
            isUninstallBlocked(packageName) == blocked
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to setUninstallBlocked for $packageName", e)
            false
        }
    }

    /**
     * Verifies if uninstallation of an application is currently blocked.
     */
    fun isUninstallBlocked(packageName: String): Boolean {
        return try {
            dpm?.isUninstallBlocked(adminComponent, packageName) == true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Whitelists packages eligible for Lock Task (Kiosk) Mode.
     */
    fun setLockTaskPackages(packages: Array<String>): Boolean {
        if (!isDeviceOwner()) return false
        return try {
            dpm?.setLockTaskPackages(adminComponent, packages)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to setLockTaskPackages", e)
            false
        }
    }

    /**
     * Queries currently permitted Lock Task packages.
     */
    fun getLockTaskPackages(): Set<String> {
        return try {
            dpm?.getLockTaskPackages(adminComponent)?.toSet() ?: emptySet()
        } catch (e: Throwable) {
            emptySet()
        }
    }

    /**
     * Applies or removes a standard Android user restriction (e.g. DISALLOW_FACTORY_RESET).
     */
    fun setUserRestriction(restrictionKey: String, enable: Boolean): Boolean {
        if (!isDeviceOwner() && !isProfileOwner()) return false
        return try {
            if (enable) {
                dpm?.addUserRestriction(adminComponent, restrictionKey)
            } else {
                dpm?.clearUserRestriction(adminComponent, restrictionKey)
            }
            hasUserRestriction(restrictionKey) == enable
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to toggle user restriction $restrictionKey", e)
            false
        }
    }

    /**
     * Checks if a user restriction is actively applied.
     */
    fun hasUserRestriction(restrictionKey: String): Boolean {
        return try {
            val bundle = dpm?.getUserRestrictions(adminComponent)
            bundle?.getBoolean(restrictionKey, false) == true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Maximum Anti-Bypass & Tamper Resistance Suite.
     * Enforces impenetrable system locks:
     * - Prevents uninstallation of FDC
     * - Blocks App Info (Force Stop & Clear Data) in Android Settings
     * - Blocks changing System Date/Time (preventing clock tampering to bypass lockout)
     * - Blocks Settings-based Factory Reset
     * - Blocks Safe Mode Boot
     * - Blocks Guest / Secondary User account creation
     */
    fun enforceTamperResistance(enable: Boolean): Boolean {
        if (!isDeviceOwner()) return false
        return try {
            // 1. Lock self uninstallation
            dpm?.setUninstallBlocked(adminComponent, context.packageName, enable)

            // 2. Anti-tamper user restrictions
            val antiBypassRestrictions = listOf(
                UserManager.DISALLOW_APPS_CONTROL,       // Disables "Clear Data" and "Force Stop" in Settings
                UserManager.DISALLOW_CONFIG_DATE_TIME,   // Prevents clock manipulation to bypass timers/lockouts
                UserManager.DISALLOW_FACTORY_RESET,      // Prevents Settings factory reset
                UserManager.DISALLOW_SAFE_BOOT,          // Prevents Safe Mode reboot
                UserManager.DISALLOW_ADD_USER,           // Prevents switching to guest profile
                UserManager.DISALLOW_MODIFY_ACCOUNTS     // Prevents account modification tampering
            )

            for (restriction in antiBypassRestrictions) {
                if (enable) {
                    dpm?.addUserRestriction(adminComponent, restriction)
                } else {
                    dpm?.clearUserRestriction(adminComponent, restriction)
                }
            }
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error applying tamper resistance", e)
            false
        }
    }

    /**
     * Verifies if comprehensive Anti-Bypass Tamper Resistance is active.
     */
    fun isTamperResistanceActive(): Boolean {
        return isDeviceOwner() &&
                isUninstallBlocked(context.packageName) &&
                hasUserRestriction(UserManager.DISALLOW_APPS_CONTROL) &&
                hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET)
    }

    /**
     * Clears all enforced restrictions in preparation for safe, authorized deprovisioning.
     */
    fun clearAllRestrictions(): Boolean {
        return try {
            val restrictions = listOf(
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
                UserManager.DISALLOW_MODIFY_ACCOUNTS,
                UserManager.DISALLOW_APPS_CONTROL,
                UserManager.DISALLOW_CONFIG_DATE_TIME
            )
            for (r in restrictions) {
                dpm?.clearUserRestriction(adminComponent, r)
            }

            // Allow self-uninstallation
            dpm?.setUninstallBlocked(adminComponent, context.packageName, false)

            // Reset Lock Task packages
            dpm?.setLockTaskPackages(adminComponent, arrayOf(context.packageName))
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error clearing restrictions during deprovisioning", e)
            false
        }
    }

    /**
     * Deprovisions FDC from Device Owner status.
     * WARNING: Only works if clearDeviceOwnerApp is invoked by the active DO package.
     */
    fun clearDeviceOwner(): Boolean {
        if (!isDeviceOwner()) return false
        return try {
            clearAllRestrictions()
            @Suppress("DEPRECATION")
            dpm?.clearDeviceOwnerApp(context.packageName)
            !isDeviceOwner()
        } catch (e: Throwable) {
            Log.e(TAG, "Error clearing device owner", e)
            false
        }
    }

    companion object {
        private const val TAG = "DevicePolicyBridge"
    }
}
