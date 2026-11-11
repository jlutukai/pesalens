package lutukai.pesalens.core.testing.fake

import kotlinx.coroutines.flow.MutableStateFlow
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.PinCheck

/** Scripted lock: [verifyPin] answers [nextCheck] and records the PIN it was asked about in [pins]. */
class FakeAppLockRepository(initial: AppLockState = AppLockState()) : AppLockRepository {
    val stateFlow = MutableStateFlow(initial)
    var nextCheck: PinCheck = PinCheck.Correct

    private val _pins = mutableListOf<String>()
    val pins: List<String> get() = _pins

    private val _checkedPins = mutableListOf<String>()
    val checkedPins: List<String> get() = _checkedPins

    var biometricEnabledCount: Int = 0
        private set
    var disableCount: Int = 0
        private set

    override val state = stateFlow

    override suspend fun setPin(pin: String) {
        _pins += pin
        stateFlow.value = AppLockState(method = LockMethod.Pin)
    }

    override suspend fun enableBiometric() {
        biometricEnabledCount++
        stateFlow.value = AppLockState(method = LockMethod.Biometric)
    }

    override suspend fun disable() {
        disableCount++
        stateFlow.value = AppLockState()
    }

    override suspend fun verifyPin(pin: String): PinCheck {
        _checkedPins += pin
        return nextCheck
    }
}
