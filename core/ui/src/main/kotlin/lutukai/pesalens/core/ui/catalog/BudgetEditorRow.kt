package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.component.PesaAmountField
import lutukai.pesalens.core.designsystem.component.PesaStepperButton
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.BudgetLineUi
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.catalog.model.shillingDigits

private val RowShape = RoundedCornerShape(10.dp)

/**
 * One category limit: swatch, label, [-], 80dp field, [+], on a 56dp+ row with a top border. The component reports taps
 * as a [StepDirection] (the caller applies the 500 step); [enabled] = false locks both buttons and the field.
 */
@Composable
fun BudgetEditorRow(
    line: BudgetLineUi,
    enabled: Boolean,
    onChange: (String) -> Unit,
    onStep: (StepDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    val label: @Composable (Modifier) -> Unit = { m ->
        Text(line.label, style = PesaLensTheme.type.bodySmall.copy(lineHeight = 17.5.sp), color = colors.text, modifier = m)
    }
    val controls: @Composable () -> Unit = {
        PesaStepperButton(
            PesaIcons.Minus,
            stringResource(R.string.core_ui_budget_line_decrease, line.label),
            onClick = { onStep(StepDirection.Down) },
            enabled = enabled && line.limit.minor > 0,
            width = 44.dp,
            shape = RowShape,
        )
        PesaAmountField(
            value = line.limit.shillingDigits(),
            onValueChange = onChange,
            placeholder = "0",
            modifier = Modifier.width(fieldWidth(80.dp)),
            enabled = enabled,
            contentDescription = stringResource(R.string.core_ui_budget_line_description, line.label),
            textAlign = TextAlign.Center,
            prefix = null,
            containerColor = colors.surfaceAlt,
            shape = RowShape,
            horizontalPadding = 4.dp,
            textStyle = PesaLensTheme.type.bodySmall.copy(fontFeatureSettings = "tnum"),
        )
        PesaStepperButton(
            PesaIcons.Plus,
            stringResource(R.string.core_ui_budget_line_increase, line.label),
            onClick = { onStep(StepDirection.Up) },
            enabled = enabled,
            width = 44.dp,
            shape = RowShape,
        )
    }
    if (LocalDensity.current.fontScale >= StackedFontScale) {
        // Large text: the name gets its own full-width line so it never breaks mid-word beside the stepper.
        Column(modifier.fillMaxWidth().topBorder(colors.border).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                PatternSwatch(line.tone, 12.dp)
                label(Modifier.weight(1f))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) { controls() }
        }
    } else {
        Row(
            modifier.fillMaxWidth().topBorder(colors.border).heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PatternSwatch(line.tone, 12.dp)
            label(Modifier.weight(1f))
            controls()
        }
    }
}

/** From this font scale the budget rows stack the name above the controls. */
internal const val StackedFontScale = 1.5f
