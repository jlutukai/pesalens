package lutukai.pesalens.core.designsystem

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import lutukai.pesalens.core.designsystem.theme.PesaColors

class PesaColorsTest {

    @Test
    fun light_matchesNocturneTable() {
        val c = PesaColors.Light
        assertEquals(Color(0xFFF3F5FE), c.background)
        assertEquals(Color(0xFFFAFBFF), c.surface)
        assertEquals(Color(0xFFF3F5FE), c.surfaceAlt)
        assertEquals(Color(0xFF292B31), c.text)
        assertEquals(Color(0xFF595D6C), c.textMuted)
        assertEquals(Color(0xFF796CBF), c.accent)
        assertEquals(Color(0xFF5D5294), c.accentText)
        assertEquals(Color(0xFFE7E5FE), c.tint)
        assertEquals(Color(0xFFF5F4FF), c.tintSubtle)
        assertEquals(Color(0xFFCFD3E5), c.border)
        assertEquals(Color(0xFF824C11), c.warnText)
        assertEquals(Color(0xFFFEF0D4), c.warnBackground)
        assertEquals(Color(0xFFDBB06B), c.warnBorder)
        assertEquals(
            listOf(0xFF5D5294, 0xFF968AE0, 0xFFD2CEFD, 0xFF595D6C, 0xFF9397AB, 0xFFCFD3E5).map { Color(it) },
            c.chart.toList(),
        )
        assertEquals(Color(0xB3F3F5FE), c.patternInk)
        assertEquals(Color(0x73161826), c.scrim)
        assertEquals(false, c.isDark)
    }

    @Test
    fun dark_matchesNocturneTable() {
        val c = PesaColors.Dark
        assertEquals(Color(0xFF161826), c.background)
        assertEquals(Color(0xFF232532), c.surface)
        assertEquals(Color(0xFF1C1E2B), c.surfaceAlt)
        assertEquals(Color(0xFFE9E9ED), c.text)
        assertEquals(Color(0xFFB2B6CA), c.textMuted)
        assertEquals(Color(0xFF9184D9), c.accent)
        assertEquals(Color(0xFFD2CEFD), c.accentText)
        assertEquals(Color(0xFF423A6A), c.tint)
        assertEquals(Color(0xFF2B2741), c.tintSubtle)
        assertEquals(Color(0xFF3F424D), c.border)
        assertEquals(Color(0xFFF7CD87), c.warnText)
        assertEquals(Color(0xFF33230F), c.warnBackground)
        assertEquals(Color(0xFF846230), c.warnBorder)
        assertEquals(
            listOf(0xFFD2CEFD, 0xFF968AE0, 0xFF5D5294, 0xFFCFD3E5, 0xFF9397AB, 0xFF595D6C).map { Color(it) },
            c.chart.toList(),
        )
        assertEquals(Color(0xB3232532), c.patternInk)
        assertEquals(Color(0x99000000), c.scrim)
        assertEquals(true, c.isDark)
    }

    @Test
    fun chart_hasSixColors() {
        assertEquals(6, PesaColors.Light.chart.size)
        assertEquals(6, PesaColors.Dark.chart.size)
    }
}
