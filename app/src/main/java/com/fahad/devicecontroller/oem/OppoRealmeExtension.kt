package com.fahad.devicecontroller.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fahad.devicecontroller.core.model.OemBrand

/**
 * OPPO / OnePlus / Realme (ColorOS / OxygenOS / Realme UI) Extension.
 * Prevents aggressive task freezer terminations on Oplus unified architecture.
 */
class OppoRealmeExtension(private val context: Context) : OemExtension {

    override val brand: OemBrand = OemBrand.OPPO
    override val extensionName: String = "ColorOS / OxygenOS / Realme UI Bridge"

    override fun isSupported(): Boolean {
        val m = Build.MANUFACTURER.lowercase()
        val b = Build.BRAND.lowercase()
        return m.contains("oppo") || m.contains("oneplus") || m.contains("realme") ||
                b.contains("oppo") || b.contains("oneplus") || b.contains("realme")
    }

    override fun getDiagnosticInfo(): String {
        return if (isSupported()) {
            "Active: Oplus Architecture Detected (${Build.BRAND})"
        } else {
            "Inactive: Non-Oplus Hardware"
        }
    }

    override fun applyExtensionPolicy(policyName: String, enabled: Boolean): Boolean {
        return isSupported()
    }

    /**
     * Creates intent to launch ColorOS background task permission manager.
     */
    fun createBackgroundPowerIntent(): Intent {
        val intent = Intent()
        intent.component = ComponentName(
            "com.coloros.safecenter",
            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}

