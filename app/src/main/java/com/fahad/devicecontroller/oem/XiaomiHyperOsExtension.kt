package com.fahad.devicecontroller.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fahad.devicecontroller.core.model.OemBrand

/**
 * Xiaomi / POCO / Redmi HyperOS & MIUI Extension.
 * Resolves proprietary SecurityCenter AutoStart and aggressive process termination.
 */
class XiaomiHyperOsExtension(private val context: Context) : OemExtension {

    override val brand: OemBrand = OemBrand.XIAOMI
    override val extensionName: String = "Xiaomi HyperOS / MIUI Power Bridge"

    override fun isSupported(): Boolean {
        val m = Build.MANUFACTURER.lowercase()
        val b = Build.BRAND.lowercase()
        return m.contains("xiaomi") || m.contains("redmi") || b.contains("poco")
    }

    override fun getDiagnosticInfo(): String {
        return if (isSupported()) {
            "Active: Xiaomi HyperOS / MIUI SecurityCenter Detected"
        } else {
            "Inactive: Non-Xiaomi Hardware"
        }
    }

    override fun applyExtensionPolicy(policyName: String, enabled: Boolean): Boolean {
        return isSupported()
    }

    /**
     * Creates an Intent to launch Xiaomi's native AutoStart Management screen.
     */
    fun createAutoStartIntent(): Intent {
        val intent = Intent()
        intent.component = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    /**
     * Creates an Intent to launch Xiaomi's native Battery Saver App Management.
     */
    fun createBatterySaverIntent(): Intent {
        val intent = Intent()
        intent.component = ComponentName(
            "com.miui.powerkeeper",
            "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
        )
        intent.putExtra("package_name", context.packageName)
        intent.putExtra("package_label", "FDC")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}

