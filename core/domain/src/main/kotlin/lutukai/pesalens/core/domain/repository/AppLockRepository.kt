package lutukai.pesalens.core.domain.repository

import kotlinx.coroutines.flow.Flow
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.PinCheck

interface AppLockRepository {
    val state: Flow<AppLockState>

    suspend fun setPin(pin: String)

    suspend fun enableBiometric()

    suspend fun disable()

    suspend fun verifyPin(pin: String): PinCheck
}
