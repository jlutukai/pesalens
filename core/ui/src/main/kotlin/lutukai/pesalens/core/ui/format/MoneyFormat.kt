package lutukai.pesalens.core.ui.format

import lutukai.pesalens.core.model.Money
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** "KSh 65,000": whole shillings, half-up rounding, negatives clamp to zero, always en-US grouping. */
fun Money.formatKes(): String {
    val shillings = (maxOf(minor, 0L) + 50) / 100
    // DecimalFormat is not thread-safe, so build per call; amounts are formatted rarely enough for this to be cheap.
    val format = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
    return "KSh " + format.format(shillings)
}
