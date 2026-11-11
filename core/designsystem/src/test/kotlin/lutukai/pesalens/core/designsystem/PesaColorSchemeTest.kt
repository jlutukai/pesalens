package lutukai.pesalens.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import lutukai.pesalens.core.designsystem.theme.PesaColors
import lutukai.pesalens.core.designsystem.theme.toColorScheme

class PesaColorSchemeTest {

    @Test
    fun dark_mapsTokens() {
        val c = PesaColors.Dark
        val s = c.toColorScheme()
        assertEquals(c.accent, s.primary)
        assertEquals(c.background, s.onPrimary)
        assertEquals(c.tintSubtle, s.primaryContainer)
        assertEquals(c.tint, s.secondaryContainer)
        assertEquals(c.background, s.background)
        assertEquals(c.surface, s.surface)
        assertEquals(c.surfaceAlt, s.surfaceContainerLow)
        assertEquals(c.text, s.onSurface)
        assertEquals(c.textMuted, s.onSurfaceVariant)
        assertEquals(c.border, s.outline)
        assertEquals(c.warnText, s.error)
        assertEquals(c.scrim, s.scrim)
    }

    @Test
    fun dark_unmappedRolesUseDarkFallbacks() {
        val s = PesaColors.Dark.toColorScheme()
        assertEquals(darkColorScheme().surfaceVariant, s.surfaceVariant)
        assertNotEquals(lightColorScheme().surfaceVariant, s.surfaceVariant)
        assertNotEquals(lightColorScheme().surfaceContainerHighest, s.surfaceContainerHighest)
    }

    @Test
    fun onError_isWarnBackgroundInBothThemes() {
        assertEquals(PesaColors.Light.warnBackground, PesaColors.Light.toColorScheme().onError)
        assertEquals(PesaColors.Dark.warnBackground, PesaColors.Dark.toColorScheme().onError)
    }
}
