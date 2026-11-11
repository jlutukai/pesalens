package lutukai.pesalens.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Nocturne palette. The only place raw color values live. */
@Immutable
data class PesaColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val text: Color,
    val textMuted: Color,
    val accent: Color,
    val accentText: Color,
    val tint: Color,
    val tintSubtle: Color,
    val border: Color,
    val warnText: Color,
    val warnBackground: Color,
    val warnBorder: Color,
    val chart: ImmutableList<Color>,
    val patternInk: Color,
    val scrim: Color,
    val isDark: Boolean,
) {
    /** Input placeholder: the text colour at 55% (the prototype's `::placeholder { opacity: .55 }`). */
    val placeholder: Color get() = text.copy(alpha = 0.55f)

    companion object {
        val Light = PesaColors(
            background = Color(0xFFF3F5FE),
            surface = Color(0xFFFAFBFF),
            surfaceAlt = Color(0xFFF3F5FE),
            text = Color(0xFF292B31),
            textMuted = Color(0xFF595D6C),
            accent = Color(0xFF796CBF),
            accentText = Color(0xFF5D5294),
            tint = Color(0xFFE7E5FE),
            tintSubtle = Color(0xFFF5F4FF),
            border = Color(0xFFCFD3E5),
            warnText = Color(0xFF824C11),
            warnBackground = Color(0xFFFEF0D4),
            warnBorder = Color(0xFFDBB06B),
            chart = persistentListOf(
                Color(0xFF5D5294), Color(0xFF968AE0), Color(0xFFD2CEFD),
                Color(0xFF595D6C), Color(0xFF9397AB), Color(0xFFCFD3E5),
            ),
            patternInk = Color(0xB3F3F5FE),
            scrim = Color(0x73161826),
            isDark = false,
        )

        val Dark = PesaColors(
            background = Color(0xFF161826),
            surface = Color(0xFF232532),
            surfaceAlt = Color(0xFF1C1E2B),
            text = Color(0xFFE9E9ED),
            textMuted = Color(0xFFB2B6CA),
            accent = Color(0xFF9184D9),
            accentText = Color(0xFFD2CEFD),
            tint = Color(0xFF423A6A),
            tintSubtle = Color(0xFF2B2741),
            border = Color(0xFF3F424D),
            warnText = Color(0xFFF7CD87),
            warnBackground = Color(0xFF33230F),
            warnBorder = Color(0xFF846230),
            chart = persistentListOf(
                Color(0xFFD2CEFD), Color(0xFF968AE0), Color(0xFF5D5294),
                Color(0xFFCFD3E5), Color(0xFF9397AB), Color(0xFF595D6C),
            ),
            patternInk = Color(0xB3232532),
            scrim = Color(0x99000000),
            isDark = true,
        )
    }
}
