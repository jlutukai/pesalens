package lutukai.pesalens.core.designsystem.pattern

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/** Non-color encoding for chart series, drawn in `patternInk` over the series color. */
enum class ChartPattern {
    Solid, Diagonal, Dots, Horizontal, ReverseDiagonal, Vertical;

    companion object {
        fun forTone(tone: Int): ChartPattern = entries[tone]
    }
}

private const val StripePeriodDp = 7f
private const val LinePeriodDp = 6f
private const val InkWidthDp = 2f
private const val DotRadiusDp = 1.3f
private const val DotGridDp = 6f

/** Fills the bounds with [base], then overlays [pattern] in [ink]. */
fun Modifier.patternFill(pattern: ChartPattern, base: Color, ink: Color): Modifier =
    background(base).drawWithContent {
        drawContent()
        when (pattern) {
            ChartPattern.Solid -> Unit
            ChartPattern.Diagonal -> stripes(ink, angle = 45f, periodDp = StripePeriodDp)
            ChartPattern.ReverseDiagonal -> stripes(ink, angle = -45f, periodDp = StripePeriodDp)
            ChartPattern.Horizontal -> stripes(ink, angle = 0f, periodDp = LinePeriodDp)
            ChartPattern.Vertical -> stripes(ink, angle = 90f, periodDp = LinePeriodDp)
            ChartPattern.Dots -> dots(ink)
        }
    }

/** Stripes whose direction is [angle] degrees from horizontal, repeated every [periodDp] across the line normal. */
private fun DrawScope.stripes(ink: Color, angle: Float, periodDp: Float) {
    val period = periodDp.dp.toPx()
    val width = InkWidthDp.dp.toPx()
    // Draw horizontal lines over a square large enough to cover the bounds after rotation, then rotate.
    val reach = hypot(size.width, size.height)
    val cx = size.width / 2f
    val cy = size.height / 2f
    clipRect {
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            var y = cy - reach / 2f
            while (y <= cy + reach / 2f) {
                drawRect(
                    color = ink,
                    topLeft = Offset(cx - reach / 2f, y),
                    size = Size(reach, width),
                )
                y += period
            }
        }
    }
}

private fun DrawScope.dots(ink: Color) {
    val grid = DotGridDp.dp.toPx()
    val radius = DotRadiusDp.dp.toPx()
    var y = grid / 2f
    while (y < size.height + grid) {
        var x = grid / 2f
        while (x < size.width + grid) {
            drawCircle(color = ink, radius = radius, center = Offset(x, y))
            x += grid
        }
        y += grid
    }
}
