package lutukai.pesalens.feature.settings.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.model.ThemePreference
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.fake.FakeAppLockRepository
import lutukai.pesalens.core.testing.fake.FakeConversationSession
import lutukai.pesalens.core.testing.fake.FakeFinanceRepository
import lutukai.pesalens.core.testing.fake.FakeModelManager
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.core.testing.fake.FakeUserMessageSink
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.settings.impl.presentation.SettingsDialog
import lutukai.pesalens.feature.settings.impl.presentation.SettingsEffect
import lutukai.pesalens.feature.settings.impl.presentation.SettingsIntent
import lutukai.pesalens.feature.settings.impl.presentation.SettingsViewModel
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val lock = FakeAppLockRepository()
    private val models = FakeModelManager()
    private val finance = FakeFinanceRepository()
    private val conversation = FakeConversationSession()
    private val messages = FakeUserMessageSink()

    private fun viewModel(systemDark: Boolean = false) =
        SettingsViewModel(settings, lock, models, finance, conversation, messages).also {
            it.onIntent(SettingsIntent.Load(systemDark))
        }

    private fun msg(id: Int) = UiText.Res(id)

    @Test
    fun onDeviceSegment_disabledUntilReady() = runTest {
        val vm = viewModel()
        assertFalse(vm.state.value.onDeviceEnabled)
        models.statusFlow.value = ModelStatus.Downloading(40)
        assertFalse(vm.state.value.onDeviceEnabled)
        assertEquals(ModelStatus.Downloading(40), vm.state.value.modelStatus)
        models.statusFlow.value = ModelStatus.Ready
        assertTrue(vm.state.value.onDeviceEnabled)
    }

    @Test
    fun load_reflectsSettingsAndLock() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = viewModel()
        assertEquals(AssistantMode.OnDevice, vm.state.value.mode)
        assertEquals(LockMethod.Biometric, vm.state.value.lockMethod)
    }

    @Test
    fun load_isIdempotent_butTracksSystemDark() = runTest {
        val vm = viewModel(systemDark = false)
        vm.onIntent(SettingsIntent.Load(true))
        assertTrue(vm.state.value.resolvedDark)
        settings.setAssistantMode(AssistantMode.OnDevice)
        assertEquals(AssistantMode.OnDevice, vm.state.value.mode)
    }

    @Test
    fun setCloud_withoutConsent_opensConsent() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(SettingsIntent.SetMode(AssistantMode.Cloud))
            assertEquals(SettingsEffect.OpenCloudConsent, awaitItem())
        }
        assertEquals(AssistantMode.OnDevice, settings.settings.first().assistantMode)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun setCloud_withConsent_setsModeAndMessages() = runTest {
        settings.giveCloudConsent()
        settings.setAssistantMode(AssistantMode.OnDevice)
        val vm = viewModel()
        vm.onIntent(SettingsIntent.SetMode(AssistantMode.Cloud))
        assertEquals(AssistantMode.Cloud, settings.settings.first().assistantMode)
        assertEquals(listOf(msg(R.string.settings_message_cloud)), messages.shown)
        assertEquals(AssistantMode.Cloud, vm.state.value.mode)
    }

    @Test
    fun setOnDevice_messages() = runTest {
        models.statusFlow.value = ModelStatus.Ready
        val vm = viewModel()
        vm.onIntent(SettingsIntent.SetMode(AssistantMode.OnDevice))
        assertEquals(AssistantMode.OnDevice, settings.settings.first().assistantMode)
        assertEquals(listOf(msg(R.string.settings_message_on_device)), messages.shown)
    }

    @Test
    fun setOnDevice_beforeReady_isIgnored() = runTest {
        val vm = viewModel()
        vm.onIntent(SettingsIntent.SetMode(AssistantMode.OnDevice))
        assertEquals(AssistantMode.Cloud, settings.settings.first().assistantMode)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun confirmClear_clearsAndMessages() = runTest {
        val vm = viewModel()
        vm.onIntent(SettingsIntent.AskClear)
        assertEquals(SettingsDialog.ClearConversation, vm.state.value.dialog)
        vm.onIntent(SettingsIntent.ConfirmClear)
        vm.onIntent(SettingsIntent.ConfirmClear)
        assertEquals(1, conversation.clearCalls)
        assertEquals(SettingsDialog.None, vm.state.value.dialog)
        assertEquals(listOf(msg(R.string.settings_message_cleared)), messages.shown)
    }

    @Test
    fun confirmReset_success_clearsAndMessages() = runTest {
        val vm = viewModel()
        vm.onIntent(SettingsIntent.AskReset)
        assertEquals(SettingsDialog.ResetSampleData, vm.state.value.dialog)
        vm.onIntent(SettingsIntent.ConfirmReset)
        assertEquals(1, finance.resetCalls)
        assertEquals(1, conversation.clearCalls)
        assertEquals(SettingsDialog.None, vm.state.value.dialog)
        assertEquals(listOf(msg(R.string.settings_message_reset)), messages.shown)
    }

    @Test
    fun confirmReset_notConnected_showsFailureMessage() = runTest {
        finance.resetResult = AppResult.Failure(AppError.NotConnected)
        val vm = viewModel()
        vm.onIntent(SettingsIntent.AskReset)
        vm.onIntent(SettingsIntent.ConfirmReset)
        assertEquals(1, finance.resetCalls)
        assertEquals(0, conversation.clearCalls)
        assertEquals(SettingsDialog.None, vm.state.value.dialog)
        assertEquals(listOf(msg(R.string.settings_message_reset_failed)), messages.shown)
    }

    @Test
    fun dismiss_closesDialog_andConfirmWithoutDialogDoesNothing() = runTest {
        val vm = viewModel()
        vm.onIntent(SettingsIntent.AskReset)
        vm.onIntent(SettingsIntent.Dismiss)
        assertEquals(SettingsDialog.None, vm.state.value.dialog)
        vm.onIntent(SettingsIntent.ConfirmReset)
        vm.onIntent(SettingsIntent.ConfirmClear)
        assertEquals(0, finance.resetCalls)
        assertEquals(0, conversation.clearCalls)
    }

    @Test
    fun themeSystem_showsResolvedValue() = runTest {
        val dark = viewModel(systemDark = true)
        assertEquals(ThemePreference.System, dark.state.value.theme)
        assertTrue(dark.state.value.resolvedDark)
        val light = viewModel(systemDark = false)
        assertFalse(light.state.value.resolvedDark)
    }

    @Test
    fun setTheme_storesExplicitChoice() = runTest {
        val vm = viewModel(systemDark = false)
        vm.onIntent(SettingsIntent.SetTheme(dark = true))
        assertEquals(ThemePreference.Dark, settings.settings.first().theme)
        assertTrue(vm.state.value.resolvedDark)
        vm.onIntent(SettingsIntent.SetTheme(dark = false))
        assertEquals(ThemePreference.Light, settings.settings.first().theme)
        assertFalse(vm.state.value.resolvedDark)
    }

    @Test
    fun navigation_intentsBecomeEffects_andBackFiresOnce() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(SettingsIntent.OpenModel)
            assertEquals(SettingsEffect.OpenModel, awaitItem())
            vm.onIntent(SettingsIntent.OpenAppLock)
            assertEquals(SettingsEffect.OpenAppLock, awaitItem())
            vm.onIntent(SettingsIntent.Back)
            vm.onIntent(SettingsIntent.Back)
            assertEquals(SettingsEffect.Close, awaitItem())
            expectNoEvents()
        }
    }
}
