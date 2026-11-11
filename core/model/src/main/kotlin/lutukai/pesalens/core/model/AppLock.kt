package lutukai.pesalens.core.model

import kotlin.time.Instant

enum class LockMethod { None, Pin, Biometric }

data class AppLockState(
    val method: LockMethod = LockMethod.None,
    val failedAttempts: Int = 0,
    val cooldownEndsAt: Instant? = null,
)

sealed interface PinCheck {
    data object Correct : PinCheck

    data class Wrong(val attemptsBeforeCooldown: Int) : PinCheck

    data class CoolingDown(val until: Instant) : PinCheck
}
