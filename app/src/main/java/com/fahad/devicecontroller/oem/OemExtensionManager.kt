package com.fahad.devicecontroller.oem

import android.content.Context
import com.fahad.devicecontroller.core.model.OemBrand

/**
 * Universal OEM Extension Orchestrator.
 * Dynamically resolves vendor-specific extensions without hard linking.
 */
class OemExtensionManager(private val context: Context) {

    val detectedBrand: OemBrand = OemBrand.detect()

    val activeExtension: OemExtension = when (detectedBrand) {
        OemBrand.SAMSUNG -> SamsungKnoxExtension(context)
        OemBrand.XIAOMI -> XiaomiHyperOsExtension(context)
        OemBrand.OPPO, OemBrand.ONEPLUS, OemBrand.REALME -> OppoRealmeExtension(context)
        OemBrand.VIVO -> VivoExtension(context)
        OemBrand.GOOGLE, OemBrand.NOTHING, OemBrand.MOTOROLA, OemBrand.GENERIC -> GenericAospExtension(context, detectedBrand)
    }

    /**
     * Checks if the active extension provides additional hardware features.
     */
    fun hasActiveOemExtension(): Boolean {
        return activeExtension.isSupported() && detectedBrand != OemBrand.GENERIC
    }

    /**
     * Returns diagnostic description for the compatibility report.
     */
    fun getDiagnosticSummary(): String {
        return "${detectedBrand.displayName} [${detectedBrand.skinName}] — ${activeExtension.getDiagnosticInfo()}"
    }
}

