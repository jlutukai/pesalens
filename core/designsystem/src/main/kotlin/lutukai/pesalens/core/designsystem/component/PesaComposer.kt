package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/** Chat composer: a 48dp pill text field and a 48dp circular send button. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    placeholder: String,
    sendContentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    val type = PesaLensTheme.type
    val shape = PesaLensTheme.shapes.composer
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(PesaSpacing.minTouchTarget)
                .background(colors.surface, shape)
                .border(1.dp, colors.border, shape)
                .semantics { contentDescription = placeholder },
            textStyle = type.body.copy(color = colors.text),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (sendEnabled) onSend() }),
            singleLine = true,
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { inner ->
                // 18dp padding inside the 1dp border, as in CSS.
                Box(Modifier.padding(horizontal = 19.dp), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = type.body, color = colors.placeholder)
                    }
                    inner()
                }
            },
        )
        OutlinedIconButton(
            onClick = onSend,
            modifier = Modifier.size(PesaSpacing.minTouchTarget),
            enabled = sendEnabled,
            shapes = IconButtonDefaults.shapes(shape = shape, pressedShape = RoundedCornerShape(16.dp)),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = colors.tintSubtle,
                contentColor = colors.accentText,
                disabledContainerColor = colors.tintSubtle.copy(alpha = 0.45f),
                disabledContentColor = colors.accentText.copy(alpha = 0.45f),
            ),
            border = BorderStroke(1.dp, if (sendEnabled) colors.accent else colors.accent.copy(alpha = 0.45f)),
        ) {
            Icon(PesaIcons.PaperPlaneRight, contentDescription = sendContentDescription, modifier = Modifier.size(20.dp))
        }
    }
}
