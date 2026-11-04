package com.fahad.devicecontroller.oem

import android.content.Context
import android.os.Build
import com.fahad.devicecontroller.core.model.OemBrand

/**
 * Clean reference extension for Google Pixel, Motorola, Nothing OS, and standard AOSP devices.
 * Uses pure public Android Enterprise APIs with zero vendor bloat.
 */
class GenericAospExtension(private val context: Context, override val brand: OemBrand) : OemExtension {

    override val extensionName: String = "Clean AOSP / Pixel Standard Reference"

    override fun isSupported(): Boolean = true

    override fun getDiagnosticInfo(): String {
        return "Active: Pure Android Enterprise Standard (${Build.MANUFACTURER} ${Build.MODEL})"
    }

    override fun applyExtensionPolicy(policyName: String, enabled: Boolean): Boolean = true
}

