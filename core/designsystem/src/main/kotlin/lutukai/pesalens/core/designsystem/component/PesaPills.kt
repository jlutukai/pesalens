package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

enum class PillTone {
    /** 1dp textMuted border. */
    Neutral,

    /** Accent border, tint fill, accentText. */
    Accent,

    /** 1dp dashed textMuted border. */
    Dashed,
}

private val PillText @Composable get() = PesaLensTheme.type.kicker.copy(letterSpacing = TextUnit.Unspecified)

/** Pill with an optional leading icon. Radius is half the height (22dp -> 11, 24dp -> 12). */
@Composable
fun PesaPill(text: String, tone: PillTone, icon: ImageVector? = null, height: Dp = 22.dp) {
    val colors = PesaLensTheme.colors
    val shape = RoundedCornerShape(height / 2)
    val content = if (tone == PillTone.Accent) colors.accentText else colors.text
    val base = Modifier.heightIn(min = height).clip(shape)
    val styled = when (tone) {
        PillTone.Neutral -> base.border(BorderStroke(1.dp, colors.textMuted), shape)
        PillTone.Accent -> base.background(colors.tint).border(BorderStroke(1.dp, colors.accent), shape)
        PillTone.Dashed -> base.drawBehind {
            val w = 1.dp.toPx()
            drawRoundRect(
                color = colors.textMuted,
                topLeft = Offset(w / 2, w / 2),
                size = Size(size.width - w, size.height - w),
                cornerRadius = CornerRadius(size.height / 2 - w / 2),
                style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))),
            )
        }
    }
    Row(
        styled.padding(horizontal = PesaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xs),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
        Text(text, style = PillText.copy(fontWeight = if (tone == PillTone.Dashed) FontWeight.Normal else FontWeight.Medium), color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Uppercased section kicker in textMuted. */
@Composable
fun PesaKicker(text: String) {
    Text(text.uppercase(), style = PesaLensTheme.type.kicker, color = PesaLensTheme.colors.textMuted)
}
