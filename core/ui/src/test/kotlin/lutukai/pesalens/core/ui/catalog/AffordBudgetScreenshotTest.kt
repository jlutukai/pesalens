package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.preview.PreviewFixtures
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

/** Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`. 412dp wide, as the prototype phone. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h1800dp-xhdpi")
class AffordBudgetScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun details_light() = capture("details", Variant.Light) { DetailsSheet() }
    @Test fun details_dark() = capture("details", Variant.Dark) { DetailsSheet() }
    @Test fun details_fontScale2() = capture("details", Variant.Large) { DetailsSheet() }

    @Test fun affordability_light() = capture("affordability", Variant.Light) { AffordabilitySheet() }
    @Test fun affordability_dark() = capture("affordability", Variant.Dark) { AffordabilitySheet() }
    @Test fun affordability_fontScale2() = capture("affordability", Variant.Large) { AffordabilitySheet() }

    @Test fun budget_light() = capture("budget", Variant.Light) { BudgetSheet() }
    @Test fun budget_dark() = capture("budget", Variant.Dark) { BudgetSheet() }
    @Test fun budget_fontScale2() = capture("budget", Variant.Large) { BudgetSheet() }

    private enum class Variant(val suffix: String, val dark: Boolean, val fontScale: Float) {
        Light("light", false, 1f),
        Dark("dark", true, 1f),
        Large("fontScale2", false, 2f),
    }

    private fun capture(name: String, variant: Variant, content: @Composable () -> Unit) {
        // Step the clock by hand, as the other catalog shots do: it makes sure the first frame is drawn before capture.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = variant.fontScale)) {
                PesaLensTheme(variant.dark) {
                    Column(Modifier.background(PesaLensTheme.colors.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        content()
                    }
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(200)
        composeRule.onRoot().captureRoboImage("src/test/screenshots/afford_${name}_${variant.suffix}.png")
    }
}

@Composable
private fun DetailsSheet() {
    AffordabilityDetailsCard(PreviewFixtures.affordabilityForm(), {}, {}, {}, {})
    AffordabilityDetailsCard(PreviewFixtures.affordabilityForm(tried = true), {}, {}, {}, {})
    AffordabilityDetailsCard(PreviewFixtures.affordabilityForm("30000", "10000", submitted = true), {}, {}, {}, {})
}

@Composable
private fun AffordabilitySheet() {
    AffordabilityCard(PreviewFixtures.affordability(), {}, {})
    AffordabilityCard(PreviewFixtures.affordability(90_000, 100_000, 10_000), {}, {})
    AffordabilityCard(PreviewFixtures.affordability(monthlyShillings = 0), {}, {})
    // 21 months: 18 blocks plus the "+3" overflow chip.
    AffordabilityCard(PreviewFixtures.affordability(240_000, 30_000, 10_000), {}, {})
}

@Composable
private fun BudgetSheet() {
    listOf(
        PreviewFixtures.budget(),
        PreviewFixtures.overBudget(),
        PreviewFixtures.budget(status = BudgetStatus.Saved),
        PreviewFixtures.overBudget(BudgetStatus.Discarded),
    ).forEach { BudgetEditorCard(it, {}, { _, _ -> }, { _, _ -> }, {}, {}) }
    BudgetSavedCard(PreviewFixtures.month, "KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated.")
}

@Composable
private fun ModelSheet() {
    listOf(
        ModelStatus.NotDownloaded,
        ModelStatus.ReadyToDownload,
        ModelStatus.Downloading(42),
        ModelStatus.Verifying,
        ModelStatus.Ready,
        ModelStatus.Failed(42),
        ModelStatus.Unsupported,
    ).forEach { ModelStatusHeader(it) }
    ModelFileFacts()
}

/** A separate class: a dialog window left over from one test confuses the next test's root capture in the same class. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h900dp-xhdpi")
class AffordBudgetDialogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun reviewDialog_light() = captureDialog("review_dialog", false, PreviewFixtures.budget())
    @Test fun reviewDialog_dark() = captureDialog("review_dialog", true, PreviewFixtures.budget())
    @Test fun reviewDialogOver_light() = captureDialog("review_dialog_over", false, PreviewFixtures.overBudget())


    private fun captureDialog(name: String, dark: Boolean, ui: BudgetUi) {
        composeRule.setContent {
            PesaLensTheme(dark) {
                BudgetReviewDialog(ui, onConfirm = {}, onDismiss = {})
            }
        }
        // The dialog is its own window and root; capture that one.
        composeRule.onAllNodes(isRoot()).onLast().captureRoboImage("src/test/screenshots/afford_${name}_${if (dark) "dark" else "light"}.png")
    }
}

/** Its own class: the shots in one class share a Robolectric sandbox and late captures there can come back blank. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h1000dp-xhdpi")
class ModelStatusScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun model_light() = capture("model_light", false, 1f)
    @Test fun model_dark() = capture("model_dark", true, 1f)
    @Test fun model_fontScale2() = capture("model_fontScale2", false, 2f)

    private fun capture(name: String, dark: Boolean, fontScale: Float) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                PesaLensTheme(dark) {
                    Column(Modifier.background(PesaLensTheme.colors.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ModelSheet()
                    }
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(200)
        composeRule.onRoot().captureRoboImage("src/test/screenshots/afford_$name.png")
    }
}
