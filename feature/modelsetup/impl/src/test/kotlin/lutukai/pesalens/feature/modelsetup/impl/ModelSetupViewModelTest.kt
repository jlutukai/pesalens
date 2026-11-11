package lutukai.pesalens.feature.modelsetup.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.fake.FakeModelManager
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.core.testing.fake.FakeUserMessageSink
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupEffect
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupIntent
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupUiState
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupViewModel
import org.junit.Rule
import org.junit.Test

class ModelSetupViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val models = FakeModelManager()
    private val settings = FakeSettingsRepository()
    private val messages = FakeUserMessageSink()

    private fun viewModel(origin: ModelSetupOrigin = ModelSetupOrigin.Settings) =
        ModelSetupViewModel(origin, models, settings, messages).also { it.onIntent(ModelSetupIntent.Load) }

    private fun msg(id: Int) = UiText.Res(id)

    @Test
    fun statusMapping_allSeven() = runTest {
        val origin = ModelSetupOrigin.Assistant
        val vm = viewModel(origin)
        val expected = listOf(
            ModelStatus.NotDownloaded to ModelSetupUiState.NotDownloaded(origin),
            ModelStatus.ReadyToDownload to ModelSetupUiState.ReadyToDownload(origin),
            ModelStatus.Downloading(37) to ModelSetupUiState.Downloading(origin, 37),
            ModelStatus.Verifying to ModelSetupUiState.Verifying(origin),
            ModelStatus.Ready to ModelSetupUiState.Ready(origin),
            ModelStatus.Failed(42) to ModelSetupUiState.Failed(origin, 42),
            ModelStatus.Unsupported to ModelSetupUiState.Unsupported(origin),
        )
        for ((status, state) in expected) {
            models.statusFlow.value = status
            assertEquals(state, vm.state.value)
        }
    }

    @Test
    fun load_isIdempotent() = runTest {
        val vm = viewModel()
        vm.onIntent(ModelSetupIntent.Load)
        models.statusFlow.value = ModelStatus.Ready
        assertEquals(ModelSetupUiState.Ready(ModelSetupOrigin.Settings), vm.state.value)
    }

    @Test
    fun checkDevice_success_callsManagerAndKeepsState() = runTest {
        val vm = viewModel()
        vm.onIntent(ModelSetupIntent.CheckDevice)
        assertEquals(listOf("checkDevice"), models.calls)
        assertEquals(ModelSetupUiState.NotDownloaded(ModelSetupOrigin.Settings), vm.state.value)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun checkDevice_notConnected_showsUnavailableMessage() = runTest {
        models.checkResult = AppResult.Failure(AppError.NotConnected)
        val vm = viewModel()
        vm.onIntent(ModelSetupIntent.CheckDevice)
        assertEquals(listOf(msg(R.string.model_message_unavailable)), messages.shown)
        assertEquals(ModelSetupUiState.NotDownloaded(ModelSetupOrigin.Settings), vm.state.value)
    }

    @Test
    fun checkDevice_whileInFlight_ignoresFurtherTaps() = runTest {
        val gate = CompletableDeferred<AppResult<Unit>>()
        var checks = 0
        val slow = object : ModelManager {
            override val status: Flow<ModelStatus> = MutableStateFlow(ModelStatus.NotDownloaded)
            override suspend fun checkDevice(): AppResult<Unit> {
                checks++
                return gate.await()
            }
            override suspend fun startDownload() = Unit
            override suspend fun cancelDownload() = Unit
            override suspend fun delete() = Unit
        }
        val vm = ModelSetupViewModel(ModelSetupOrigin.Settings, slow, settings, messages)
        vm.onIntent(ModelSetupIntent.CheckDevice)
        vm.onIntent(ModelSetupIntent.CheckDevice)
        assertEquals(1, checks)
        gate.complete(AppResult.Success(Unit))
        vm.onIntent(ModelSetupIntent.CheckDevice)
        assertEquals(2, checks)
    }

    @Test
    fun download_andRetry_startDownload() = runTest {
        val vm = viewModel()
        vm.onIntent(ModelSetupIntent.Download)
        vm.onIntent(ModelSetupIntent.Retry)
        assertEquals(listOf("startDownload", "startDownload"), models.calls)
    }

    @Test
    fun cancel_messages() = runTest {
        val vm = viewModel()
        vm.onIntent(ModelSetupIntent.Cancel)
        assertEquals(listOf("cancelDownload"), models.calls)
        assertEquals(listOf(msg(R.string.model_message_cancelled)), messages.shown)
    }

    @Test
    fun delete_setsCloudAndMessages() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        val vm = viewModel()
        vm.onIntent(ModelSetupIntent.Delete)
        assertEquals(listOf("delete"), models.calls)
        assertEquals(AssistantMode.Cloud, settings.settings.first().assistantMode)
        assertEquals(listOf(msg(R.string.model_message_deleted)), messages.shown)
    }

    @Test
    fun useOnDevice_fromWelcome_continuesOnboarding() = runTest {
        val vm = viewModel(ModelSetupOrigin.Welcome)
        vm.effects.test {
            vm.onIntent(ModelSetupIntent.UseOnDevice)
            vm.onIntent(ModelSetupIntent.UseOnDevice)
            assertEquals(ModelSetupEffect.ContinueOnboarding, awaitItem())
            expectNoEvents()
        }
        assertEquals(AssistantMode.OnDevice, settings.settings.first().assistantMode)
        assertEquals(listOf(msg(R.string.model_message_on_device)), messages.shown)
    }

    @Test
    fun useOnDevice_fromSettingsOrAssistant_closes() = runTest {
        for (origin in listOf(ModelSetupOrigin.Settings, ModelSetupOrigin.Assistant)) {
            val vm = viewModel(origin)
            vm.effects.test {
                vm.onIntent(ModelSetupIntent.UseOnDevice)
                assertEquals(ModelSetupEffect.Close, awaitItem())
            }
        }
    }

    @Test
    fun useCloud_withoutConsent_opensConsent() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ModelSetupIntent.UseCloud)
            assertEquals(ModelSetupEffect.OpenCloudConsent, awaitItem())
        }
        assertEquals(AssistantMode.OnDevice, settings.settings.first().assistantMode)
    }

    @Test
    fun useCloud_withConsent_setsCloudAndCloses() = runTest {
        settings.giveCloudConsent()
        settings.setAssistantMode(AssistantMode.OnDevice)
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ModelSetupIntent.UseCloud)
            assertEquals(ModelSetupEffect.Close, awaitItem())
        }
        assertEquals(AssistantMode.Cloud, settings.settings.first().assistantMode)
    }

    @Test
    fun back_closesOnce() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ModelSetupIntent.Back)
            vm.onIntent(ModelSetupIntent.Back)
            assertEquals(ModelSetupEffect.Close, awaitItem())
            expectNoEvents()
        }
    }
}
