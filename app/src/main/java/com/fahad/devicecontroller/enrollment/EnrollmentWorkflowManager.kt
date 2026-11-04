package com.fahad.devicecontroller.enrollment

import android.content.Context
import org.json.JSONObject

/**
 * Official Android Enterprise Enrollment Workflows & Provisioning Generator.
 * Delivers transparent, guided provisioning without silent exploits or false claims.
 */
class EnrollmentWorkflowManager(private val context: Context) {

    /**
     * Exact ADB command required to provision FDC as the Device Owner.
     */
    val adbCommand: String =
        "adb shell dpm set-device-owner com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver"

    /**
     * Generates standard Android Enterprise QR Code Provisioning JSON bundle.
     */
    fun generateQrProvisioningPayload(apkDownloadUrl: String, apkSha256Checksum: String): String {
        val json = JSONObject()
        json.put("android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME", "com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver")
        json.put("android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION", apkDownloadUrl)
        json.put("android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM", apkSha256Checksum)
        json.put("android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED", true)
        json.put("android.app.extra.PROVISIONING_SKIP_ENCRYPTION", false)
        return json.toString(2)
    }

    /**
     * Returns structured, human-readable troubleshooting guidance for enrollment errors.
     */
    fun getTroubleshootingAdvice(errorCode: String): String {
        return when {
            errorCode.contains("already some accounts", ignoreCase = true) ->
                "Account Conflict: Android Enterprise forbids setting Device Owner if any user or Google accounts are active. Navigate to Android Settings -> Accounts and remove all accounts, or factory reset the device."
            errorCode.contains("already a device owner", ignoreCase = true) ->
                "Existing DPC Conflict: The device is already managed by another MDM or company profile. Deprovision the existing manager or wipe the device."
            errorCode.contains("unknown component", ignoreCase = true) ->
                "Installation Error: The FDC APK is not installed on the primary user space. Install the APK via 'adb install fdc.apk' before running the dpm command."
            else ->
                "Check USB Debugging in Developer Options and verify the device is recognized via 'adb devices'."
        }
    }
}

