package lutukai.pesalens.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

data class SegmentOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
)

/**
 * Radio-group segmented control: a 4dp-padded surfaceAlt track holding equal-width toggle buttons.
 * Each option exposes [Role.RadioButton] and a selected state.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> PesaSegmentedControl(
    options: ImmutableList<SegmentOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = PesaLensTheme.colors
    val shapes = PesaLensTheme.shapes
    Row(
        modifier = modifier
            .background(colors.surfaceAlt, shapes.segment)
            .border(1.dp, colors.border, shapes.segment)
            .padding(PesaSpacing.xs)
            .selectableGroup()
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(PesaSpacing.xs),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onSelect(option.value) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .semantics {
                        role = Role.RadioButton
                        this.selected = isSelected
                    },
                enabled = option.enabled,
                shapes = ToggleButtonShapes(
                    shape = shapes.segmentItem,
                    pressedShape = RoundedCornerShape(6.dp),
                    checkedShape = shapes.segmentItem,
                ),
                colors = ToggleButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = colors.text,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = colors.text.copy(alpha = 0.45f),
                    checkedContainerColor = colors.surface,
                    checkedContentColor = colors.accentText,
                ),
                border = BorderStroke(1.dp, if (isSelected) colors.accent else Color.Transparent),
                contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
                Icon(option.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(option.label, style = PesaLensTheme.type.label)
            }
        }
    }
}
