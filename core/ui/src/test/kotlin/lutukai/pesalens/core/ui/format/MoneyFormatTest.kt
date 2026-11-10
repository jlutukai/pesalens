package lutukai.pesalens.core.ui.format

import lutukai.pesalens.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {
    @Test fun groupsWholeShillings() = assertEquals("KSh 65,000", Money.kes(65_000).formatKes())

    @Test fun zero() = assertEquals("KSh 0", Money(0).formatKes())

    @Test fun negativeClampsToZero() = assertEquals("KSh 0", Money.kes(-10_000).formatKes())

    @Test fun roundsHalfUp() {
        assertEquals("KSh 12,346", Money(1_234_550).formatKes())
        assertEquals("KSh 12,345", Money(1_234_549).formatKes())
    }

    @Test fun millions() = assertEquals("KSh 1,250,000", Money.kes(1_250_000).formatKes())
}
