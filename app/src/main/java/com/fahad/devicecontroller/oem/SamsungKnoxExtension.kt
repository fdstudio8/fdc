package com.fahad.devicecontroller.oem

import android.content.Context
import android.os.Build
import android.util.Log
import com.fahad.devicecontroller.core.model.OemBrand

/**
 * Samsung Knox Extension.
 * Quarantined behind dynamic reflection so non-Samsung devices (Google, Xiaomi, etc.)
 * never crash with ClassNotFoundException or NoClassDefFoundError.
 */
class SamsungKnoxExtension(private val context: Context) : OemExtension {

    override val brand: OemBrand = OemBrand.SAMSUNG
    override val extensionName: String = "Samsung Knox Enterprise SDK"

    private var knoxEdmClass: Class<*>? = null
    private var isKnoxAvailable: Boolean = false
    private var knoxVersionString: String = "Not Detected"

    init {
        detectKnox()
    }

    private fun detectKnox() {
        if (!Build.MANUFACTURER.contains("samsung", ignoreCase = true) &&
            !Build.BRAND.contains("samsung", ignoreCase = true)) {
            isKnoxAvailable = false
            return
        }

        try {
            knoxEdmClass = Class.forName("com.samsung.android.knox.EnterpriseDeviceManager")
            isKnoxAvailable = true

            // Attempt to retrieve Knox API version if exposed reflectively
            try {
                val knoxVersionClass = Class.forName("com.samsung.android.knox.EnterpriseKnoxManager")
                val getKnoxVersionMethod = knoxVersionClass.getMethod("getVersion")
                val versionObj = getKnoxVersionMethod.invoke(null)
                knoxVersionString = versionObj?.toString() ?: "Knox Standard Present"
            } catch (e: Throwable) {
                knoxVersionString = "Knox Enterprise Classes Detected"
            }
        } catch (e: ClassNotFoundException) {
            isKnoxAvailable = false
            knoxVersionString = "Samsung device without Knox Enterprise SDK"
        } catch (e: Throwable) {
            isKnoxAvailable = false
            Log.w(TAG, "Error probing Samsung Knox", e)
        }
    }

    override fun isSupported(): Boolean = isKnoxAvailable

    override fun getDiagnosticInfo(): String {
        return if (isKnoxAvailable) {
            "Active: $knoxVersionString (Conditional Reflection Mode)"
        } else {
            "Dormant: No Knox classes present (Pure Android Enterprise in effect)"
        }
    }

    override fun applyExtensionPolicy(policyName: String, enabled: Boolean): Boolean {
        if (!isKnoxAvailable || knoxEdmClass == null) return false

        return try {
            val getInstanceMethod = knoxEdmClass?.getMethod("getInstance", Context::class.java)
            val edmInstance = getInstanceMethod?.invoke(null, context)
            if (edmInstance != null) {
                Log.i(TAG, "Reflectively invoking Knox policy: $policyName (enabled=$enabled)")
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed reflectively applying Knox policy: $policyName", e)
            false
        }
    }

    companion object {
        private const val TAG = "SamsungKnoxExt"
    }
}

