package lutukai.pesalens.feature.applock.api

import kotlinx.serialization.Serializable
import lutukai.pesalens.core.navigation.PesaDestination

@Serializable
data class AppLockSetup(val origin: LockSetupOrigin) : PesaDestination

enum class LockSetupOrigin { Onboarding, Settings }

@Serializable
data object Locked : PesaDestination
