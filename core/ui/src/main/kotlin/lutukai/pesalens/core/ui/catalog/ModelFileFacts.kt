package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.component.PesaPill
import lutukai.pesalens.core.designsystem.component.PillTone
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.ui.R

/** Model name, download size and free space needed. The sizes carry a dashed "illustrative" tag: they are placeholders. */
@Composable
fun ModelFileFacts(modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    Surface(
        modifier.fillMaxWidth(),
        shape = PesaLensTheme.shapes.tileLarge,
        color = colors.surface,
        contentColor = colors.text,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FactRow(stringResource(R.string.core_ui_model_facts_model_label)) {
                Text(stringResource(R.string.core_ui_model_facts_model_name), style = PesaLensTheme.type.bodySmall, color = colors.text)
            }
            FactRow(stringResource(R.string.core_ui_model_facts_download_label)) {
                Illustrative(stringResource(R.string.core_ui_model_facts_download_value))
            }
            FactRow(stringResource(R.string.core_ui_model_facts_space_label)) {
                Illustrative(stringResource(R.string.core_ui_model_facts_space_value))
            }
            Text(stringResource(R.string.core_ui_model_facts_footnote), style = SmallLabel.copy(lineHeight = 17.4.sp), color = colors.textMuted)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactRow(label: String, value: @Composable () -> Unit) {
    // Wraps the value under the label when large text leaves no room for both on one line.
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = PesaLensTheme.type.bodySmall, color = PesaLensTheme.colors.textMuted, modifier = Modifier.weight(1f))
        value()
    }
}

@Composable
private fun Illustrative(value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(value, style = PesaLensTheme.type.bodySmall, color = PesaLensTheme.colors.text)
        PesaPill(stringResource(R.string.core_ui_model_facts_illustrative), PillTone.Dashed, height = 20.dp)
    }
}
