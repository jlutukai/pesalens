package lutukai.pesalens.core.designsystem.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2

enum class PesaWidthClass { Compact, Medium, Expanded }

@Immutable
data class PesaWindowInfo(val widthClass: PesaWidthClass, val isCompactHeight: Boolean)

private const val MediumMinWidthDp = 600
private const val ExpandedMinWidthDp = 840
private const val CompactHeightMaxDp = 480

/** Pure mapping from window size in dp to PesaLens' window info. */
fun pesaWindowInfo(widthDp: Int, heightDp: Int): PesaWindowInfo = PesaWindowInfo(
    widthClass = when {
        widthDp >= ExpandedMinWidthDp -> PesaWidthClass.Expanded
        widthDp >= MediumMinWidthDp -> PesaWidthClass.Medium
        else -> PesaWidthClass.Compact
    },
    isCompactHeight = heightDp < CompactHeightMaxDp,
)

@Composable
fun rememberPesaWindowInfo(): PesaWindowInfo {
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return remember(sizeClass) {
        pesaWindowInfo(widthDp = sizeClass.minWidthDp, heightDp = sizeClass.minHeightDp)
    }
}
