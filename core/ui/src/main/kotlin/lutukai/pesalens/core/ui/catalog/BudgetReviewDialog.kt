package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.component.PesaAlertDialog
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaButtonStyle
import lutukai.pesalens.core.designsystem.component.PesaValueRow
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong
import kotlin.math.abs

/** "Save this budget?" with income, every line, the totals and, when over income, a (non-alert) warning. The body scrolls when the screen is short. */
@Composable
fun BudgetReviewDialog(ui: BudgetUi, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = PesaLensTheme.colors
    val over = ui.totals.isOver
    PesaAlertDialog(
        title = stringResource(R.string.core_ui_budget_dialog_title),
        onDismiss = onDismiss,
        subtitle = stringResource(R.string.core_ui_budget_dialog_subtitle, ui.month.formatLong()),
        dismiss = { PesaButton(stringResource(R.string.core_ui_budget_dialog_back), onDismiss, style = PesaButtonStyle.Text) },
        confirm = { PesaButton(stringResource(R.string.core_ui_budget_dialog_confirm), onConfirm, style = PesaButtonStyle.Tonal) },
    ) {
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                PesaValueRow(stringResource(R.string.core_ui_budget_income_label), ui.income.formatKes(), minHeight = 34.dp, topBorder = Color.Transparent)
                ui.lines.forEach { line ->
                    ReviewRow(line.label, line.limit.formatKes(), minHeight = 32.dp, topBorder = colors.border, strong = false)
                }
                ReviewRow(stringResource(R.string.core_ui_budget_total), ui.totals.total.formatKes(), 36.dp, colors.text, strong = true)
                ReviewRow(
                    stringResource(if (over) R.string.core_ui_budget_over_by else R.string.core_ui_budget_unallocated),
                    Money(abs(ui.totals.difference.minor)).formatKes(),
                    34.dp,
                    Color.Transparent,
                    strong = true,
                )
            }
            if (over) BudgetWarning(stringResource(R.string.core_ui_budget_dialog_over), PesaLensTheme.type.meta, assertive = false)
        }
    }
}

/** Label and value on one line, with a 1dp top border; unlike [PesaValueRow] the label is text-coloured. */
@Composable
private fun ReviewRow(label: String, value: String, minHeight: Dp, topBorder: Color, strong: Boolean) {
    val colors = PesaLensTheme.colors
    val weight = if (strong) FontWeight.Medium else FontWeight.Normal
    val style = PesaLensTheme.type.bodySmall.copy(fontWeight = weight)
    Row(
        Modifier.fillMaxWidth().topBorder(topBorder).heightIn(min = minHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = style, color = colors.text, modifier = Modifier.weight(1f))
        Text(value, style = style.copy(fontFeatureSettings = "tnum"), color = colors.text)
    }
}
