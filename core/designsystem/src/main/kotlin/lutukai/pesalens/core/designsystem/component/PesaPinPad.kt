package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme

private val KeyWidth = 76.dp
private val KeyHeight = 64.dp
private val RowGap = 12.dp
private val ColumnGap = 28.dp

/** Width of [PesaPinPad]: three keys and two column gaps. Layouts that place it beside other content need this much. */
val PesaPinPadWidth = KeyWidth * 3 + ColumnGap * 2
private val KeyShape = RoundedCornerShape(32.dp)
private val KeyPressedShape = RoundedCornerShape(16.dp)

/** Four PIN dots, 16dp with a 2dp border, spaced 18dp. Decorative: hidden from accessibility. */
@Composable
fun PesaPinDots(filled: Int, isError: Boolean, modifier: Modifier = Modifier) {
    val colors = PesaLensTheme.colors
    val ring = if (isError) colors.warnText else colors.accent
    Row(
        modifier = modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        repeat(4) { i ->
            Box(
                Modifier
                    .size(16.dp)
                    .background(if (i < filled) colors.accent else Color.Transparent, CircleShape)
                    .border(2.dp, ring, CircleShape),
            )
        }
    }
}

/** 3x4 numeric keypad: 76x64 round keys, gaps 12 (rows) and 28 (columns), borderless delete key. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaPinPad(
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    deleteContentDescription: String,
    enabled: Boolean = true,
) {
    val colors = PesaLensTheme.colors
    val keyShapes = ButtonDefaults.shapes(shape = KeyShape, pressedShape = KeyPressedShape)
    val digitStyle = PesaLensTheme.type.titleLarge.copy(fontSize = 26.sp)
    Column(verticalArrangement = Arrangement.spacedBy(RowGap)) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ColumnGap)) {
                row.forEach { digit ->
                    DigitKey(digit, enabled, onDigit, keyShapes, colors.surface, colors.text, colors.border, digitStyle)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ColumnGap)) {
            Box(Modifier.size(KeyWidth, KeyHeight))
            DigitKey('0', enabled, onDigit, keyShapes, colors.surface, colors.text, colors.border, digitStyle)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(KeyWidth, KeyHeight),
                enabled = enabled,
                shapes = IconButtonDefaults.shapes(shape = KeyShape, pressedShape = KeyPressedShape),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = colors.text,
                    disabledContentColor = colors.text.copy(alpha = 0.45f),
                ),
            ) {
                Icon(PesaIcons.Backspace, contentDescription = deleteContentDescription, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DigitKey(
    digit: Char,
    enabled: Boolean,
    onDigit: (Char) -> Unit,
    shapes: ButtonShapes,
    container: Color,
    content: Color,
    border: Color,
    style: TextStyle,
) {
    Button(
        onClick = { onDigit(digit) },
        modifier = Modifier.size(KeyWidth, KeyHeight),
        enabled = enabled,
        shapes = shapes,
        colors = ButtonColors(container, content, container.copy(alpha = 0.45f), content.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, if (enabled) border else border.copy(alpha = 0.45f)),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(digit.toString(), style = style)
    }
}
