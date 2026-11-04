package com.fahad.devicecontroller.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fahad.devicecontroller.core.model.OemBrand

/**
 * Vivo / iQOO Funtouch OS & OriginOS Extension.
 * Handles Vivo iManager background high power consumption exemptions.
 */
class VivoExtension(private val context: Context) : OemExtension {

    override val brand: OemBrand = OemBrand.VIVO
    override val extensionName: String = "Vivo Funtouch / OriginOS Bridge"

    override fun isSupported(): Boolean {
        val m = Build.MANUFACTURER.lowercase()
        val b = Build.BRAND.lowercase()
        return m.contains("vivo") || b.contains("iqoo") || m.contains("iqoo")
    }

    override fun getDiagnosticInfo(): String {
        return if (isSupported()) {
            "Active: Vivo iManager Ecosystem Detected"
        } else {
            "Inactive: Non-Vivo Hardware"
        }
    }

    override fun applyExtensionPolicy(policyName: String, enabled: Boolean): Boolean {
        return isSupported()
    }

    /**
     * Creates intent to launch Vivo High Background Power Consumption settings.
     */
    fun createHighBackgroundPowerIntent(): Intent {
        val intent = Intent()
        intent.component = ComponentName(
            "com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.PurviewTabActivity"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}

