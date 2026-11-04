package com.fahad.devicecontroller.core.model

import android.os.Build

/**
 * Recognized Android manufacturers and custom OEM software layers.
 */
enum class OemBrand(val displayName: String, val skinName: String) {
    SAMSUNG("Samsung", "One UI"),
    GOOGLE("Google Pixel", "Stock Pixel Android"),
    XIAOMI("Xiaomi / POCO / Redmi", "HyperOS / MIUI"),
    OPPO("OPPO", "ColorOS"),
    ONEPLUS("OnePlus", "OxygenOS"),
    REALME("Realme", "Realme UI"),
    VIVO("Vivo / iQOO", "Funtouch OS / OriginOS"),
    NOTHING("Nothing", "Nothing OS"),
    MOTOROLA("Motorola", "MyUX / Hello UI"),
    GENERIC("Generic Android", "AOSP");

    companion object {
        fun detect(): OemBrand {
            val manufacturer = Build.MANUFACTURER.lowercase()
            val brand = Build.BRAND.lowercase()

            return when {
                manufacturer.contains("samsung") || brand.contains("samsung") -> SAMSUNG
                manufacturer.contains("google") || brand.contains("google") -> GOOGLE
                manufacturer.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> XIAOMI
                manufacturer.contains("oppo") || brand.contains("oppo") -> OPPO
                manufacturer.contains("oneplus") || brand.contains("oneplus") -> ONEPLUS
                manufacturer.contains("realme") || brand.contains("realme") -> REALME
                manufacturer.contains("vivo") || brand.contains("iqoo") -> VIVO
                manufacturer.contains("nothing") || brand.contains("nothing") -> NOTHING
                manufacturer.contains("motorola") || brand.contains("moto") -> MOTOROLA
                else -> GENERIC
            }
        }
    }
}

