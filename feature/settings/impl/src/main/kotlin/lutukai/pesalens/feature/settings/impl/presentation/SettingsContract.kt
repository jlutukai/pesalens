package lutukai.pesalens.feature.settings.impl.presentation

import androidx.compose.runtime.Immutable
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.model.ThemePreference
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState

/** [resolvedDark] is what the theme segment shows: the system setting while [theme] is System. */
@Immutable
data class SettingsUiState(
    val mode: AssistantMode = AssistantMode.Cloud,
    val onDeviceEnabled: Boolean = false,
    val modelStatus: ModelStatus = ModelStatus.NotDownloaded,
    val lockMethod: LockMethod = LockMethod.None,
    val theme: ThemePreference = ThemePreference.System,
    val resolvedDark: Boolean = false,
    val dialog: SettingsDialog = SettingsDialog.None,
) : UiState

sealed interface SettingsDialog {
    data object None : SettingsDialog
    data object ClearConversation : SettingsDialog
    data object ResetSampleData : SettingsDialog
}

sealed interface SettingsIntent : UiIntent {
    data class Load(val systemDark: Boolean) : SettingsIntent
    data class SetMode(val mode: AssistantMode) : SettingsIntent
    data class SetTheme(val dark: Boolean) : SettingsIntent
    data object OpenModel : SettingsIntent
    data object OpenAppLock : SettingsIntent
    data object AskClear : SettingsIntent
    data object AskReset : SettingsIntent
    data object Dismiss : SettingsIntent
    data object ConfirmClear : SettingsIntent
    data object ConfirmReset : SettingsIntent
    data object Back : SettingsIntent
}

sealed interface SettingsEffect : UiEffect {
    data object OpenModel : SettingsEffect
    data object OpenAppLock : SettingsEffect
    data object OpenCloudConsent : SettingsEffect
    data object Close : SettingsEffect
}
