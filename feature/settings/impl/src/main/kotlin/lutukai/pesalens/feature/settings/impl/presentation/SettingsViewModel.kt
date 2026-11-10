package lutukai.pesalens.feature.settings.impl.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.domain.repository.ConversationSession
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.model.ThemePreference
import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.core.ui.mvi.MviViewModel
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.settings.impl.R

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val lock: AppLockRepository,
    private val models: ModelManager,
    private val finance: FinanceRepository,
    private val conversation: ConversationSession,
    private val messages: UserMessageSink,
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(SettingsUiState()) {

    private var loadJob: Job? = null
    private val systemDark = MutableStateFlow(false)
    private var closed = false

    override fun handleIntent(intent: SettingsIntent) = when (intent) {
        is SettingsIntent.Load -> load(intent.systemDark)
        is SettingsIntent.SetMode -> setMode(intent.mode)
        is SettingsIntent.SetTheme -> setTheme(intent.dark)
        SettingsIntent.OpenModel -> emit(SettingsEffect.OpenModel)
        SettingsIntent.OpenAppLock -> emit(SettingsEffect.OpenAppLock)
        SettingsIntent.AskClear -> reduce { copy(dialog = SettingsDialog.ClearConversation) }
        SettingsIntent.AskReset -> reduce { copy(dialog = SettingsDialog.ResetSampleData) }
        SettingsIntent.Dismiss -> reduce { copy(dialog = SettingsDialog.None) }
        SettingsIntent.ConfirmClear -> confirmClear()
        SettingsIntent.ConfirmReset -> confirmReset()
        SettingsIntent.Back -> close()
    }

    /** Collects the three sources once for the life of the ViewModel; a later Load only updates the system theme. */
    private fun load(dark: Boolean) {
        systemDark.value = dark
        if (loadJob != null) return
        loadJob = combine(settings.settings, lock.state, models.status, systemDark) { app, lockState, status, sysDark ->
            { state: SettingsUiState ->
                state.copy(
                    mode = app.assistantMode,
                    onDeviceEnabled = status is ModelStatus.Ready,
                    modelStatus = status,
                    lockMethod = lockState.method,
                    theme = app.theme,
                    resolvedDark = if (app.theme == ThemePreference.System) sysDark else app.theme == ThemePreference.Dark,
                )
            }
        }
            .onEach { apply -> reduce { apply(this) } }
            .launchIn(viewModelScope)
    }

    /** Consent is read from the repository, not state, so a tap before the first emission still branches correctly. */
    private fun setMode(mode: AssistantMode) {
        when (mode) {
            AssistantMode.Cloud -> viewModelScope.launch {
                if (settings.settings.first().cloudConsentGiven) {
                    settings.setAssistantMode(AssistantMode.Cloud)
                    messages.show(UiText.Res(R.string.settings_message_cloud))
                } else {
                    emit(SettingsEffect.OpenCloudConsent)
                }
            }
            AssistantMode.OnDevice -> {
                if (!currentState.onDeviceEnabled) return
                viewModelScope.launch {
                    settings.setAssistantMode(AssistantMode.OnDevice)
                    messages.show(UiText.Res(R.string.settings_message_on_device))
                }
            }
        }
    }

    private fun setTheme(dark: Boolean) {
        viewModelScope.launch { settings.setTheme(if (dark) ThemePreference.Dark else ThemePreference.Light) }
    }

    /** The dialog closes before the work starts, so a second tap on the confirm button finds no dialog and does nothing. */
    private fun confirmClear() {
        if (currentState.dialog != SettingsDialog.ClearConversation) return
        reduce { copy(dialog = SettingsDialog.None) }
        viewModelScope.launch {
            conversation.clear()
            messages.show(UiText.Res(R.string.settings_message_cleared))
        }
    }

    private fun confirmReset() {
        if (currentState.dialog != SettingsDialog.ResetSampleData) return
        reduce { copy(dialog = SettingsDialog.None) }
        viewModelScope.launch {
            when (finance.resetSampleData()) {
                is AppResult.Success -> {
                    conversation.clear()
                    messages.show(UiText.Res(R.string.settings_message_reset))
                }
                is AppResult.Failure -> messages.show(UiText.Res(R.string.settings_message_reset_failed))
            }
        }
    }

    /** One navigation effect; further taps (double tap) are ignored. */
    private fun close() {
        if (closed) return
        closed = true
        emit(SettingsEffect.Close)
    }
}
