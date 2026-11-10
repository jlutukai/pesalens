package lutukai.pesalens.core.domain.calc

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PinPolicyTest {
    @Test
    fun weakPins() {
        listOf("1111", "0000", "1234", "4321", "0123", "9876").forEach {
            assertTrue(PinPolicy.isWeak(it), it)
        }
    }

    @Test
    fun strongPins() {
        listOf("2580", "1357").forEach { assertFalse(PinPolicy.isWeak(it), it) }
    }
}
