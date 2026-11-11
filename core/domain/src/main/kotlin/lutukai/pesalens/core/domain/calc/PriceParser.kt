package lutukai.pesalens.core.domain.calc

import lutukai.pesalens.core.model.Money
import java.math.BigDecimal

object PriceParser {
    private val number = Regex("""(\d[\d,]*(?:\.\d+)?)\s*([kK](?![A-Za-z]))?""")
    private val minimumShillings = BigDecimal(100)
    private val thousand = BigDecimal(1000)

    /** First amount of at least KSh 100 in [text], honouring a `k` suffix; null if none. */
    fun parse(text: String): Money? =
        number.findAll(text).firstNotNullOfOrNull { match ->
            val base = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return@firstNotNullOfOrNull null
            val shillings = if (match.groupValues[2].isNotEmpty()) base * thousand else base
            if (shillings >= minimumShillings) Money.kes(shillings.toLong()) else null
        }
}
