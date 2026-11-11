package lutukai.pesalens.core.domain.ai

import kotlinx.coroutines.flow.Flow
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.model.ModelStatus

interface ModelManager {
    val status: Flow<ModelStatus>

    suspend fun checkDevice(): AppResult<Unit>

    suspend fun startDownload()

    suspend fun cancelDownload()

    suspend fun delete()
}
