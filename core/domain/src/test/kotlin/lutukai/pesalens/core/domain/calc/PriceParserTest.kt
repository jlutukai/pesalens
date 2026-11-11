package lutukai.pesalens.core.domain.calc

import lutukai.pesalens.core.model.Money.Companion.kes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PriceParserTest {
    @Test
    fun parsesPlainAndPrefixed() {
        assertEquals(kes(90_000), PriceParser.parse("90,000"))
        assertEquals(kes(90_000), PriceParser.parse("KSh 90,000"))
    }

    @Test
    fun parsesKSuffix() {
        assertEquals(kes(90_000), PriceParser.parse("90k"))
        assertEquals(kes(120_000), PriceParser.parse("120k"))
        assertEquals(kes(120_000), PriceParser.parse("120K"))
        assertEquals(kes(1_500), PriceParser.parse("can I afford 1.5k"))
    }

    @Test
    fun rejectsTooSmallOrMissing() {
        assertNull(PriceParser.parse("50"))
        assertNull(PriceParser.parse("help me create a budget"))
    }
}
