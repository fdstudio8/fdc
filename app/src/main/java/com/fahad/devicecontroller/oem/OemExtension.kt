package com.fahad.devicecontroller.oem

import com.fahad.devicecontroller.core.model.OemBrand

/**
 * Universal interface for manufacturer-specific hardware & firmware extensions.
 * Extensions provide supplemental features without locking core DPC behavior.
 */
interface OemExtension {
    val brand: OemBrand
    val extensionName: String

    /**
     * Checks if the vendor extension APIs are actually present and callable on this device.
     */
    fun isSupported(): Boolean

    /**
     * Returns diagnostic metadata regarding vendor version and capabilities.
     */
    fun getDiagnosticInfo(): String

    /**
     * Applies an OEM-specific enterprise policy if supported.
     */
    fun applyExtensionPolicy(policyName: String, enabled: Boolean): Boolean
}

