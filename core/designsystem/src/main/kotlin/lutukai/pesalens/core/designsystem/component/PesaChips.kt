package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

/** The prototype's 14px side padding plus its 1px border, which CSS draws outside the padding. */
private val ChipPadding = PaddingValues(horizontal = 15.dp)

/**
 * Suggestion chip: 40dp tall, chip shape, 1dp border, accentText label. [containerColor] is the surface by default; the
 * details form's "Use sample values" passes transparent.
 */
@Composable
fun PesaSuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PesaLensTheme.colors.surface,
) {
    val colors = PesaLensTheme.colors
    SuggestionChip(
        onClick = onClick,
        label = { Text(text, style = PesaLensTheme.type.label) },
        modifier = modifier.heightIn(min = 40.dp),
        shape = PesaLensTheme.shapes.chip,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = colors.accentText,
        ),
        border = BorderStroke(1.dp, colors.border),
        horizontalArrangement = Arrangement.Start,
        contentPadding = ChipPadding,
    )
}
