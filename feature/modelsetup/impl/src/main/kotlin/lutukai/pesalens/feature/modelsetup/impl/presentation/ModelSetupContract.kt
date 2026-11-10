package lutukai.pesalens.feature.modelsetup.impl.presentation

import androidx.compose.runtime.Immutable
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin

/** One state per `ModelStatus`; [origin] only decides where the screen goes once a mode is chosen. */
@Immutable
sealed interface ModelSetupUiState : UiState {
    val origin: ModelSetupOrigin

    data class NotDownloaded(override val origin: ModelSetupOrigin) : ModelSetupUiState
    data class ReadyToDownload(override val origin: ModelSetupOrigin) : ModelSetupUiState
    data class Downloading(override val origin: ModelSetupOrigin, val percent: Int) : ModelSetupUiState
    data class Verifying(override val origin: ModelSetupOrigin) : ModelSetupUiState
    data class Ready(override val origin: ModelSetupOrigin) : ModelSetupUiState
    data class Failed(override val origin: ModelSetupOrigin, val stoppedAtPercent: Int) : ModelSetupUiState
    data class Unsupported(override val origin: ModelSetupOrigin) : ModelSetupUiState
}

sealed interface ModelSetupIntent : UiIntent {
    data object Load : ModelSetupIntent
    data object CheckDevice : ModelSetupIntent
    data object Download : ModelSetupIntent
    data object Cancel : ModelSetupIntent
    data object Retry : ModelSetupIntent
    data object UseOnDevice : ModelSetupIntent
    data object Delete : ModelSetupIntent
    data object UseCloud : ModelSetupIntent
    data object Back : ModelSetupIntent
}

sealed interface ModelSetupEffect : UiEffect {
    data object Close : ModelSetupEffect
    data object OpenCloudConsent : ModelSetupEffect
    data object ContinueOnboarding : ModelSetupEffect
}
