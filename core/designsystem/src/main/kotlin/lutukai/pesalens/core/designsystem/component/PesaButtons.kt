package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaColors
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

enum class PesaButtonStyle {
    /** 1dp accent border, accentText label, transparent. */
    Outlined,

    /** tintSubtle fill with a 1.5dp accent border. */
    Tonal,

    /** Borderless, text color. */
    Text,

    /** Borderless, textMuted, accentText while pressed. */
    TextMuted,

    /** 1dp border, text color. */
    Neutral,

    /** warnBackground fill, warnBorder, warnText. */
    Warn,
}

internal const val DISABLED_ALPHA = 0.45f

private val LargeHeight = 52.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PesaButtonStyle = PesaButtonStyle.Outlined,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    large: Boolean = false,
) {
    val colors = PesaLensTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val look = style.look(colors, pressed)
    val restShape = if (large) PesaLensTheme.shapes.buttonLarge else PesaLensTheme.shapes.button
    // The pressed shape is a tighter radius; M3 morphs between the two.
    val pressedShape = RoundedCornerShape(if (large) 8.dp else 6.dp)
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = if (large) LargeHeight else PesaSpacing.minTouchTarget),
        enabled = enabled,
        shapes = ButtonDefaults.shapes(shape = restShape, pressedShape = pressedShape),
        colors = ButtonColors(
            containerColor = look.container,
            contentColor = look.content,
            disabledContainerColor = look.container.copy(alpha = look.container.alpha * DISABLED_ALPHA),
            disabledContentColor = look.content.copy(alpha = DISABLED_ALPHA),
        ),
        border = if (look.borderWidth > 0.dp) {
            BorderStroke(look.borderWidth, look.borderColor.copy(alpha = look.borderColor.alpha * if (enabled) 1f else DISABLED_ALPHA))
        } else {
            null
        },
        contentPadding = PaddingValues(horizontal = PesaSpacing.lg),
        interactionSource = interaction,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(PesaSpacing.sm))
        }
        Text(text, style = PesaLensTheme.type.label)
    }
}

private class ButtonLook(val container: Color, val content: Color, val borderWidth: Dp = 0.dp, val borderColor: Color = Color.Transparent)

private fun PesaButtonStyle.look(colors: PesaColors, pressed: Boolean): ButtonLook = when (this) {
    PesaButtonStyle.Outlined -> ButtonLook(Color.Transparent, colors.accentText, 1.dp, colors.accent)
    PesaButtonStyle.Tonal -> ButtonLook(colors.tintSubtle, colors.accentText, 1.5.dp, colors.accent)
    PesaButtonStyle.Text -> ButtonLook(Color.Transparent, colors.text)
    PesaButtonStyle.TextMuted -> ButtonLook(Color.Transparent, if (pressed) colors.accentText else colors.textMuted)
    PesaButtonStyle.Neutral -> ButtonLook(Color.Transparent, colors.text, 1.dp, colors.border)
    PesaButtonStyle.Warn -> ButtonLook(colors.warnBackground, colors.warnText, 1.dp, colors.warnBorder)
}
