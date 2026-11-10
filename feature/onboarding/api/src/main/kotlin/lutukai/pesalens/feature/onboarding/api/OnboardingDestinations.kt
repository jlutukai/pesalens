package lutukai.pesalens.feature.onboarding.api

import kotlinx.serialization.Serializable
import lutukai.pesalens.core.navigation.PesaDestination

@Serializable
data object Welcome : PesaDestination

/** Cloud AI consent sheet. [origin] decides what happens once the user agrees. */
@Serializable
data class CloudConsent(val origin: ConsentOrigin) : PesaDestination

enum class ConsentOrigin { Welcome, ModelSetup, Settings, Assistant }
