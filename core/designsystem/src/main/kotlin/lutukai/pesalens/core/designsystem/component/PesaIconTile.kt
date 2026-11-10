package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

enum class TileTone {
    /** tintSubtle fill, border, accentText icon. */
    Accent,

    /** warn fill, border and icon. */
    Warn,

    /** surfaceAlt fill, border, text icon (transaction rows). */
    Neutral,
}

/** Square icon tile (decorative icon). */
@Composable
fun PesaIconTile(
    icon: ImageVector,
    size: Dp,
    iconSize: Dp,
    tone: TileTone = TileTone.Accent,
    shape: Shape = PesaLensTheme.shapes.tile,
) {
    val colors = PesaLensTheme.colors
    val (fill, edge, ink) = when (tone) {
        TileTone.Accent -> Triple(colors.tintSubtle, colors.border, colors.accentText)
        TileTone.Warn -> Triple(colors.warnBackground, colors.warnBorder, colors.warnText)
        TileTone.Neutral -> Triple(colors.surfaceAlt, colors.border, colors.text)
    }
    Surface(Modifier.size(size), shape = shape, color = fill, contentColor = ink, border = BorderStroke(1.dp, edge)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    }
}
