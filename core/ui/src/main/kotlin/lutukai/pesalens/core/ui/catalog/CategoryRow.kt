package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.pattern.ChartPattern
import lutukai.pesalens.core.designsystem.pattern.patternFill
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.ui.format.formatKes

/** Spoken name of a chart pattern, e.g. "diagonal stripes". */
@Composable
internal fun ChartPattern.displayName(): String = stringResource(
    when (this) {
        ChartPattern.Solid -> R.string.core_ui_pattern_solid
        ChartPattern.Diagonal -> R.string.core_ui_pattern_diagonal
        ChartPattern.Dots -> R.string.core_ui_pattern_dots
        ChartPattern.Horizontal -> R.string.core_ui_pattern_horizontal
        ChartPattern.ReverseDiagonal -> R.string.core_ui_pattern_reverse
        ChartPattern.Vertical -> R.string.core_ui_pattern_vertical
    },
)

/** "Food and groceries, KSh 15,000, 23 percent. Open transactions", shared by the row and its bar segment. */
@Composable
internal fun CategorySpendUi.rowDescription(): String =
    stringResource(R.string.core_ui_category_description, label, amount.formatKes(), sharePercent)

internal val SmallLabel: TextStyle
    @Composable get() = PesaLensTheme.type.kicker.copy(letterSpacing = TextUnit.Unspecified)

internal val TabularBody: TextStyle
    @Composable get() = PesaLensTheme.type.body.copy(fontFeatureSettings = "tnum")

/** 48dp+ tappable row: pattern swatch, label with "share · pattern", amount, chevron. */
@Composable
fun CategoryRow(item: CategorySpendUi, patternName: String, onClick: () -> Unit) {
    val colors = PesaLensTheme.colors
    val description = item.rowDescription()
    val border = colors.border
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val w = 1.dp.toPx()
                drawLine(border, Offset(0f, w / 2), Offset(size.width, w / 2), w)
            }
            .heightIn(min = PesaSpacing.minTouchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(start = 2.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PatternSwatch(item.tone, 16.dp)
        Column(Modifier.weight(1f)) {
            Text(item.label, style = PesaLensTheme.type.body, color = colors.text)
            Text(
                stringResource(R.string.core_ui_category_subtitle, item.sharePercent, patternName),
                style = SmallLabel,
                color = colors.textMuted,
            )
        }
        Text(item.amount.formatKes(), style = TabularBody, color = colors.text)
        Icon(PesaIcons.CaretRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
    }
}

/** Series swatch: chart colour with its pattern, 4dp corners, 1dp border. */
@Composable
internal fun PatternSwatch(tone: Int, size: Dp) {
    val colors = PesaLensTheme.colors
    val shape = RoundedCornerShape(4.dp)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .patternFill(ChartPattern.forTone(tone), colors.chart[tone], colors.patternInk)
            .border(BorderStroke(1.dp, colors.border), shape),
    )
}
