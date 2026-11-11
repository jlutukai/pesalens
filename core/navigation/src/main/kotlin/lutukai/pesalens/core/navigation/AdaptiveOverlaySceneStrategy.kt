package lutukai.pesalens.core.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import lutukai.pesalens.core.designsystem.adaptive.PesaWidthClass
import lutukai.pesalens.core.designsystem.adaptive.rememberPesaWindowInfo
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.designsystem.theme.PesaSpacing

private val OverlayPadding = 22.dp
private val HandleGap = 14.dp
private val HandleWidth = 36.dp
private val HandleHeight = 4.dp
private val BorderWidth = 1.dp
private val DialogMaxWidth = 560.dp
private val DialogShadow = 16.dp

/**
 * Shows the top entry over the entries beneath it when its metadata carries [PesaPanes.overlay]: a modal bottom
 * sheet when [useSheet], otherwise a centred dialog. The scene draws all chrome (shape, surface, border, drag
 * handle, padding, scrim), so the entry's content is only the body. Dismissing either calls the scope's `onBack`.
 *
 * List it before the list-detail strategy in `NavDisplay(sceneStrategies = …)`.
 */
class AdaptiveOverlaySceneStrategy<T : Any>(private val useSheet: Boolean) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val top = entries.lastOrNull() ?: return null
        if (PesaPanes.OverlayKey !in top.metadata) return null
        // An overlay must sit on something: Nav3 rejects an OverlayScene whose overlaidEntries are empty.
        if (entries.size < 2) return null
        return AdaptiveOverlayScene(entry = top, previousEntries = entries.dropLast(1), useSheet = useSheet, onBack = onBack)
    }
}

/** [AdaptiveOverlaySceneStrategy] that uses a sheet on compact widths and a dialog on medium and expanded ones. */
@Composable
fun <T : Any> rememberAdaptiveOverlaySceneStrategy(): AdaptiveOverlaySceneStrategy<T> {
    val useSheet = rememberPesaWindowInfo().widthClass == PesaWidthClass.Compact
    return remember(useSheet) { AdaptiveOverlaySceneStrategy(useSheet) }
}

@OptIn(ExperimentalMaterial3Api::class)
private class AdaptiveOverlayScene<T : Any>(
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    private val useSheet: Boolean,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    // The mode is part of the key so a width-class change replaces a sheet with a dialog (and back) instead of
    // NavDisplay keeping the scene it first showed under the same key.
    override val key: Any = entry.contentKey to useSheet

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val overlaidEntries: List<NavEntry<T>> = previousEntries

    private var sheetState: SheetState? = null

    override val content: @Composable () -> Unit = {
        // Sheet and dialog live in their own windows, whose compositions provide the activity's lifecycle
        // owner; hand the entry the scene's owner instead, as Nav3's DialogScene does.
        val lifecycleOwner = rememberLifecycleOwner()
        val body: @Composable () -> Unit = {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) { entry.Content() }
        }
        if (useSheet) {
            val state = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            )
            sheetState = state
            OverlaySheet(state, onDismiss = onBack, body = body)
        } else {
            OverlayDialog(onDismiss = onBack, body = body)
        }
    }

    /** Runs when the entry is popped from the back stack: slide the sheet away before it leaves composition. */
    override suspend fun onRemove() {
        sheetState?.hide()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdaptiveOverlayScene<*>) return false
        return key == other.key && entry == other.entry && previousEntries == other.previousEntries
    }

    override fun hashCode(): Int = (key.hashCode() * 31 + entry.hashCode()) * 31 + previousEntries.hashCode()
}

/** The prototype's bottom sheet: 28dp top corners, surface, 1dp top border, 36×4 handle, 12/22/22 padding. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlaySheet(state: SheetState, onDismiss: () -> Unit, body: @Composable () -> Unit) {
    val colors = PesaLensTheme.colors
    val shape = PesaLensTheme.shapes.sheet
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = shape,
        containerColor = colors.surface,
        contentColor = colors.text,
        scrimColor = colors.scrim,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .topBorder(shape, colors.border)
                // The top padding sits under the 1dp top border, as in CSS.
                .padding(start = OverlayPadding, end = OverlayPadding, top = PesaSpacing.md + BorderWidth, bottom = OverlayPadding),
        ) {
            // Decorative, as in the prototype: the whole sheet drags, and the scrim and back dismiss it.
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(HandleWidth, HandleHeight)
                    .background(colors.border, CircleShape),
            )
            Spacer(Modifier.height(HandleGap))
            body()
        }
    }
}

/** The prototype's centred dialog: at most 560dp wide, dialog shape, surface, 1dp border, on the scrim token. */
@Composable
private fun OverlayDialog(onDismiss: () -> Unit, body: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        // The scrim is drawn below, so the window's default dim is zeroed to avoid doubling it.
        val view = LocalView.current
        SideEffect { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }
        val colors = PesaLensTheme.colors
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.scrim)
                .pointerInput(onDismiss) { detectTapGestures(onTap = { onDismiss() }) }
                .padding(PesaSpacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = DialogMaxWidth)
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures { } },
                shape = PesaLensTheme.shapes.dialog,
                color = colors.surface,
                contentColor = colors.text,
                border = BorderStroke(BorderWidth, colors.border),
                shadowElevation = DialogShadow,
            ) {
                Box(Modifier.padding(OverlayPadding + BorderWidth)) { body() }
            }
        }
    }
}

/**
 * A 1dp border along the top edge of [shape] only, like CSS `border-top` on a rounded box: the shape's outline is
 * drawn with its sides and bottom pushed outside the clip.
 */
private fun Modifier.topBorder(shape: Shape, color: Color): Modifier = drawWithCache {
    val stroke = BorderWidth.toPx()
    val outline = shape.createOutline(Size(size.width + 2 * stroke, size.height + 2 * stroke), layoutDirection, this)
    onDrawBehind {
        clipRect {
            translate(left = -stroke, top = stroke / 2) { drawOutline(outline, color, style = Stroke(stroke)) }
        }
    }
}
