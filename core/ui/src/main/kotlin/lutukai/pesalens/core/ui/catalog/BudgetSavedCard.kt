package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.format.formatLong

/** Confirmation after a save: "Budget saved for November 2026", then [summary] and the on-device note. */
@Composable
fun BudgetSavedCard(month: YearMonth, summary: String, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    PesaCard(
        modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        border = colors.accent,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(PesaIcons.CheckCircleFill, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(26.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.core_ui_budget_saved_title, month.formatLong()), style = PesaLensTheme.type.subtitle, color = colors.text)
                Text(stringResource(R.string.core_ui_budget_saved_body, summary), style = PesaLensTheme.type.bodySmall, color = colors.textMuted)
            }
        }
    }
}
