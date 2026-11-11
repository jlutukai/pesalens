package lutukai.pesalens.core.data

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InMemoryConversationSessionTest {
    @Test
    fun clear_emitsToCollectorsAndNeverSuspendsWithoutOne() = runTest {
        val session = InMemoryConversationSession()
        session.clear()
        session.clears.test {
            session.clear()
            awaitItem()
        }
    }
}
