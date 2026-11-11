package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/**
 * Digits-only amount input. [value] and [onValueChange] carry digits only ("30000"); the field
 * displays them grouped ("30,000") through [ThousandsGrouping], which keeps the caret mapped.
 */
@Composable
fun PesaAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    contentDescription: String? = null,
    textAlign: TextAlign = TextAlign.Start,
    prefix: String? = "KSh",
    containerColor: Color = PesaLensTheme.colors.surface,
    shape: Shape = PesaLensTheme.shapes.input,
    horizontalPadding: Dp = 14.dp,
    textStyle: TextStyle = PesaLensTheme.type.body,
    prefixStyle: TextStyle = PesaLensTheme.type.body,
    prefixGap: Dp = 6.dp,
) {
    val colors = PesaLensTheme.colors
    val borderColor = if (isError) colors.warnBorder else colors.border
    BasicTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.filter { it in '0'..'9' }) },
        modifier = modifier
            .fillMaxWidth()
            .height(PesaSpacing.minTouchTarget)
            .background(containerColor, shape)
            .border(1.dp, borderColor, shape)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        enabled = enabled,
        textStyle = textStyle.copy(color = colors.text, textAlign = textAlign),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        visualTransformation = ThousandsGrouping,
        cursorBrush = SolidColor(colors.accent),
        decorationBox = { inner ->
            Row(
                Modifier.padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prefix != null) {
                    Text(prefix, style = prefixStyle, color = colors.textMuted, modifier = Modifier.padding(end = prefixGap))
                }
                Box(
                    Modifier.weight(1f),
                    contentAlignment = if (textAlign == TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = textStyle,
                            color = colors.placeholder,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    inner()
                }
            }
        },
    )
}

/** Inserts en-US thousands separators into a digit string, with a caret-preserving offset map. */
internal object ThousandsGrouping : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val n = digits.length
        val out = StringBuilder()
        // originalToTransformed[o] is the caret position after the first o digits (after any separator).
        val originalToTransformed = IntArray(n + 1)
        digits.forEachIndexed { i, c ->
            if (i > 0 && (n - i) % 3 == 0) out.append(',')
            originalToTransformed[i] = out.length
            out.append(c)
        }
        originalToTransformed[n] = out.length
        val transformed = out.toString()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = originalToTransformed[offset.coerceIn(0, n)]
            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, transformed.length)
                return t - transformed.substring(0, t).count { it == ',' }
            }
        }
        return TransformedText(AnnotatedString(transformed), mapping)
    }
}
