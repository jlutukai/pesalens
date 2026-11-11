package lutukai.pesalens.feature.onboarding.api

import kotlinx.serialization.Serializable
import lutukai.pesalens.core.navigation.PesaDestination

@Serializable
data object Welcome : PesaDestination

/** Cloud AI consent sheet. [origin] decides what happens once the user agrees. */
@Serializable
data class CloudConsent(val origin: ConsentOrigin) : PesaDestination

/**
 * Who opened the consent sheet. `Welcome` and `ModelSetup` (model setup opened from Welcome) are onboarding, where
 * agreeing continues it; model setup opened later from Settings or the assistant passes `Settings` or `Assistant`,
 * whose sheet just closes.
 */
enum class ConsentOrigin { Welcome, ModelSetup, Settings, Assistant }
