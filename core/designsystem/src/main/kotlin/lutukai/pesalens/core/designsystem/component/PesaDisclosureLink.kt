package lutukai.pesalens.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

/**
 * Inline disclosure like a bare `<details>`: accentText 14sp medium label with a leading icon, no divider or chevron.
 * The label row is 32dp visually inside a 48dp touch target; [content] follows 6dp below when expanded.
 */
@Composable
fun PesaDisclosureLink(title: String, icon: ImageVector, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = PesaLensTheme.colors
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(modifier) {
        Row(
            Modifier
                .heightIn(min = PesaSpacing.minTouchTarget)
                .clickable(role = Role.Button) { expanded = !expanded }
                .semantics(mergeDescendants = true) {
                    if (expanded) collapse { expanded = false; true } else expand { expanded = true; true }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(16.dp))
            Text(title, style = PesaLensTheme.type.label.copy(fontSize = 13.sp), color = colors.accentText)
        }
        AnimatedVisibility(expanded) { Column(Modifier.padding(top = 6.dp)) { content() } }
    }
}
