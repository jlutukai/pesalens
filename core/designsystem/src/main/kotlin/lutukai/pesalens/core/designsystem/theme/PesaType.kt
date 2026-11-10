package lutukai.pesalens.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.R

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
)

@Immutable
data class PesaType(
    val figure: TextStyle,
    val display: TextStyle,
    val headline: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val title: TextStyle,
    val titleSmall: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val bodySmall: TextStyle,
    val meta: TextStyle,
    val kicker: TextStyle,
    val caption: TextStyle,
) {
    companion object {
        fun inter(): PesaType {
            fun style(
                size: Int,
                weight: FontWeight,
                lineHeight: TextUnit = TextUnit.Unspecified,
                letterSpacing: TextUnit = TextUnit.Unspecified,
                features: String? = null,
            ) = TextStyle(
                fontFamily = InterFamily,
                fontSize = size.sp,
                fontWeight = weight,
                lineHeight = lineHeight,
                letterSpacing = letterSpacing,
                fontFeatureSettings = features,
            )
            return PesaType(
                figure = style(30, FontWeight.Medium, 34.5.sp, (-0.6).sp, features = "tnum"),
                display = style(30, FontWeight.Medium, 34.5.sp, (-0.6).sp),
                headline = style(28, FontWeight.Medium, 32.2.sp, (-0.56).sp),
                titleLarge = style(22, FontWeight.Medium),
                titleMedium = style(20, FontWeight.Medium),
                title = style(18, FontWeight.Medium, letterSpacing = (-0.18).sp),
                titleSmall = style(17, FontWeight.Medium),
                subtitle = style(16, FontWeight.Medium),
                body = style(15, FontWeight.Normal, 21.75.sp),
                bodyStrong = style(15, FontWeight.Medium),
                label = style(14, FontWeight.Medium),
                bodySmall = style(14, FontWeight.Normal, 20.3.sp),
                meta = style(13, FontWeight.Normal, 18.85.sp),
                kicker = style(12, FontWeight.Normal, letterSpacing = 0.72.sp),
                caption = style(11, FontWeight.Normal),
            )
        }
    }
}
