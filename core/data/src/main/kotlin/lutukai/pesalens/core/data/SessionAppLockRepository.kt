package lutukai.pesalens.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.PinCheck
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * App lock held for the life of the process. Only a salted SHA-256 hash of the PIN is kept, never the PIN itself.
 * Five consecutive wrong PINs start a 30 second cooldown during which every check, even a correct one, is refused.
 */
class SessionAppLockRepository(private val clock: Clock) : AppLockRepository {
    private class Credential(val salt: ByteArray, val hash: ByteArray)

    private class Snapshot(val state: AppLockState = AppLockState(), val credential: Credential? = null)

    private val random = SecureRandom()
    private val mutex = Mutex()
    private val snapshot = MutableStateFlow(Snapshot())

    override val state: Flow<AppLockState> = snapshot.map { it.state }

    override suspend fun setPin(pin: String) = mutex.withLock {
        val salt = ByteArray(SaltBytes).also(random::nextBytes)
        snapshot.value = Snapshot(AppLockState(method = LockMethod.Pin), Credential(salt, hash(salt, pin)))
    }

    override suspend fun enableBiometric() = mutex.withLock {
        snapshot.value = Snapshot(AppLockState(method = LockMethod.Biometric))
    }

    override suspend fun disable() = mutex.withLock {
        snapshot.value = Snapshot()
    }

    override suspend fun verifyPin(pin: String): PinCheck = mutex.withLock {
        val current = snapshot.value
        val now = clock.now()
        val cooldownEndsAt = current.state.cooldownEndsAt
        if (cooldownEndsAt != null && now < cooldownEndsAt) return PinCheck.CoolingDown(cooldownEndsAt)

        val credential = current.credential ?: return PinCheck.Wrong(MaxAttempts)
        if (MessageDigest.isEqual(credential.hash, hash(credential.salt, pin))) {
            snapshot.value = Snapshot(current.state.copy(failedAttempts = 0, cooldownEndsAt = null), credential)
            return PinCheck.Correct
        }

        // A wrong PIN after an expired cooldown starts a fresh run of attempts.
        val failed = (if (cooldownEndsAt != null) 0 else current.state.failedAttempts) + 1
        if (failed >= MaxAttempts) {
            val until = now + Cooldown
            snapshot.value = Snapshot(current.state.copy(failedAttempts = failed, cooldownEndsAt = until), credential)
            PinCheck.CoolingDown(until)
        } else {
            snapshot.value = Snapshot(current.state.copy(failedAttempts = failed, cooldownEndsAt = null), credential)
            PinCheck.Wrong(MaxAttempts - failed)
        }
    }

    private fun hash(salt: ByteArray, pin: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(salt + pin.toByteArray(Charsets.UTF_8))

    private companion object {
        const val SaltBytes = 16
        const val MaxAttempts = 5
        val Cooldown = 30.seconds
    }
}
