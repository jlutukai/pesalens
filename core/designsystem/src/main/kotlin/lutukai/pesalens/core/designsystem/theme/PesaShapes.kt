package lutukai.pesalens.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class PesaShapes(
    val card: Shape = RoundedCornerShape(18.dp),
    val dialog: Shape = RoundedCornerShape(24.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val button: Shape = RoundedCornerShape(12.dp),
    val buttonLarge: Shape = RoundedCornerShape(14.dp),
    val chip: Shape = RoundedCornerShape(20.dp),
    val input: Shape = RoundedCornerShape(12.dp),
    val composer: Shape = RoundedCornerShape(24.dp),
    val segment: Shape = RoundedCornerShape(14.dp),
    val segmentItem: Shape = RoundedCornerShape(10.dp),
    val tile: Shape = RoundedCornerShape(12.dp),
    val tileLarge: Shape = RoundedCornerShape(16.dp),
    val pill: Shape = RoundedCornerShape(11.dp),
    val bubbleBot: Shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
    val bubbleUser: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 4.dp, bottomStart = 16.dp),
    val bar: Shape = RoundedCornerShape(7.dp),
    val track: Shape = RoundedCornerShape(5.dp),
)
