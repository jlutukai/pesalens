package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

/** Suggestion chip: 40dp tall, chip shape, surface fill, 1dp border, accentText label. */
@Composable
fun PesaSuggestionChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    SuggestionChip(
        onClick = onClick,
        label = { Text(text, style = PesaLensTheme.type.label, modifier = Modifier.padding(horizontal = 6.dp)) },
        modifier = modifier.heightIn(min = 40.dp),
        shape = PesaLensTheme.shapes.chip,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = colors.surface,
            labelColor = colors.accentText,
        ),
        border = BorderStroke(1.dp, colors.border),
    )
}
