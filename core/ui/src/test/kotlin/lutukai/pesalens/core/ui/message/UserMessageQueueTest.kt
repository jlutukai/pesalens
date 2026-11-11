package lutukai.pesalens.core.ui.message

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.ui.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals

class UserMessageQueueTest {
    @Test fun deliversMessagesInOrder() = runTest {
        val queue = UserMessageQueue()
        queue.show(UiText.Raw("one"))
        queue.show(UiText.Raw("two"))
        queue.messages.test {
            assertEquals(UiText.Raw("one"), awaitItem())
            assertEquals(UiText.Raw("two"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
