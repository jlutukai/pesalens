package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.PesaAmountField
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.component.PesaKicker
import lutukai.pesalens.core.designsystem.component.PesaRatioBar
import lutukai.pesalens.core.designsystem.component.PesaValueRow
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.catalog.model.shillingDigits
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong
import kotlin.math.abs

/**
 * Editable budget draft: income, allocation bar, per-category limits, totals, the over-budget alert and Cancel / Review.
 * Saved and Discarded drafts lock every input and show a status line instead of the actions.
 */
@Composable
fun BudgetEditorCard(
    ui: BudgetUi,
    onIncomeChange: (String) -> Unit,
    onLineChange: (index: Int, String) -> Unit,
    onLineStep: (index: Int, StepDirection) -> Unit,
    onCancel: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    val group = stringResource(R.string.core_ui_budget_group)
    val editable = ui.status == BudgetStatus.Draft
    val over = ui.totals.isOver
    PesaCard(modifier.semantics { contentDescription = group }) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            PesaKicker(stringResource(R.string.core_ui_budget_kicker, ui.month.formatLong()))
            Text(stringResource(R.string.core_ui_budget_title), style = type.title, color = colors.text)
            Text(stringResource(R.string.core_ui_budget_hint), style = type.meta, color = colors.textMuted)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.core_ui_budget_income_label), style = type.bodySmall, color = colors.text, modifier = Modifier.weight(1f))
            PesaAmountField(
                value = ui.income.shillingDigits(),
                onValueChange = onIncomeChange,
                placeholder = "0",
                modifier = Modifier.width(fieldWidth(150.dp)),
                enabled = editable,
                contentDescription = stringResource(R.string.core_ui_budget_income_description),
                textAlign = TextAlign.End,
                containerColor = colors.surfaceAlt,
                horizontalPadding = 10.dp,
                textStyle = type.body.copy(fontFeatureSettings = "tnum"),
                prefixStyle = type.meta,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PesaRatioBar(ui.totals.allocatedRatio, color = if (over) colors.warnText else colors.accent)
            Text(
                stringResource(R.string.core_ui_budget_allocated, ui.totals.total.formatKes(), ui.income.formatKes()),
                style = SmallLabel,
                color = colors.textMuted,
            )
        }

        Column {
            ui.lines.forEachIndexed { index, line ->
                BudgetEditorRow(
                    line = line,
                    enabled = editable,
                    onChange = { onLineChange(index, it) },
                    onStep = { onLineStep(index, it) },
                )
            }
        }

        val difference = Money(abs(ui.totals.difference.minor)).formatKes()
        Column {
            PesaValueRow(stringResource(R.string.core_ui_budget_total), ui.totals.total.formatKes(), strong = true)
            PesaValueRow(
                stringResource(if (over) R.string.core_ui_budget_over_by else R.string.core_ui_budget_unallocated),
                difference,
                strong = true,
            )
        }

        if (over) {
            BudgetWarning(stringResource(R.string.core_ui_budget_over_alert, difference), type.bodySmall, assertive = true)
        }

        if (editable) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                PesaButton(stringResource(R.string.core_ui_budget_cancel), onCancel, style = PesaButtonStyle.Text)
                PesaButton(stringResource(R.string.core_ui_budget_review), onReview)
            }
        } else {
            val status = if (ui.status == BudgetStatus.Saved) {
                stringResource(R.string.core_ui_budget_status_saved, ui.month.formatLong())
            } else {
                stringResource(R.string.core_ui_budget_status_discarded)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(PesaIcons.LockSimple, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                Text(status, style = type.meta, color = colors.textMuted)
            }
        }
    }
}

/** Warn box with a warning icon. [assertive] makes it an alert: announced immediately when it appears. */
@Composable
internal fun BudgetWarning(text: String, style: TextStyle, assertive: Boolean) {
    val colors = PesaLensTheme.colors
    Surface(
        Modifier.fillMaxWidth().then(
            if (assertive) Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive } else Modifier,
        ),
        shape = PesaLensTheme.shapes.tile,
        color = colors.warnBackground,
        contentColor = colors.warnText,
        border = BorderStroke(1.dp, colors.warnBorder),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(PesaIcons.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text, style = style, color = colors.warnText)
        }
    }
}
