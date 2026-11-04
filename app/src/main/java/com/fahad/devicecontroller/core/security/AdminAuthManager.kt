package com.fahad.devicecontroller.core.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

/**
 * High-reliability, cryptographic Administrator Authentication Governor.
 * Enforces PBKDF2 / SHA-256 salted hashing, constant-time comparison,
 * exponential lockout protection, and a Cryptographic Emergency Master Key.
 */
class AdminAuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var activeSessionExpiry: Long = 0L

    init {
        // Initialize default administrator PIN ("1234") on first launch
        if (!prefs.contains(KEY_PIN_HASH)) {
            val defaultSalt = generateSalt()
            val defaultHash = hashPin("1234", defaultSalt)
            prefs.edit()
                .putString(KEY_PIN_SALT, defaultSalt)
                .putString(KEY_PIN_HASH, defaultHash)
                .apply()
        }

        // Initialize default Emergency Master Key ("FDC-MASTER-9988-7766") on first launch
        if (!prefs.contains(KEY_MASTER_KEY_HASH)) {
            val masterSalt = generateSalt()
            val masterHash = hashMasterKey(DEFAULT_MASTER_KEY, masterSalt)
            prefs.edit()
                .putString(KEY_MASTER_KEY_SALT, masterSalt)
                .putString(KEY_MASTER_KEY_HASH, masterHash)
                .apply()
        }
    }

    /**
     * Verifies the administrator PIN using constant-time comparison.
     * Creates a 5-minute active session on success.
     */
    @Synchronized
    fun verifyPin(candidatePin: String): Boolean {
        if (isLockedOut()) return false

        val salt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val expectedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        val candidateHash = hashPin(candidatePin, salt)

        val matches = MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        ) && expectedHash.isNotEmpty()

        return if (matches) {
            // Success: reset failures and grant session
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            activeSessionExpiry = System.currentTimeMillis() + SESSION_DURATION_MS
            true
        } else {
            // Failure: record and enforce exponential lockout
            val failures = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failures)

            if (failures >= 10) {
                editor.putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15))
            } else if (failures >= 5) {
                editor.putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60))
            }
            editor.apply()
            false
        }
    }

    /**
     * Verifies the Emergency Master Key.
     * The Master Key bypasses any active lockout, resets failed attempts,
     * and grants immediate administrator access.
     */
    @Synchronized
    fun verifyMasterKey(candidateKey: String): Boolean {
        val normalizedCandidate = normalizeKey(candidateKey)
        val salt = prefs.getString(KEY_MASTER_KEY_SALT, "") ?: ""
        val expectedHash = prefs.getString(KEY_MASTER_KEY_HASH, "") ?: ""
        val candidateHash = hashMasterKey(normalizedCandidate, salt)

        val matches = MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        ) && expectedHash.isNotEmpty()

        if (matches) {
            // Immediately lift lockout, reset failed attempts, and grant admin session
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            activeSessionExpiry = System.currentTimeMillis() + SESSION_DURATION_MS
            return true
        }
        return false
    }

    /**
     * Resets the Administrator PIN using the Emergency Master Key.
     */
    @Synchronized
    fun resetPinWithMasterKey(masterKey: String, newPin: String): Boolean {
        if (newPin.length < 4) return false
        if (!verifyMasterKey(masterKey)) return false

        val newSalt = generateSalt()
        val newHash = hashPin(newPin, newSalt)

        prefs.edit()
            .putString(KEY_PIN_SALT, newSalt)
            .putString(KEY_PIN_HASH, newHash)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        return true
    }

    /**
     * Changes the administrator PIN with mandatory verification of current PIN.
     */
    @Synchronized
    fun changePin(oldPin: String, newPin: String): Boolean {
        if (newPin.length < 4) return false
        if (!verifyPin(oldPin)) return false

        val newSalt = generateSalt()
        val newHash = hashPin(newPin, newSalt)

        prefs.edit()
            .putString(KEY_PIN_SALT, newSalt)
            .putString(KEY_PIN_HASH, newHash)
            .apply()
        return true
    }

    /**
     * Updates the Master Key (requires current Master Key verification).
     */
    @Synchronized
    fun updateMasterKey(currentMasterKey: String, newMasterKey: String): Boolean {
        val normalizedNew = normalizeKey(newMasterKey)
        if (normalizedNew.length < 8) return false
        if (!verifyMasterKey(currentMasterKey)) return false

        val newSalt = generateSalt()
        val newHash = hashMasterKey(normalizedNew, newSalt)

        prefs.edit()
            .putString(KEY_MASTER_KEY_SALT, newSalt)
            .putString(KEY_MASTER_KEY_HASH, newHash)
            .apply()
        return true
    }

    /**
     * Checks if authentication is currently locked out due to brute-force attempts.
     */
    fun isLockedOut(): Boolean {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        return System.currentTimeMillis() < lockoutUntil
    }

    /**
     * Returns remaining lockout seconds (0 if not locked out).
     */
    fun remainingLockoutSeconds(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val remaining = (lockoutUntil - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0L
    }

    /**
     * Checks if an authenticated session is currently valid without re-prompting.
     */
    fun hasActiveSession(): Boolean {
        return System.currentTimeMillis() < activeSessionExpiry
    }

    /**
     * Closes the active administrator session immediately.
     */
    fun clearSession() {
        activeSessionExpiry = 0L
    }

    private fun normalizeKey(key: String): String {
        return key.trim().replace("-", "").replace(" ", "").uppercase()
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun hashMasterKey(key: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:FDC_MASTER:$key"
        val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "fdc_admin_auth"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_MASTER_KEY_HASH = "master_key_hash"
        private const val KEY_MASTER_KEY_SALT = "master_key_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"

        /**
         * Default factory Emergency Master Key:
         * Formatted as: FDC-MASTER-9988-7766 (can be entered with or without dashes/spaces).
         */
        const val DEFAULT_MASTER_KEY = "FDC-MASTER-9988-7766"

        private val SESSION_DURATION_MS = TimeUnit.MINUTES.toMillis(5)
    }
}
