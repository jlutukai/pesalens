package lutukai.pesalens.feature.applock.impl

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import kotlin.test.assertEquals
import lutukai.pesalens.feature.applock.impl.biometric.unlockAuthenticators
import org.junit.Test

class UnlockAuthenticatorsTest {
    @Test
    fun api30AndUp_allowsStrongBiometricOrDeviceCredential() {
        assertEquals(BIOMETRIC_STRONG or DEVICE_CREDENTIAL, unlockAuthenticators(30))
        assertEquals(BIOMETRIC_STRONG or DEVICE_CREDENTIAL, unlockAuthenticators(37))
    }

    @Test
    fun beforeApi30_allowsWeakBiometricOrDeviceCredential() {
        assertEquals(BIOMETRIC_WEAK or DEVICE_CREDENTIAL, unlockAuthenticators(24))
        assertEquals(BIOMETRIC_WEAK or DEVICE_CREDENTIAL, unlockAuthenticators(29))
    }
}
