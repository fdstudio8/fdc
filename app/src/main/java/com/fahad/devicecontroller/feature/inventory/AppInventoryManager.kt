package com.fahad.devicecontroller.feature.inventory

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.fahad.devicecontroller.core.model.ManagedApp
import com.fahad.devicecontroller.dpc.DevicePolicyBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Manages device application inventory, package state verification, and restrictions.
 */
class AppInventoryManager(
    private val context: Context,
    private val policyBridge: DevicePolicyBridge
) {

    private val packageManager: PackageManager = context.packageManager

    private val _installedApps = MutableStateFlow<List<ManagedApp>>(emptyList())
    val installedApps: StateFlow<List<ManagedApp>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Scans installed applications and inspects live DPC restriction status.
     */
    suspend fun refreshApps() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val flags = PackageManager.GET_META_DATA
            val packages = packageManager.getInstalledApplications(flags)
            val result = mutableListOf<ManagedApp>()

            for (appInfo in packages) {
                // Skip FDC itself from restriction targeting
                if (appInfo.packageName == context.packageName) continue

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Throwable) {
                    appInfo.packageName
                }

                val isHidden = policyBridge.isApplicationHidden(appInfo.packageName)
                val isSuspended = policyBridge.isPackageSuspended(appInfo.packageName)
                val isUninstallBlocked = policyBridge.isUninstallBlocked(appInfo.packageName)

                result.add(
                    ManagedApp(
                        packageName = appInfo.packageName,
                        appName = label,
                        isSystemApp = isSystem,
                        isHidden = isHidden,
                        isSuspended = isSuspended,
                        isUninstallBlocked = isUninstallBlocked,
                        icon = null // Loaded lazily if needed to conserve memory
                    )
                )
            }

            // Sort: user apps first, then alphabetically
            val sorted = result.sortedWith(
                compareBy<ManagedApp> { it.isSystemApp }
                    .thenBy { it.appName.lowercase() }
            )
            _installedApps.value = sorted
        } catch (e: Throwable) {
            // Graceful fallback
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Toggles application hiding. Returns true if verification succeeded.
     */
    fun toggleAppHidden(packageName: String, shouldHide: Boolean): Boolean {
        val success = policyBridge.setApplicationHidden(packageName, shouldHide)
        if (success) {
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == packageName) it.copy(isHidden = shouldHide) else it
            }
        }
        return success
    }

    /**
     * Toggles application suspension.
     */
    fun toggleAppSuspended(packageName: String, shouldSuspend: Boolean): Boolean {
        val success = policyBridge.setPackagesSuspended(arrayOf(packageName), shouldSuspend)
        if (success) {
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == packageName) it.copy(isSuspended = shouldSuspend) else it
            }
        }
        return success
    }

    /**
     * Toggles uninstall block.
     */
    fun toggleUninstallBlocked(packageName: String, shouldBlock: Boolean): Boolean {
        val success = policyBridge.setUninstallBlocked(packageName, shouldBlock)
        if (success) {
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == packageName) it.copy(isUninstallBlocked = shouldBlock) else it
            }
        }
        return success
    }
}

