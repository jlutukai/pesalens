package lutukai.pesalens.core.ui.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatTest {
    @Test fun longMonth() = assertEquals("November 2026", YearMonth(2026, 11).formatLong())

    @Test fun shortMonthHasNoYearExceptJanuary() {
        assertEquals("Dec", YearMonth(2026, 12).formatShort())
        assertEquals("Jan '27", YearMonth(2027, 1).formatShort())
        assertEquals("Feb", YearMonth(2027, 2).formatShort())
    }

    @Test fun dayMonth() = assertEquals("2 Nov", LocalDate(2026, 11, 2).formatDayMonth())

    @Test fun ignoresDeviceLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.FRANCE)
            assertEquals("November 2026", YearMonth(2026, 11).formatLong())
            assertEquals("2 Nov", LocalDate(2026, 11, 2).formatDayMonth())
        } finally {
            Locale.setDefault(original)
        }
    }
}
