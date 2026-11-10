package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

private fun Modifier.topBorder(color: Color): Modifier = drawBehind {
    val w = 1.dp.toPx()
    drawLine(color, Offset(0f, w / 2), Offset(size.width, w / 2), w)
}

/** Label (textMuted) and value (text) on one line, with a 1dp top border. */
@Composable
fun PesaValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    topBorder: Color = PesaLensTheme.colors.border,
    minHeight: Dp = 36.dp,
) {
    val colors = PesaLensTheme.colors
    Row(
        modifier.fillMaxWidth().heightIn(min = minHeight).topBorder(topBorder),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm),
    ) {
        Text(label, style = PesaLensTheme.type.bodySmall, color = colors.textMuted, modifier = Modifier.weight(1f))
        Text(
            value,
            style = PesaLensTheme.type.bodySmall.copy(
                fontWeight = if (strong) FontWeight.Medium else FontWeight.Normal,
                fontFeatureSettings = "tnum",
            ),
            color = colors.text,
        )
    }
}

/** 56dp settings-style row: icon, title with optional subtitle, optional chevron. */
@Composable
fun PesaNavRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    subtitle: String? = null,
    showChevron: Boolean = true,
    topBorder: Boolean = false,
) {
    val colors = PesaLensTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (topBorder) Modifier.topBorder(colors.border) else Modifier)
            .padding(horizontal = PesaSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = PesaLensTheme.type.body, color = colors.text)
            if (subtitle != null) Text(subtitle, style = PesaLensTheme.type.kicker.copy(letterSpacing = TextUnit.Unspecified), color = colors.textMuted)
        }
        if (showChevron) Icon(PesaIcons.CaretRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
    }
}

/** Bordered option card: icon tile, title and subtitle, chevron. */
@Composable
fun PesaCardRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    minHeight: Dp = 72.dp,
) {
    val colors = PesaLensTheme.colors
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
        shape = PesaLensTheme.shapes.tileLarge,
        color = colors.surface,
        contentColor = colors.text,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = PesaSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PesaIconTile(icon, size = 44.dp, iconSize = 22.dp, shape = PesaLensTheme.shapes.segment)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PesaSpacing.xxs)) {
                Text(title, style = PesaLensTheme.type.bodyStrong, color = colors.text)
                Text(subtitle, style = PesaLensTheme.type.meta, color = colors.textMuted)
            }
            Icon(PesaIcons.CaretRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}
