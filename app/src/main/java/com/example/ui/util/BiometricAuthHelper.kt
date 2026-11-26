package com.example.ui.util

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

object BiometricAuthHelper {

    @Volatile
    var isPromptInProgress: Boolean = false
        private set

    private fun allowedAuthenticators(): Int {
        return BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }

    private fun hasDeviceScreenLock(context: Context): Boolean {
        return try {
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.isDeviceSecure == true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns true if the device supports biometric or device lock (PIN/pattern/password) auth.
     */
    fun canAuthenticate(context: Context): Boolean {
        val status = try {
            BiometricManager.from(context).canAuthenticate(allowedAuthenticators())
        } catch (_: Exception) {
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN
        }
        return status == BiometricManager.BIOMETRIC_SUCCESS || hasDeviceScreenLock(context)
    }

    /**
     * Unwraps [ContextWrapper]s (such as ModalBottomSheet/Dialog ContextThemeWrappers)
     * and launches Android's Biometric/Device Credential prompt on the host [FragmentActivity].
     */
    fun authenticateContext(
        context: Context,
        title: String,
        subtitle: String? = null,
        onSuccess: () -> Unit,
        onUnavailable: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val activity = context.findFragmentActivity()
        if (activity == null) {
            onError("Unable to open device authentication from this window")
            return
        }
        authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = onSuccess,
            onUnavailable = onUnavailable,
            onError = onError
        )
    }

    /**
     * Launches Android's unified Biometric/Device Credential prompt.
     * Invokes [onUnavailable] when no screen lock or biometrics are configured on the device.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        onSuccess: () -> Unit,
        onUnavailable: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!canAuthenticate(activity)) {
            onUnavailable()
            return
        }

        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val weakStatus = runCatching {
                BiometricManager.from(activity).canAuthenticate(allowedAuthenticators())
            }.getOrDefault(BiometricManager.BIOMETRIC_STATUS_UNKNOWN)
            if (weakStatus == BiometricManager.BIOMETRIC_SUCCESS) {
                allowedAuthenticators()
            } else {
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            }
        } else {
            allowedAuthenticators()
        }

        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isPromptInProgress = false
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        isPromptInProgress = false
                        val isUserDismiss = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode == BiometricPrompt.ERROR_CANCELED
                        if (!isUserDismiss && errString.isNotBlank()) {
                            onError(errString.toString())
                        }
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .apply {
                    if (!subtitle.isNullOrBlank()) {
                        setSubtitle(subtitle)
                    }
                }
                .setAllowedAuthenticators(authenticators)
                .build()

            isPromptInProgress = true
            prompt.authenticate(promptInfo)
        } catch (e: Exception) {
            isPromptInProgress = false
            onError(e.localizedMessage ?: "Authentication unavailable")
        }
    }
}
