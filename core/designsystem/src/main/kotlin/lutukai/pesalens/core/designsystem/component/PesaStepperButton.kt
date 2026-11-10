package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/** Outlined +/- stepper, 48dp tall; [width] is 48dp by default and 44dp in dense rows. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaStepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    width: Dp = 48.dp,
    shape: Shape = PesaLensTheme.shapes.button,
) {
    val colors = PesaLensTheme.colors
    // The visual is [width] x 48; the layout box is at least 48dp wide so the touch target never
    // overlaps neighbours, with the visual centred in it.
    Box(
        Modifier.size(maxOf(width, PesaSpacing.minTouchTarget), PesaSpacing.minTouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = Modifier.size(width, PesaSpacing.minTouchTarget),
            enabled = enabled,
            shapes = IconButtonDefaults.shapes(shape = shape, pressedShape = RoundedCornerShape(6.dp)),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.text,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.text.copy(alpha = 0.45f),
            ),
            border = BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.45f)),
        ) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}
