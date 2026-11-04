package com.fahad.devicecontroller.admin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fahad.devicecontroller.MainActivity

/**
 * Optional Dialer Secret Code Receiver.
 * Triggers admin interface when *#*#1234#*#* is entered into the dialer on supported devices.
 * Dynamically wrapped to prevent failure on devices/tablets without telephony stacks.
 */
class FdcDialerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == "android.provider.Telephony.SECRET_CODE") {
            val uri = intent.data
            val host = uri?.host ?: ""
            Log.i(TAG, "Telephony secret code triggered: host=$host")

            if (host == "1234" || host.isEmpty()) {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(MainActivity.EXTRA_TRIGGER_ADMIN_AUTH, true)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        private const val TAG = "FdcDialerReceiver"
    }
}

