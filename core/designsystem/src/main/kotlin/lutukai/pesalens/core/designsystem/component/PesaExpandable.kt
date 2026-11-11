package lutukai.pesalens.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.icon.PesaIcons
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/** Disclosure row (like `<details>`): 56dp title row with a top border; content shows when expanded. */
@Composable
fun PesaExpandable(title: String, icon: ImageVector, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = PesaLensTheme.colors
    var expanded by rememberSaveable { mutableStateOf(false) }
    val border = colors.border
    Column(
        modifier.fillMaxWidth().drawBehind {
            val w = 1.dp.toPx()
            drawLine(border, Offset(0f, w / 2), Offset(size.width, w / 2), w)
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button) { expanded = !expanded }
                .semantics(mergeDescendants = true) {
                    // The exposed action reflects the state: Expand while collapsed, Collapse while expanded.
                    if (expanded) {
                        collapse { expanded = false; true }
                    } else {
                        expand { expanded = true; true }
                    }
                }
                .padding(horizontal = PesaSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Text(title, style = PesaLensTheme.type.body, color = colors.text, modifier = Modifier.weight(1f))
            // Like the prototype's <summary>, the caret keeps pointing down; the state is in the semantics actions.
            Icon(PesaIcons.CaretDown, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
        }
        AnimatedVisibility(expanded) { content() }
    }
}
