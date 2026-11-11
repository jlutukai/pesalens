package lutukai.pesalens.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.model.ModelStatus

/** No on-device model is available yet: the status is always [ModelStatus.NotDownloaded]. */
class NotConnectedModelManager : ModelManager {
    override val status: Flow<ModelStatus> = flowOf(ModelStatus.NotDownloaded)

    override suspend fun checkDevice(): AppResult<Unit> = AppResult.Failure(AppError.NotConnected)

    override suspend fun startDownload() = Unit

    override suspend fun cancelDownload() = Unit

    override suspend fun delete() = Unit
}
