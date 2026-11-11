package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

/**
 * Surface card: 1dp border, card shape, 16dp padding and 14dp gap by default. The padding is measured inside the border,
 * as in CSS, so the content sits [contentPadding] + 1dp from the edge.
 */
@Composable
fun PesaCard(
    modifier: Modifier = Modifier,
    border: Color = PesaLensTheme.colors.border,
    contentPadding: Dp = 16.dp,
    spacing: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = PesaLensTheme.shapes.card,
        color = PesaLensTheme.colors.surface,
        contentColor = PesaLensTheme.colors.text,
        border = BorderStroke(1.dp, border),
    ) {
        Column(Modifier.padding(contentPadding + 1.dp), verticalArrangement = Arrangement.spacedBy(spacing), content = content)
    }
}
