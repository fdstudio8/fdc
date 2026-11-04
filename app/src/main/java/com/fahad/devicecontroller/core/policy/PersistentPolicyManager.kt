package com.fahad.devicecontroller.core.policy

import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager
import android.util.Log
import com.fahad.devicecontroller.dpc.DevicePolicyBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

data class PermanentPolicyConfig(
    val isPermanentLockdownEnabled: Boolean = false,
    val isPermanentKioskEnabled: Boolean = false,
    val isAntiTamperShieldEnabled: Boolean = true,
    val isFactoryResetBlocked: Boolean = true,
    val isSafeBootBlocked: Boolean = true,
    val isUnknownSourcesBlocked: Boolean = true,
    val isCameraBlocked: Boolean = false,
    val suspendedPackages: Set<String> = emptySet(),
    val hiddenPackages: Set<String> = emptySet(),
    val uninstallBlockedPackages: Set<String> = emptySet()
)

/**
 * Permanent Policy Persistence & Autonomic Re-enforcement Engine.
 * Ensures that whatever setting is configured stays active PERMANENTLY ("Hamesha ke liye")
 * across phone reboots, app restarts, and process terminations.
 */
class PersistentPolicyManager(
    private val context: Context,
    private val policyBridge: DevicePolicyBridge
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<PermanentPolicyConfig> = _config.asStateFlow()

    init {
        // Automatically re-apply all permanent policies upon initialization
        reapplyAllPolicies()
    }

    /**
     * Loads saved permanent policy configuration from persistent storage.
     */
    fun loadConfig(): PermanentPolicyConfig {
        val isLockdown = prefs.getBoolean(KEY_PERMANENT_LOCKDOWN, false)
        val isKiosk = prefs.getBoolean(KEY_PERMANENT_KIOSK, false)
        val isAntiTamper = prefs.getBoolean(KEY_ANTI_TAMPER, true)
        val isResetBlocked = prefs.getBoolean(KEY_BLOCK_RESET, true)
        val isSafeBootBlocked = prefs.getBoolean(KEY_BLOCK_SAFE_BOOT, true)
        val isUnknownSourcesBlocked = prefs.getBoolean(KEY_BLOCK_UNKNOWN_SOURCES, true)
        val isCameraBlocked = prefs.getBoolean(KEY_BLOCK_CAMERA, false)

        val suspended = loadStringSet(KEY_SUSPENDED_PACKAGES)
        val hidden = loadStringSet(KEY_HIDDEN_PACKAGES)
        val uninstallBlocked = loadStringSet(KEY_UNINSTALL_BLOCKED_PACKAGES)

        return PermanentPolicyConfig(
            isPermanentLockdownEnabled = isLockdown,
            isPermanentKioskEnabled = isKiosk,
            isAntiTamperShieldEnabled = isAntiTamper,
            isFactoryResetBlocked = isResetBlocked,
            isSafeBootBlocked = isSafeBootBlocked,
            isUnknownSourcesBlocked = isUnknownSourcesBlocked,
            isCameraBlocked = isCameraBlocked,
            suspendedPackages = suspended,
            hiddenPackages = hidden,
            uninstallBlockedPackages = uninstallBlocked
        )
    }

    /**
     * Re-applies all persistent policies to hardware via DevicePolicyBridge.
     * Guaranteed to execute on device boot and application launch.
     */
    fun reapplyAllPolicies() {
        if (!policyBridge.isDeviceOwner()) {
            Log.w(TAG, "Device is not in Device Owner mode. Persistent system policies awaiting elevation.")
            return
        }

        try {
            val current = _config.value

            // 1. Re-enforce Anti-Tamper Shield (Force-stop & Clear-data block in Settings)
            if (current.isAntiTamperShieldEnabled) {
                policyBridge.enforceTamperResistance(true)
            }

            // 2. Re-enforce Factory Reset Block
            policyBridge.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, current.isFactoryResetBlocked)

            // 3. Re-enforce Safe Boot Block
            policyBridge.setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, current.isSafeBootBlocked)

            // 4. Re-enforce Unknown Sources Block
            policyBridge.setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, current.isUnknownSourcesBlocked)

            // 5. Re-enforce Permanently Suspended Packages
            if (current.suspendedPackages.isNotEmpty()) {
                policyBridge.setPackagesSuspended(current.suspendedPackages.toTypedArray(), true)
            }

            // 6. Re-enforce Permanently Hidden Packages
            for (pkg in current.hiddenPackages) {
                policyBridge.setApplicationHidden(pkg, true)
            }

            // 7. Re-enforce Uninstall Blocked Packages
            for (pkg in current.uninstallBlockedPackages) {
                policyBridge.setUninstallBlocked(pkg, true)
            }
            // Always block uninstall of FDC itself
            policyBridge.setUninstallBlocked(context.packageName, true)

            // 8. Re-enforce Kiosk Lock Task packages if permanent kiosk is active
            if (current.isPermanentKioskEnabled) {
                policyBridge.setLockTaskPackages(arrayOf(context.packageName))
            }

            Log.i(TAG, "All permanent policies successfully re-enforced on hardware.")
        } catch (e: Throwable) {
            Log.e(TAG, "Error re-applying permanent policies", e)
        }
    }

    fun setPermanentLockdown(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERMANENT_LOCKDOWN, enabled).apply()
        _config.value = _config.value.copy(isPermanentLockdownEnabled = enabled)
        reapplyAllPolicies()
    }

    fun setPermanentKiosk(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERMANENT_KIOSK, enabled).apply()
        _config.value = _config.value.copy(isPermanentKioskEnabled = enabled)
        reapplyAllPolicies()
    }

    fun setAntiTamperShield(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_TAMPER, enabled).apply()
        _config.value = _config.value.copy(isAntiTamperShieldEnabled = enabled)
        policyBridge.enforceTamperResistance(enabled)
    }

    fun setFactoryResetBlocked(blocked: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_RESET, blocked).apply()
        _config.value = _config.value.copy(isFactoryResetBlocked = blocked)
        policyBridge.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, blocked)
    }

    fun setSafeBootBlocked(blocked: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_SAFE_BOOT, blocked).apply()
        _config.value = _config.value.copy(isSafeBootBlocked = blocked)
        policyBridge.setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, blocked)
    }

    fun setUnknownSourcesBlocked(blocked: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_UNKNOWN_SOURCES, blocked).apply()
        _config.value = _config.value.copy(isUnknownSourcesBlocked = blocked)
        policyBridge.setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, blocked)
    }

    fun updateSuspendedPackages(newSet: Set<String>) {
        val current = _config.value.suspendedPackages
        val toRemove = current - newSet
        val toAdd = newSet - current

        // Unsuspend packages that were removed from remote config
        if (toRemove.isNotEmpty()) {
            policyBridge.setPackagesSuspended(toRemove.toTypedArray(), false)
        }

        // Suspend packages that were added to remote config
        if (toAdd.isNotEmpty()) {
            policyBridge.setPackagesSuspended(toAdd.toTypedArray(), true)
        }

        saveStringSet(KEY_SUSPENDED_PACKAGES, newSet)
        _config.value = _config.value.copy(suspendedPackages = newSet)
        Log.i(TAG, "Suspended packages updated from remote. Active: ${newSet.size}, Suspended: +${toAdd.size}, Restored: -${toRemove.size}")
    }

    fun updateHiddenPackages(newSet: Set<String>) {
        val current = _config.value.hiddenPackages
        val toRemove = current - newSet
        val toAdd = newSet - current

        // Unhide packages that were removed from remote config
        for (pkg in toRemove) {
            policyBridge.setApplicationHidden(pkg, false)
        }

        // Hide packages that were added to remote config
        for (pkg in toAdd) {
            policyBridge.setApplicationHidden(pkg, true)
        }

        saveStringSet(KEY_HIDDEN_PACKAGES, newSet)
        _config.value = _config.value.copy(hiddenPackages = newSet)
        Log.i(TAG, "Hidden packages updated from remote. Active: ${newSet.size}, Hidden: +${toAdd.size}, Restored: -${toRemove.size}")
    }

    fun addSuspendedPackage(packageName: String) {
        val updated = _config.value.suspendedPackages + packageName
        saveStringSet(KEY_SUSPENDED_PACKAGES, updated)
        _config.value = _config.value.copy(suspendedPackages = updated)
        policyBridge.setPackagesSuspended(arrayOf(packageName), true)
    }

    fun removeSuspendedPackage(packageName: String) {
        val updated = _config.value.suspendedPackages - packageName
        saveStringSet(KEY_SUSPENDED_PACKAGES, updated)
        _config.value = _config.value.copy(suspendedPackages = updated)
        policyBridge.setPackagesSuspended(arrayOf(packageName), false)
    }

    fun addHiddenPackage(packageName: String) {
        val updated = _config.value.hiddenPackages + packageName
        saveStringSet(KEY_HIDDEN_PACKAGES, updated)
        _config.value = _config.value.copy(hiddenPackages = updated)
        policyBridge.setApplicationHidden(packageName, true)
    }

    fun removeHiddenPackage(packageName: String) {
        val updated = _config.value.hiddenPackages - packageName
        saveStringSet(KEY_HIDDEN_PACKAGES, updated)
        _config.value = _config.value.copy(hiddenPackages = updated)
        policyBridge.setApplicationHidden(packageName, false)
    }

    fun addUninstallBlockedPackage(packageName: String) {
        val updated = _config.value.uninstallBlockedPackages + packageName
        saveStringSet(KEY_UNINSTALL_BLOCKED_PACKAGES, updated)
        _config.value = _config.value.copy(uninstallBlockedPackages = updated)
        policyBridge.setUninstallBlocked(packageName, true)
    }

    fun removeUninstallBlockedPackage(packageName: String) {
        val updated = _config.value.uninstallBlockedPackages - packageName
        saveStringSet(KEY_UNINSTALL_BLOCKED_PACKAGES, updated)
        _config.value = _config.value.copy(uninstallBlockedPackages = updated)
        policyBridge.setUninstallBlocked(packageName, false)
    }

    private fun loadStringSet(key: String): Set<String> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            val result = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                result.add(jsonArray.getString(i))
            }
            result
        } catch (e: Throwable) {
            emptySet()
        }
    }

    private fun saveStringSet(key: String, set: Set<String>) {
        val jsonArray = JSONArray()
        for (item in set) {
            jsonArray.put(item)
        }
        prefs.edit().putString(key, jsonArray.toString()).apply()
    }

    companion object {
        private const val TAG = "PersistentPolicyManager"
        private const val PREFS_NAME = "fdc_permanent_policies"
        private const val KEY_PERMANENT_LOCKDOWN = "permanent_lockdown"
        private const val KEY_PERMANENT_KIOSK = "permanent_kiosk"
        private const val KEY_ANTI_TAMPER = "anti_tamper"
        private const val KEY_BLOCK_RESET = "block_reset"
        private const val KEY_BLOCK_SAFE_BOOT = "block_safe_boot"
        private const val KEY_BLOCK_UNKNOWN_SOURCES = "block_unknown_sources"
        private const val KEY_BLOCK_CAMERA = "block_camera"
        private const val KEY_SUSPENDED_PACKAGES = "suspended_packages"
        private const val KEY_HIDDEN_PACKAGES = "hidden_packages"
        private const val KEY_UNINSTALL_BLOCKED_PACKAGES = "uninstall_blocked_packages"
    }
}

