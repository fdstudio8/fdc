package com.fahad.devicecontroller.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fahad.devicecontroller.FdcApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Autonomic Boot Receiver.
 * Executes immediately upon phone reboot (ACTION_BOOT_COMPLETED) to ensure
 * all permanent policies, app suspensions, hiding, and anti-bypass restrictions
 * are instantly re-applied and active forever.
 */
class FdcBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.i(TAG, "Device boot completed detected. Re-applying all permanent policies...")

            try {
                val app = context.applicationContext as? FdcApplication
                app?.persistentPolicyManager?.reapplyAllPolicies()
                Log.i(TAG, "Permanent policies successfully restored on reboot.")

                // Trigger immediate GitHub sync upon network recovery
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        app?.gitHubRemoteConfigManager?.syncFromGitHub()
                    } catch (e: Throwable) {
                        Log.w(TAG, "Boot GitHub sync deferred: ${e.message}")
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to restore permanent policies on boot", e)
            }
        }
    }

    companion object {
        private const val TAG = "FdcBootReceiver"
    }
}

