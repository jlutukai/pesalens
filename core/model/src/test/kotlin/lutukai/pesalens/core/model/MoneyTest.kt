package lutukai.pesalens.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyTest {
    @Test
    fun kes_convertsToMinorUnits() {
        assertEquals(6_500_000L, Money.kes(65_000).minor)
    }

    @Test
    fun plusAndMinus_work() {
        assertEquals(Money.kes(15), Money.kes(10) + Money.kes(5))
        assertEquals(Money.kes(-5), Money.kes(10) - Money.kes(15))
        assertEquals(10L, Money(1_099).wholeShillings)
    }
}
