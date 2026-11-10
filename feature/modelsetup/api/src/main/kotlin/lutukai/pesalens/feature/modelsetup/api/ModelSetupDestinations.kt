package lutukai.pesalens.feature.modelsetup.api

import kotlinx.serialization.Serializable
import lutukai.pesalens.core.navigation.PesaDestination

@Serializable
data class ModelSetup(val origin: ModelSetupOrigin) : PesaDestination

enum class ModelSetupOrigin { Welcome, Settings, Assistant }
