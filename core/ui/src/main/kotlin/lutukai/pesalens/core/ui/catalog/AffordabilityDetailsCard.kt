package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.component.PesaAmountField
import lutukai.pesalens.core.designsystem.component.PesaButton
import lutukai.pesalens.core.designsystem.component.PesaCard
import lutukai.pesalens.core.designsystem.component.PesaKicker
import lutukai.pesalens.core.designsystem.component.PesaSuggestionChip
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.AffordabilityFormUi
import lutukai.pesalens.core.ui.format.formatKes

/**
 * Asks for the two amounts the estimate needs. Missing fields show a warn border and a hint once the user has tried to
 * submit; after [AffordabilityFormUi.submitted] the fields lock and the actions go away.
 */
@Composable
fun AffordabilityDetailsCard(
    form: AffordabilityFormUi,
    onAvailableChange: (String) -> Unit,
    onMonthlyChange: (String) -> Unit,
    onUseSample: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    PesaCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            PesaKicker(stringResource(R.string.core_ui_details_kicker, form.price.formatKes()))
            Text(stringResource(R.string.core_ui_details_title), style = type.title, color = colors.text)
            Text(stringResource(R.string.core_ui_details_body), style = type.bodySmall, color = colors.textMuted)
        }
        AmountInput(
            label = stringResource(R.string.core_ui_details_available_label),
            placeholder = stringResource(R.string.core_ui_details_available_placeholder),
            value = form.available,
            onChange = onAvailableChange,
            missing = form.availableMissing,
            missingText = stringResource(R.string.core_ui_details_available_missing),
            enabled = !form.submitted,
        )
        AmountInput(
            label = stringResource(R.string.core_ui_details_monthly_label),
            placeholder = stringResource(R.string.core_ui_details_monthly_placeholder),
            value = form.monthly,
            onChange = onMonthlyChange,
            missing = form.monthlyMissing,
            missingText = stringResource(R.string.core_ui_details_monthly_missing),
            enabled = !form.submitted,
        )
        if (!form.submitted) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PesaSuggestionChip(stringResource(R.string.core_ui_details_use_sample), onUseSample, containerColor = Color.Transparent)
                PesaButton(stringResource(R.string.core_ui_details_submit), onSubmit, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun AmountInput(
    label: String,
    placeholder: String,
    value: String,
    onChange: (String) -> Unit,
    missing: Boolean,
    missingText: String,
    enabled: Boolean,
) {
    val colors = PesaLensTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = PesaLensTheme.type.bodySmall, color = colors.text)
        PesaAmountField(
            value = value,
            onValueChange = onChange,
            placeholder = placeholder,
            enabled = enabled,
            isError = missing,
            contentDescription = label,
            containerColor = colors.surfaceAlt,
            horizontalPadding = 12.dp,
            textStyle = PesaLensTheme.type.body.copy(fontSize = 16.sp, fontFeatureSettings = "tnum"),
            prefixStyle = PesaLensTheme.type.bodySmall,
        )
        if (missing) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(PesaIcons.WarningCircle, contentDescription = null, tint = colors.warnText, modifier = Modifier.size(14.dp))
                Text(missingText, style = PesaLensTheme.type.meta, color = colors.warnText)
            }
        }
    }
}
