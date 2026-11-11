package lutukai.pesalens.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConversationSession {
    val clears: Flow<Unit>

    suspend fun clear()
}
