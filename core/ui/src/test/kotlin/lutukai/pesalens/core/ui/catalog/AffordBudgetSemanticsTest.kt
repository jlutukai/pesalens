package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.catalog.model.monthBlocks
import lutukai.pesalens.core.ui.preview.PreviewFixtures
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h2300dp-xhdpi")
class AffordBudgetSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val noopAfford: (StepDirection) -> Unit = {}

    private fun budgetCard(
        ui: lutukai.pesalens.core.ui.catalog.model.BudgetUi,
        onLineStep: (Int, StepDirection) -> Unit = { _, _ -> },
        onLineChange: (Int, String) -> Unit = { _, _ -> },
    ) = composeRule.setContent {
        PesaLensTheme(darkTheme = false) {
            BudgetEditorCard(ui, onIncomeChange = {}, onLineChange = onLineChange, onLineStep = onLineStep, onCancel = {}, onReview = {})
        }
    }

    @Test
    fun monthBlocks_capsAt18_withOverflow() {
        val blocks = monthBlocks(YearMonth(2026, 11), 20)
        assertEquals(19, blocks.size)
        assertEquals("Dec", blocks.first())
        assertEquals("Jan '27", blocks[1])
        assertEquals("+2", blocks.last())
    }

    @Test
    fun monthBlocks_emptyWhenNoMonths_andNoOverflowAt18() {
        assertEquals(0, monthBlocks(YearMonth(2026, 11), 0).size)
        assertEquals(0, monthBlocks(YearMonth(2026, 11), -3).size)
        assertEquals(18, monthBlocks(YearMonth(2026, 11), 18).size)
    }

    @Test
    fun affordability_monthlyDown_disabledAtZero() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(monthlyShillings = 0), onMonthly = noopAfford, onAvailable = noopAfford)
            }
        }
        composeRule.onNodeWithContentDescription("Decrease monthly savings by KSh 5,000").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Increase monthly savings by KSh 5,000").assertIsEnabled()
        composeRule.onNodeWithContentDescription("Decrease amount set aside by KSh 10,000").assertIsEnabled()
    }

    @Test
    fun affordability_steppers_reportDirection() {
        val monthly = mutableListOf<StepDirection>()
        val available = mutableListOf<StepDirection>()
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(), onMonthly = { monthly += it }, onAvailable = { available += it })
            }
        }
        composeRule.onNodeWithContentDescription("Increase monthly savings by KSh 5,000").performClick()
        composeRule.onNodeWithContentDescription("Decrease monthly savings by KSh 5,000").performClick()
        composeRule.onNodeWithContentDescription("Decrease amount set aside by KSh 10,000").performClick()
        composeRule.onNodeWithContentDescription("Increase amount set aside by KSh 10,000").performClick()
        assertEquals(listOf(StepDirection.Up, StepDirection.Down), monthly)
        assertEquals(listOf(StepDirection.Down, StepDirection.Up), available)
    }

    @Test
    fun affordability_onTrack_showsHeadline() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(), onMonthly = noopAfford, onAvailable = noopAfford)
            }
        }
        composeRule.onNodeWithText("About 6 months").assertIsDisplayed()
        composeRule.onNodeWithText("to save the remaining KSh 60,000 at KSh 10,000 a month, around May 2027", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("33% set aside").assertIsDisplayed()
        composeRule.onNodeWithText("Goal KSh 90,000").assertIsDisplayed()
        composeRule.onNodeWithText("6 mo · May 2027").assertIsDisplayed()
        // The month chips are decorative: the headline already gives the date.
        composeRule.onNodeWithText("Dec").assertDoesNotExist()
    }

    @Test
    fun affordability_oneMonth_isSingular() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(90_000, 80_000, 10_000), onMonthly = noopAfford, onAvailable = noopAfford)
            }
        }
        composeRule.onNodeWithText("About 1 month").assertIsDisplayed()
    }

    @Test
    fun affordability_headlineAndWhatIfValue_arePoliteLiveRegions() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(), onMonthly = noopAfford, onAvailable = noopAfford)
            }
        }
        val polite = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
        composeRule.onNodeWithText("About 6 months").assert(polite)
        // The monthly what-if value; "KSh 10,000" is also a value row, so match the one that is a live region.
        composeRule.onNode(polite and hasText("KSh 10,000")).assertIsDisplayed()
    }

    @Test
    fun affordability_covered_showsCoveredCallout() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(90_000, 100_000, 10_000), onMonthly = noopAfford, onAvailable = noopAfford)
            }
        }
        composeRule.onNodeWithText("The amount set aside covers this purchase. Other costs and commitments aren't included here.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Already covered").assertIsDisplayed()
        composeRule.onNodeWithText("About", substring = true).assertDoesNotExist()
    }

    @Test
    fun affordability_noEndDate_showsWarn() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityCard(PreviewFixtures.affordability(monthlyShillings = 0), onMonthly = noopAfford, onAvailable = noopAfford)
            }
        }
        composeRule.onNodeWithText("There is no estimated completion date at this savings rate.").assertIsDisplayed()
        composeRule.onNodeWithText("No end date").assertIsDisplayed()
    }

    @Test
    fun detailsCard_missingFields_showHints() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityDetailsCard(
                    PreviewFixtures.affordabilityForm(tried = true),
                    onAvailableChange = {}, onMonthlyChange = {}, onUseSample = {}, onSubmit = {},
                )
            }
        }
        composeRule.onNodeWithText("Needed to work out the remaining amount").assertIsDisplayed()
        composeRule.onNodeWithText("Needed to estimate a timeline. Enter 0 if none.").assertIsDisplayed()
        composeRule.onNodeWithText("Show estimate").assertIsDisplayed()
    }

    @Test
    fun detailsCard_actionsReported_andHiddenWhenSubmitted() {
        var sample = 0
        var submit = 0
        val typed = mutableListOf<String>()
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityDetailsCard(
                    PreviewFixtures.affordabilityForm(),
                    onAvailableChange = { typed += it }, onMonthlyChange = {}, onUseSample = { sample++ }, onSubmit = { submit++ },
                )
            }
        }
        composeRule.onNodeWithText("Use sample values").performClick()
        composeRule.onNodeWithText("Show estimate").performClick()
        assertEquals(1, sample)
        assertEquals(1, submit)
    }

    @Test
    fun detailsCard_submitted_hidesActionsAndDisablesFields() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                AffordabilityDetailsCard(
                    PreviewFixtures.affordabilityForm("30000", "10000", submitted = true),
                    onAvailableChange = {}, onMonthlyChange = {}, onUseSample = {}, onSubmit = {},
                )
            }
        }
        composeRule.onNodeWithText("Show estimate").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Amount already set aside").assertIsNotEnabled()
        composeRule.onNodeWithText("30,000").assertIsDisplayed()
    }

    @Test
    fun budget_over_showsAlert() {
        budgetCard(PreviewFixtures.overBudget())
        composeRule.onNodeWithText("Category limits exceed income by KSh 10,000. Lower a limit, or review and save it on purpose.")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))
        composeRule.onNodeWithText("Over budget by").assertIsDisplayed()
    }

    @Test
    fun budget_unallocated_hasNoAlert() {
        budgetCard(PreviewFixtures.budget())
        composeRule.onNodeWithText("Unallocated").assertIsDisplayed()
        composeRule.onNodeWithText("KSh 75,000 of KSh 100,000 allocated").assertIsDisplayed()
        composeRule.onNodeWithText("Category limits exceed", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Review budget").assertIsDisplayed()
    }

    @Test
    fun budget_draft_stepAndEditAreReportedWithIndex() {
        val steps = mutableListOf<Pair<Int, StepDirection>>()
        val edits = mutableListOf<Pair<Int, String>>()
        budgetCard(PreviewFixtures.budget(), onLineStep = { i, d -> steps += i to d }, onLineChange = { i, v -> edits += i to v })
        composeRule.onNodeWithContentDescription("Decrease Transport by KSh 500").performClick()
        composeRule.onNodeWithContentDescription("Increase Food and groceries by KSh 500").performClick()
        assertEquals(listOf(2 to StepDirection.Down, 1 to StepDirection.Up), steps)
        composeRule.onNodeWithContentDescription("Utilities limit in shillings").assertIsEnabled()
        composeRule.onNodeWithContentDescription("Monthly income in shillings").assertIsEnabled()
    }

    @Test
    fun budget_locked_disablesInputs() {
        budgetCard(PreviewFixtures.budget(status = BudgetStatus.Saved))
        composeRule.onNodeWithText("Saved · November 2026").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Decrease Rent by KSh 500").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Increase Rent by KSh 500").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Rent limit in shillings").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Monthly income in shillings").assertIsNotEnabled()
        composeRule.onNodeWithText("Review budget").assertDoesNotExist()
        composeRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun budget_discarded_showsStatus() {
        budgetCard(PreviewFixtures.budget(status = BudgetStatus.Discarded))
        composeRule.onNodeWithText("Discarded · not saved").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Rent limit in shillings").assertIsNotEnabled()
    }

    @Test
    fun budgetEditorRow_fontScale2_keepsControlsVisible() {
        val line = PreviewFixtures.budget().lines[1]
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PesaLensTheme(darkTheme = false) {
                    BudgetEditorRow(line, enabled = true, onChange = {}, onStep = {}, modifier = Modifier.width(412.dp))
                }
            }
        }
        composeRule.onNodeWithContentDescription("Decrease Food and groceries by KSh 500").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithContentDescription("Increase Food and groceries by KSh 500").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithContentDescription("Food and groceries limit in shillings").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        // The name sits on its own full-width line: a mid-word break would make it wrap, so it must be one line tall.
        // The row sets a 17.5sp line height, 35dp at 2x; two lines would be 70dp.
        val label = composeRule.onNodeWithText("Food and groceries").assertIsDisplayed().getBoundsInRoot()
        val oneLine = with(Density(density = 1f, fontScale = 2f)) { 17.5.sp.toDp() }
        assertTrue((label.bottom - label.top) < oneLine * 1.5f, "label wrapped: height ${label.bottom - label.top}, one line is $oneLine")
    }

    @Test
    fun savedCard_showsTitleAndSummary() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                BudgetSavedCard(PreviewFixtures.month, "KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated.")
            }
        }
        composeRule.onNodeWithText("Budget saved for November 2026").assertIsDisplayed()
        composeRule.onNodeWithText(
            "KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated. Stored on this device only.",
        ).assertIsDisplayed()
    }

    @Test
    fun reviewDialog_listsLinesAndTotals() {
        composeRule.setContent { PesaLensTheme(darkTheme = false) { BudgetReviewDialog(PreviewFixtures.budget(), onConfirm = {}, onDismiss = {}) } }
        composeRule.onNodeWithText("Save this budget?").assertIsDisplayed()
        composeRule.onNodeWithText("November 2026 · stored on this device").assertIsDisplayed()
        composeRule.onNodeWithText("Monthly income").assertIsDisplayed()
        composeRule.onNodeWithText("KSh 100,000").assertIsDisplayed()
        PreviewFixtures.budget().lines.forEach { composeRule.onNodeWithText(it.label).assertIsDisplayed() }
        composeRule.onNodeWithText("Total budgeted").assertIsDisplayed()
        composeRule.onNodeWithText("KSh 75,000").assertIsDisplayed()
        composeRule.onNodeWithText("Unallocated").assertIsDisplayed()
        // Rent's limit and the unallocated difference are both 25,000.
        composeRule.onAllNodesWithText("KSh 25,000").assertCountEquals(2)
        composeRule.onNodeWithText("This budget is over your income. You can still save it on purpose.").assertDoesNotExist()
    }

    @Test
    fun reviewDialog_over_warns_andButtonsReport() {
        var confirmed = 0
        var dismissed = 0
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                BudgetReviewDialog(PreviewFixtures.overBudget(), onConfirm = { confirmed++ }, onDismiss = { dismissed++ })
            }
        }
        composeRule.onNodeWithText("This budget is over your income. You can still save it on purpose.").assertIsDisplayed()
        composeRule.onNodeWithText("Over budget by").assertIsDisplayed()
        composeRule.onNodeWithText("Confirm and save").performClick()
        composeRule.onNodeWithText("Go back").performClick()
        assertEquals(1, confirmed)
        assertEquals(1, dismissed)
    }

    @Test
    fun modelStatusHeader_titlesFollowStatus() {
        val titles = mapOf(
            ModelStatus.NotDownloaded to "Not downloaded",
            ModelStatus.ReadyToDownload to "Ready to download",
            ModelStatus.Downloading(40) to "Downloading",
            ModelStatus.Verifying to "Verifying file",
            ModelStatus.Ready to "Ready on this device",
            ModelStatus.Failed(40) to "Download failed",
            ModelStatus.Unsupported to "Not supported on this device",
        )
        var status by mutableStateOf<ModelStatus>(ModelStatus.NotDownloaded)
        composeRule.setContent { PesaLensTheme(darkTheme = false) { ModelStatusHeader(status) } }
        titles.forEach { (next, title) ->
            composeRule.runOnIdle { status = next }
            composeRule.onNodeWithText(title).assertIsDisplayed()
            composeRule.onNodeWithText("MODEL STATUS").assertIsDisplayed()
        }
    }

    @Test
    fun modelFileFacts_marksSizesIllustrative() {
        composeRule.setContent { PesaLensTheme(darkTheme = false) { ModelFileFacts() } }
        composeRule.onNodeWithText("Compact assistant model").assertIsDisplayed()
        composeRule.onNodeWithText("≈ 600 MB").assertIsDisplayed()
        composeRule.onNodeWithText("≈ 1.2 GB").assertIsDisplayed()
        composeRule.onAllNodesWithText("illustrative").assertCountEquals(2)
        composeRule.onNodeWithText("Placeholder figures for this prototype, not measured values. Wi-Fi recommended.").assertIsDisplayed()
    }
}
