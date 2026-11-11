package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.component.PesaDisclosureLink
import lutukai.pesalens.core.designsystem.component.PesaKicker
import lutukai.pesalens.core.designsystem.component.PesaPill
import lutukai.pesalens.core.designsystem.component.PillTone
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.pattern.ChartPattern
import lutukai.pesalens.core.designsystem.pattern.patternFill
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.SpendingUi
import lutukai.pesalens.core.ui.catalog.model.accessibilitySummary
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong

/**
 * Spending breakdown: figure, patterned stacked bar, tappable category rows, insight and a text alternative.
 * Bar segments and rows both report the category id through [onCategoryClick].
 */
@Composable
fun SpendingChartCard(spending: SpendingUi, onCategoryClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val summary = remember(spending) { spending.accessibilitySummary() }
    PesaCard(modifier.semantics { contentDescription = summary }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PesaKicker(stringResource(R.string.core_ui_spending_kicker, spending.month.formatLong()))
                Text(spending.total.formatKes(), style = PesaLensTheme.type.figure, color = colors.text)
                Text(
                    pluralStringResource(
                        R.plurals.core_ui_spending_total_caption,
                        spending.categories.size,
                        spending.categories.size,
                        spending.rangeLabel,
                    ),
                    style = PesaLensTheme.type.meta,
                    color = colors.textMuted,
                )
            }
            PesaPill(stringResource(R.string.core_ui_spending_sample_tag), PillTone.Dashed, PesaIcons.Database, height = 24.dp)
        }

        // A zero-weight segment is illegal and would be invisible anyway; rows still list every category.
        val barCategories = spending.categories.filter { it.amount.minor > 0 }
        if (barCategories.isNotEmpty()) Row(
            Modifier.fillMaxWidth().height(30.dp).clip(PesaLensTheme.shapes.bar),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            barCategories.forEach { category ->
                val description = category.rowDescription()
                Column(
                    Modifier
                        .weight(category.amount.minor.toFloat())
                        .fillMaxHeight()
                        .patternFill(ChartPattern.forTone(category.tone), colors.chart[category.tone], colors.patternInk)
                        .clickable(role = Role.Button) { onCategoryClick(category.id) }
                        .semantics { contentDescription = description },
                ) {}
            }
        }

        Column {
            spending.categories.forEach { category ->
                CategoryRow(category, ChartPattern.forTone(category.tone).displayName(), onClick = { onCategoryClick(category.id) })
            }
        }

        spending.insight?.let {
            InsightBlock(it)
        }

        PesaDisclosureLink(stringResource(R.string.core_ui_spending_read_as_text), PesaIcons.TextAlignLeft) {
            Text(summary, style = PesaLensTheme.type.meta.copy(lineHeight = 19.5.sp), color = colors.textMuted)
        }
    }
}

/** Borderless tintSubtle block: lightbulb, insight text and the "computed by the app" footnote. */
@Composable
private fun InsightBlock(text: String) {
    val colors = PesaLensTheme.colors
    Surface(Modifier.fillMaxWidth(), shape = PesaLensTheme.shapes.tile, color = colors.tintSubtle) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(PesaIcons.Lightbulb, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text, style = PesaLensTheme.type.bodySmall, color = colors.text)
                Text(
                    stringResource(R.string.core_ui_spending_insight_footnote),
                    style = PesaLensTheme.type.kicker.copy(letterSpacing = TextUnit.Unspecified),
                    color = colors.textMuted,
                )
            }
        }
    }
}
