package lutukai.pesalens.core.testing.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.model.ModelStatus

/** Status is driven by tests through [statusFlow]; every call is recorded by name in [calls]. */
class FakeModelManager : ModelManager {
    val statusFlow = MutableStateFlow<ModelStatus>(ModelStatus.NotDownloaded)
    var checkResult: AppResult<Unit> = AppResult.Success(Unit)

    private val _calls = mutableListOf<String>()
    val calls: List<String> get() = _calls

    override val status: Flow<ModelStatus> = statusFlow

    override suspend fun checkDevice(): AppResult<Unit> {
        _calls += "checkDevice"
        return checkResult
    }

    override suspend fun startDownload() {
        _calls += "startDownload"
    }

    override suspend fun cancelDownload() {
        _calls += "cancelDownload"
    }

    override suspend fun delete() {
        _calls += "delete"
    }
}
