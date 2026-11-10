package lutukai.pesalens.feature.applock.impl.biometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import lutukai.pesalens.feature.applock.impl.R

/** Shows the system biometric prompt and reports what happened as a [BiometricOutcome]. */
class BiometricLauncher(private val activity: FragmentActivity) {

    fun canUseBiometrics(): Boolean =
        BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

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

    /**
     * Strong biometrics or the device credential. Android 9 and 10 cannot combine the two, so there the prompt is
     * biometrics only with a Cancel button.
     */
    fun launchUnlock(title: String, onResult: (BiometricOutcome) -> Unit) {
        val builder = BiometricPrompt.PromptInfo.Builder().setTitle(title)
        val combined = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        if (combined) {
            builder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            builder.setAllowedAuthenticators(BIOMETRIC_STRONG).setNegativeButtonText(activity.getString(R.string.applock_cancel))
        }
        prompt(onResult).authenticate(builder.build())
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
