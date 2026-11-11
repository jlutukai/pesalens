package lutukai.pesalens.feature.modelsetup.impl.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.core.ui.mvi.MviViewModel
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.R

class ModelSetupViewModel(
    private val origin: ModelSetupOrigin,
    private val models: ModelManager,
    private val settings: SettingsRepository,
    private val messages: UserMessageSink,
) : MviViewModel<ModelSetupUiState, ModelSetupIntent, ModelSetupEffect>(ModelSetupUiState.NotDownloaded(origin)) {

    private var loadJob: Job? = null
    private var checkJob: Job? = null

    /** Set once the screen is being left; later taps are ignored so a double tap navigates once. */
    private var finished = false

    override fun handleIntent(intent: ModelSetupIntent) = when (intent) {
        ModelSetupIntent.Load -> load()
        ModelSetupIntent.CheckDevice -> checkDevice()
        ModelSetupIntent.Download, ModelSetupIntent.Retry -> download()
        ModelSetupIntent.Cancel -> cancel()
        ModelSetupIntent.UseOnDevice -> useOnDevice()
        ModelSetupIntent.Delete -> delete()
        ModelSetupIntent.UseCloud -> useCloud()
        ModelSetupIntent.Back -> finish(ModelSetupEffect.Close)
    }

    /** Collects the model status once for the life of the ViewModel. */
    private fun load() {
        if (loadJob != null) return
        loadJob = models.status
            .onEach { status -> reduce { status.toUiState(origin) } }
            .launchIn(viewModelScope)
    }

    /** A success changes the status, which the collector above reflects; a failure leaves the state as it was. */
    private fun checkDevice() {
        if (checkJob?.isActive == true) return
        checkJob = viewModelScope.launch {
            if (models.checkDevice() is AppResult.Failure) messages.show(UiText.Res(R.string.model_message_unavailable))
        }
    }

    private fun download() {
        viewModelScope.launch { models.startDownload() }
    }

    private fun cancel() {
        viewModelScope.launch {
            models.cancelDownload()
            messages.show(UiText.Res(R.string.model_message_cancelled))
        }
    }

    private fun delete() {
        viewModelScope.launch {
            models.delete()
            settings.setAssistantMode(AssistantMode.Cloud)
            messages.show(UiText.Res(R.string.model_message_deleted))
        }
    }

    private fun useOnDevice() {
        if (finished) return
        finished = true
        viewModelScope.launch {
            settings.setAssistantMode(AssistantMode.OnDevice)
            messages.show(UiText.Res(R.string.model_message_on_device))
            emit(
                when (origin) {
                    ModelSetupOrigin.Welcome -> ModelSetupEffect.ContinueOnboarding
                    ModelSetupOrigin.Settings, ModelSetupOrigin.Assistant -> ModelSetupEffect.Close
                },
            )
        }
    }

    /** Consent is read from the repository inside the coroutine, so a tap before the first emission still branches correctly. */
    private fun useCloud() {
        if (finished) return
        viewModelScope.launch {
            if (settings.settings.first().cloudConsentGiven) {
                settings.setAssistantMode(AssistantMode.Cloud)
                finish(ModelSetupEffect.Close)
            } else {
                emit(ModelSetupEffect.OpenCloudConsent)
            }
        }
    }

    private fun finish(effect: ModelSetupEffect) {
        if (finished) return
        finished = true
        emit(effect)
    }
}

private fun ModelStatus.toUiState(origin: ModelSetupOrigin): ModelSetupUiState = when (this) {
    ModelStatus.NotDownloaded -> ModelSetupUiState.NotDownloaded(origin)
    ModelStatus.ReadyToDownload -> ModelSetupUiState.ReadyToDownload(origin)
    is ModelStatus.Downloading -> ModelSetupUiState.Downloading(origin, percent)
    ModelStatus.Verifying -> ModelSetupUiState.Verifying(origin)
    ModelStatus.Ready -> ModelSetupUiState.Ready(origin)
    is ModelStatus.Failed -> ModelSetupUiState.Failed(origin, stoppedAtPercent)
    ModelStatus.Unsupported -> ModelSetupUiState.Unsupported(origin)
}
