package lutukai.pesalens.feature.transactions.impl

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsContent
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DrawAttempts = 10

/** A phone tall enough to show the whole screen without scrolling, so a shot compares to the prototype top to bottom. */
private const val TallPhone = "w412dp-h1000dp-420dpi"

/**
 * Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`. The shots are split over small
 * classes because Robolectric's native renderer returned undrawn frames once a class took too many of them.
 */
abstract class TransactionsScreenshots {
    @get:Rule
    val compose = createComposeRule()

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(): Boolean {
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

    protected fun shot(name: String, dark: Boolean, state: TransactionsUiState) = capture(name + if (dark) "_dark" else "_light", dark) {
        TransactionsContent(state, {})
    }

    private fun capture(name: String, dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { content() } }
        for (attempt in 1..DrawAttempts) {
            compose.waitForIdle()
            if (hasDrawn()) break
            // Nothing invalidates a frame the renderer dropped, so ask for another one.
            compose.runOnUiThread { (compose.onRoot().fetchSemanticsNode().root as? View)?.invalidate() }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class TransactionsContentScreenshotTest : TransactionsScreenshots() {
    @Test fun content_light() = shot("transactions_content", false, foodContent())
    @Test fun content_dark() = shot("transactions_content", true, foodContent())
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class TransactionsStatesScreenshotTest : TransactionsScreenshots() {
    @Test fun loading_light() = shot("transactions_loading", false, TransactionsUiState.Loading)
    @Test fun loading_dark() = shot("transactions_loading", true, TransactionsUiState.Loading)
    @Test fun error_light() = shot("transactions_error", false, TransactionsUiState.Error(AppError.NotConnected))
    @Test fun error_dark() = shot("transactions_error", true, TransactionsUiState.Error(AppError.NotConnected))
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class TransactionsEmptyScreenshotTest : TransactionsScreenshots() {
    @Test fun empty_light() = shot("transactions_empty", false, TransactionsUiState.Empty("Food and groceries", november))
    @Test fun empty_dark() = shot("transactions_empty", true, TransactionsUiState.Empty("Food and groceries", november))
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class TransactionsLandscapeScreenshotTest : TransactionsScreenshots() {
    @Test fun content_light() = shot("transactions_landscape", false, foodContent())
    @Test fun content_dark() = shot("transactions_landscape", true, foodContent())
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class TransactionsFoldableScreenshotTest : TransactionsScreenshots() {
    @Test fun content_light() = shot("transactions_foldable", false, foodContent())
    @Test fun content_dark() = shot("transactions_foldable", true, foodContent())
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TransactionsTabletScreenshotTest : TransactionsScreenshots() {
    @Test fun content_light() = shot("transactions_tablet", false, foodContent())
    @Test fun content_dark() = shot("transactions_tablet", true, foodContent())
}
