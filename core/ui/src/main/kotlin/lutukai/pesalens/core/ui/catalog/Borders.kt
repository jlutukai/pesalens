package lutukai.pesalens.core.ui.catalog

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 1dp line along the top edge, the prototype's `border-top` between stacked rows. */
internal fun Modifier.topBorder(color: Color): Modifier = drawBehind {
    val w = 1.dp.toPx()
    drawLine(color, Offset(0f, w / 2), Offset(size.width, w / 2), w)
}

/** Grows a fixed field width by half of any font scale above 1, so large text isn't clipped inside a fixed-width field. */
@androidx.compose.runtime.Composable
internal fun fieldWidth(base: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp {
    val scale = androidx.compose.ui.platform.LocalDensity.current.fontScale
    return base * (1f + (scale - 1f).coerceAtLeast(0f) / 2f)
}
