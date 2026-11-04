package com.fahad.devicecontroller.core.remote

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.fahad.devicecontroller.core.policy.PersistentPolicyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class RemoteConfigState(
    val githubJsonUrl: String,
    val adminServerUrl: String,
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val lastSyncStatus: String = "Not synced yet",
    val configVersion: Int = 1
)

/**
 * GitHub JSON Remote Configuration & MAMP PRO Admin Console Sync Manager.
 * Inspired by MDMesh / Headwind MDM architecture.
 *
 * 1. Fetches configuration dynamically from a remote GitHub JSON URL.
 * 2. Reads the Admin Server URL (e.g. MAMP PRO dashboard endpoint).
 * 3. Ingests permanent policies, app restrictions, and anti-tamper flags.
 * 4. Dispatches device heartbeat to the MAMP PRO server.
 */
class GitHubRemoteConfigManager(
    private val context: Context,
    private val persistentPolicyManager: PersistentPolicyManager
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        RemoteConfigState(
            githubJsonUrl = prefs.getString(KEY_GITHUB_URL, DEFAULT_GITHUB_JSON_URL) ?: DEFAULT_GITHUB_JSON_URL,
            adminServerUrl = prefs.getString(KEY_ADMIN_SERVER_URL, DEFAULT_ADMIN_SERVER_URL) ?: DEFAULT_ADMIN_SERVER_URL,
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
            configVersion = prefs.getInt(KEY_CONFIG_VERSION, 1)
        )
    )
    val state: StateFlow<RemoteConfigState> = _state.asStateFlow()

    private var syncJob: Job? = null

    /**
     * Starts continuous background polling from GitHub JSON.
     * Checks for updates every [intervalSeconds] (default: 60s).
     */
    fun startContinuousSync(scope: CoroutineScope, intervalSeconds: Long = 60L) {
        syncJob?.cancel()
        syncJob = scope.launch(Dispatchers.IO) {
            Log.i(TAG, "Starting continuous GitHub sync loop (every ${intervalSeconds}s)...")
            while (isActive) {
                try {
                    syncFromGitHub()
                } catch (e: Throwable) {
                    Log.w(TAG, "Sync iteration failed: ${e.message}")
                }
                delay(intervalSeconds * 1000L)
            }
        }
    }

    fun stopContinuousSync() {
        syncJob?.cancel()
        syncJob = null
    }

    /**
     * Updates the remote GitHub JSON URL (requires admin authentication).
     */
    fun setGitHubJsonUrl(newUrl: String) {
        prefs.edit().putString(KEY_GITHUB_URL, newUrl.trim()).apply()
        _state.value = _state.value.copy(githubJsonUrl = newUrl.trim())
    }

    /**
     * Synchronizes configuration by fetching the GitHub JSON and applying policies.
     */
    suspend fun syncFromGitHub(): Boolean = withContext(Dispatchers.IO) {
        _state.value = _state.value.copy(isSyncing = true, lastSyncStatus = "Fetching remote config...")
        val targetUrl = _state.value.githubJsonUrl

        var content = fetchJsonFromUrl(targetUrl)
        var sourceName = "GitHub"

        // If primary GitHub URL returns 404 or fails, fallback to MAMP PRO server
        if (content == null) {
            val fallbackUrl = if (_state.value.adminServerUrl.endsWith("/")) {
                "${_state.value.adminServerUrl}config.json"
            } else {
                "${_state.value.adminServerUrl}/config.json"
            }
            if (fallbackUrl != targetUrl) {
                Log.i(TAG, "Primary GitHub URL failed, attempting fallback to local MAMP PRO: $fallbackUrl")
                content = fetchJsonFromUrl(fallbackUrl)
                if (content != null) {
                    sourceName = "MAMP PRO Admin"
                }
            }
        }

        if (content != null) {
            try {
                val json = JSONObject(content)
                parseAndApplyConfig(json)

                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_SYNC_TIME, now).apply()

                _state.value = _state.value.copy(
                    isSyncing = false,
                    lastSyncTimestamp = now,
                    lastSyncStatus = "Synced successfully with $sourceName"
                )

                // Dispatch heartbeat to the configured Admin Server (MAMP PRO)
                sendHeartbeatToMampServer()
                true
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to parse JSON config", e)
                _state.value = _state.value.copy(
                    isSyncing = false,
                    lastSyncStatus = "Invalid JSON structure: ${e.message}"
                )
                false
            }
        } else {
            _state.value = _state.value.copy(
                isSyncing = false,
                lastSyncStatus = "Failed to fetch from GitHub and MAMP server"
            )
            false
        }
    }

    private fun fetchJsonFromUrl(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "FDC-Enterprise-Client/2.0")

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val text = reader.use { it.readText() }
                conn.disconnect()
                text
            } else {
                conn.disconnect()
                Log.w(TAG, "HTTP $responseCode from $urlString")
                null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Network connection error for $urlString: ${e.message}")
            null
        }
    }

    private fun parseAndApplyConfig(json: JSONObject) {
        // 1. Parse Admin Server URL
        if (json.has("admin_server_url")) {
            val serverUrl = json.getString("admin_server_url").trim()
            if (serverUrl.isNotEmpty()) {
                prefs.edit().putString(KEY_ADMIN_SERVER_URL, serverUrl).apply()
                _state.value = _state.value.copy(adminServerUrl = serverUrl)
            }
        }

        // 2. Parse Config Version
        if (json.has("config_version")) {
            val ver = json.getInt("config_version")
            prefs.edit().putInt(KEY_CONFIG_VERSION, ver).apply()
            _state.value = _state.value.copy(configVersion = ver)
        }

        // 3. Permanent Mode flags
        if (json.has("permanent_lockdown")) {
            persistentPolicyManager.setPermanentLockdown(json.getBoolean("permanent_lockdown"))
        }

        if (json.has("anti_tamper_shield")) {
            persistentPolicyManager.setAntiTamperShield(json.getBoolean("anti_tamper_shield"))
        }

        if (json.has("kiosk_mode")) {
            persistentPolicyManager.setPermanentKiosk(json.getBoolean("kiosk_mode"))
        }

        if (json.has("disallow_factory_reset")) {
            persistentPolicyManager.setFactoryResetBlocked(json.getBoolean("disallow_factory_reset"))
        }

        if (json.has("disallow_safe_boot")) {
            persistentPolicyManager.setSafeBootBlocked(json.getBoolean("disallow_safe_boot"))
        }

        // 4. Parse Suspended Packages
        if (json.has("suspended_packages")) {
            val arr = json.getJSONArray("suspended_packages")
            val targetSet = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val pkg = arr.getString(i).trim()
                if (pkg.isNotEmpty()) {
                    targetSet.add(pkg)
                }
            }
            persistentPolicyManager.updateSuspendedPackages(targetSet)
        }

        // 5. Parse Hidden Packages
        if (json.has("hidden_packages")) {
            val arr = json.getJSONArray("hidden_packages")
            val targetSet = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val pkg = arr.getString(i).trim()
                if (pkg.isNotEmpty()) {
                    targetSet.add(pkg)
                }
            }
            persistentPolicyManager.updateHiddenPackages(targetSet)
        }

        Log.i(TAG, "GitHub configuration parsed and applied to PermanentPolicyManager.")
    }

    /**
     * Sends device telemetry and policy verification heartbeat to MAMP PRO server.
     */
    suspend fun sendHeartbeatToMampServer(): Boolean = withContext(Dispatchers.IO) {
        val serverBase = _state.value.adminServerUrl
        if (serverBase.isEmpty()) return@withContext false

        val endpoint = if (serverBase.endsWith("/")) "${serverBase}api/heartbeat.php" else "$serverBase/api/heartbeat.php"

        try {
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val payload = JSONObject().apply {
                put("manufacturer", Build.MANUFACTURER)
                put("model", Build.MODEL)
                put("android_version", Build.VERSION.RELEASE)
                put("api_level", Build.VERSION.SDK_INT)
                put("device_fingerprint", Build.FINGERPRINT)
                put("permanent_lockdown", persistentPolicyManager.config.value.isPermanentLockdownEnabled)
                put("anti_tamper_active", persistentPolicyManager.config.value.isAntiTamperShieldEnabled)
                put("suspended_count", persistentPolicyManager.config.value.suspendedPackages.size)
                put("hidden_count", persistentPolicyManager.config.value.hiddenPackages.size)
                put("timestamp", System.currentTimeMillis())
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            val code = conn.responseCode
            conn.disconnect()
            code == HttpURLConnection.HTTP_OK
        } catch (e: Throwable) {
            Log.w(TAG, "MAMP PRO Heartbeat dispatch skipped: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "GitHubRemoteConfig"
        private const val PREFS_NAME = "fdc_remote_config"
        private const val KEY_GITHUB_URL = "github_json_url"
        private const val KEY_ADMIN_SERVER_URL = "admin_server_url"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_CONFIG_VERSION = "config_version"

        /**
         * Default template GitHub JSON URL (Can be customized directly in the app or MAMP)
         */
        const val DEFAULT_GITHUB_JSON_URL =
            "https://raw.githubusercontent.com/fdstudio8/fdc/main/config.json"

        /**
         * Default local MAMP PRO Server URL on Mac
         */
        const val DEFAULT_ADMIN_SERVER_URL =
            "http://192.168.31.105/fdc-admin"
    }
}
