package com.spendsee.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest

class PasscodeManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "passcode_prefs"
        private const val KEY_PASSCODE_HASH = "passcode_hash"
        private const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        @Volatile
        private var instance: PasscodeManager? = null

        fun getInstance(context: Context): PasscodeManager {
            return instance ?: synchronized(this) {
                instance ?: PasscodeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Check if passcode is enabled
     */
    fun isPasscodeEnabled(): Boolean {
        return prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
    }

    /**
     * Check if passcode has been set
     */
    fun hasPasscode(): Boolean {
        return prefs.getString(KEY_PASSCODE_HASH, null) != null
    }

    /**
     * Check if biometric authentication is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) && isBiometricAvailable()
    }

    /**
     * Check if biometric hardware is available on the device
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Save a new passcode
     */
    fun savePasscode(passcode: String) {
        val hash = hashPasscode(passcode)
        prefs.edit()
            .putString(KEY_PASSCODE_HASH, hash)
            .putBoolean(KEY_PASSCODE_ENABLED, true)
            .apply()
    }

    /**
     * Validate passcode
     */
    fun validatePasscode(passcode: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSCODE_HASH, null) ?: return false
        return hashPasscode(passcode) == storedHash
    }

    /**
     * Enable/disable passcode protection
     */
    fun setPasscodeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, enabled).apply()
    }

    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Delete passcode and disable protection
     */
    fun deletePasscode() {
        prefs.edit()
            .remove(KEY_PASSCODE_HASH)
            .putBoolean(KEY_PASSCODE_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    /**
     * Change existing passcode
     */
    fun changePasscode(oldPasscode: String, newPasscode: String): Boolean {
        if (!validatePasscode(oldPasscode)) {
            return false
        }
        savePasscode(newPasscode)
        return true
    }

    /**
     * Show biometric authentication prompt
     */
    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> onCancel()
                        else -> onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed. Please try again.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock SpendSee")
            .setSubtitle("Use biometric authentication to unlock")
            .setNegativeButtonText("Use Passcode")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Hash passcode using SHA-256
     */
    private fun hashPasscode(passcode: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(passcode.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
