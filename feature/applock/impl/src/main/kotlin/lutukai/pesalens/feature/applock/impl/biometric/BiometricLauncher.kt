package lutukai.pesalens.feature.applock.impl.biometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Shows the system biometric prompt and reports what happened as a [BiometricOutcome]. */
class BiometricLauncher(private val activity: FragmentActivity) {

    /** Setup needs an enrolled strong biometric, since the prompt's negative button is the PIN route. */
    fun canUseBiometricsForSetup(): Boolean =
        BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    /** Whether the unlock prompt can run: asks for exactly the authenticators [launchUnlock] will use. */
    fun canUnlock(): Boolean =
        BiometricManager.from(activity).canAuthenticate(unlockAuthenticators(Build.VERSION.SDK_INT)) ==
            BiometricManager.BIOMETRIC_SUCCESS

    /** Strong biometrics only; the negative button lets the user choose a PIN instead. */
    fun launchSetup(title: String, subtitle: String, usePinLabel: String, onResult: (BiometricOutcome) -> Unit) {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText(usePinLabel)
            .build()
        prompt(onResult).authenticate(info)
    }

    /** Biometrics or the device credential. With a device credential allowed, no negative button is set. */
    fun launchUnlock(title: String, onResult: (BiometricOutcome) -> Unit) {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setAllowedAuthenticators(unlockAuthenticators(Build.VERSION.SDK_INT))
            .build()
        prompt(onResult).authenticate(info)
    }

    private fun prompt(onResult: (BiometricOutcome) -> Unit) = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                onResult(BiometricOutcome.Succeeded)

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) =
                onResult(errorCode.toOutcome())
        },
    )

    private fun Int.toOutcome(): BiometricOutcome = when (this) {
        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricOutcome.UsePinInstead
        BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_CANCELED -> BiometricOutcome.Cancelled
        BiometricPrompt.ERROR_NO_BIOMETRICS,
        BiometricPrompt.ERROR_HW_NOT_PRESENT,
        BiometricPrompt.ERROR_HW_UNAVAILABLE,
        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
        -> BiometricOutcome.NotAvailable
        else -> BiometricOutcome.Error
    }
}

/**
 * Authenticators for the unlock prompt. Android 11+ can combine strong biometrics with the device credential; older
 * releases only support that combination with weak biometrics (see `BiometricManager.Authenticators`).
 */
fun unlockAuthenticators(sdkInt: Int): Int =
    if (sdkInt >= Build.VERSION_CODES.R) BIOMETRIC_STRONG or DEVICE_CREDENTIAL else BIOMETRIC_WEAK or DEVICE_CREDENTIAL
