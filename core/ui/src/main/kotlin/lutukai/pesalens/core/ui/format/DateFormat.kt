package lutukai.pesalens.core.ui.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val MonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private fun YearMonth.monthName(): String = MonthNames[month.ordinal]

/** "November 2026". */
fun YearMonth.formatLong(): String = "${monthName()} $year"

/** "Dec"; January carries a year suffix: "Jan '27". */
fun YearMonth.formatShort(): String {
    val short = monthName().take(3)
    return if (month.ordinal == 0) "$short '${(year % 100).toString().padStart(2, '0')}" else short
}

/** "2 Nov". */
fun LocalDate.formatDayMonth(): String = "$day ${MonthNames[month.ordinal].take(3)}"
