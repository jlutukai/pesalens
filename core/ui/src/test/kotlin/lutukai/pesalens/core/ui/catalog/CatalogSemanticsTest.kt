package lutukai.pesalens.core.ui.catalog

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.catalog.model.NoticeActionUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.catalog.model.accessibilitySummary
import lutukai.pesalens.core.ui.preview.PreviewFixtures
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h2300dp-xhdpi")
class CatalogSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val spending = PreviewFixtures.novemberSpending
    private val summary =
        "Spending for November 2026 totals KSh 65,000. Rent KSh 25,000 (38%), Food and groceries KSh 15,000 (23%), " +
            "Transport KSh 8,000 (12%), Utilities KSh 5,000 (8%), Shopping KSh 7,000 (11%), Other KSh 5,000 (8%)."
    private val foodDescription = "Food and groceries, KSh 15,000, 23 percent. Open transactions"

    @Test
    fun spendingChart_groupDescription_matchesPrototype() {
        assertEquals(summary, spending.accessibilitySummary())
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(spending, onCategoryClick = {}) } }
        composeRule.onNodeWithContentDescription(summary).assertIsDisplayed()
    }

    @Test
    fun spendingChart_summaryWithPatternNames_namesEachPattern() {
        val names = listOf("solid", "diagonal stripes", "dots", "horizontal lines", "reverse stripes", "vertical lines")
        val text = spending.accessibilitySummary(names)
        assertEquals(true, text.contains("Rent KSh 25,000 (38%, solid)"))
        assertEquals(true, text.contains("Other KSh 5,000 (8%, vertical lines)"))
    }

    @Test
    fun categoryRow_click_reportsId() {
        var clicked: String? = null
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(spending, onCategoryClick = { clicked = it }) } }
        composeRule.onNode(hasClickAction() and hasText("Food and groceries", substring = true)).performClick()
        assertEquals("food", clicked)
    }

    @Test
    fun categoryRow_description() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) { CategoryRow(spending.categories[1], patternName = "diagonal stripes", onClick = {}) }
        }
        composeRule.onNodeWithContentDescription(foodDescription).assertIsDisplayed()
        composeRule.onNodeWithText("23% · diagonal stripes", substring = true).assertIsDisplayed()
    }

    @Test
    fun categoryRow_andBarSegment_shareDescription() {
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(spending, onCategoryClick = {}) } }
        composeRule.onAllNodesWithContentDescription(foodDescription).assertCountEquals(2)
    }

    @Test
    fun readChartAsText_expands() {
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(spending, onCategoryClick = {}) } }
        composeRule.onNodeWithText(summary).assertDoesNotExist()
        composeRule.onNodeWithText("Read chart as text").performClick()
        composeRule.onNodeWithText(summary).assertIsDisplayed()
    }

    @Test
    fun spendingChart_zeroAmountCategory_rendersWithoutCrash() {
        val withZero = spending.copy(
            categories = (spending.categories + CategorySpendUi("empty", "Savings", Money.Zero, 0, 5)).toImmutableList(),
        )
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(withZero, onCategoryClick = {}) } }
        composeRule.onNodeWithText("Savings", substring = true).assertIsDisplayed()
    }

    @Test
    fun spendingChart_totalCaption_pluralisesCategories() {
        val one = spending.copy(categories = spending.categories.take(1).toImmutableList())
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(one, onCategoryClick = {}) } }
        composeRule.onNodeWithText("Total across 1 category · ${spending.rangeLabel}").assertIsDisplayed()
    }

    @Test
    fun spendingChart_totalCaption_manyCategories() {
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(spending, onCategoryClick = {}) } }
        composeRule.onNodeWithText("Total across 6 categories · ${spending.rangeLabel}").assertIsDisplayed()
    }

    @Test
    fun spendingChart_zeroTotal_rendersWithoutCrash() {
        val empty = spending.copy(
            total = Money.Zero,
            categories = spending.categories.map { it.copy(amount = Money.Zero, sharePercent = 0) }.toImmutableList(),
        )
        composeRule.setContent { PesaLensTheme(darkTheme = false) { SpendingChartCard(empty, onCategoryClick = {}) } }
        composeRule.onNodeWithText("Rent").assertIsDisplayed()
    }

    @Test
    fun notice_actions_invokeKeys() {
        val keys = mutableListOf<String>()
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                StatusNotice(
                    kind = NoticeKind.ModelUnavailable,
                    title = "The on-device model isn't ready",
                    body = "Body",
                    actions = persistentListOf(NoticeActionUi("Set up model", "setup"), NoticeActionUi("Use cloud AI", "cloud")),
                    onAction = { keys += it },
                )
            }
        }
        composeRule.onNodeWithText("Set up model").performClick()
        composeRule.onNodeWithText("Use cloud AI").performClick()
        assertEquals(listOf("setup", "cloud"), keys)
    }

    @Test
    fun notice_isPoliteLiveRegion() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                StatusNotice(NoticeKind.Unsupported, "Title", "Body", persistentListOf(), onAction = {})
            }
        }
        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)).assertIsDisplayed()
    }

    @Test
    fun thinkingBubble_isStatus() {
        // The loading animation never settles, so drive the clock by hand.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent { PesaLensTheme(darkTheme = false) { ThinkingBubble("Looking at your spending…") } }
        composeRule.onNodeWithText("Looking at your spending…")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
    }

    @Test
    fun suggestionRow_reportsIndex() {
        var picked = -1
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                SuggestionRow(persistentListOf("One", "Two", "Three"), onClick = { picked = it })
            }
        }
        composeRule.onNodeWithText("Two").performClick()
        assertEquals(1, picked)
    }

    @Test
    fun modePill_andCaption_followMode() {
        composeRule.setContent {
            PesaLensTheme(darkTheme = false) {
                ModePill(AssistantMode.OnDevice)
                MessageCaption(AssistantMode.Cloud)
            }
        }
        composeRule.onNodeWithText("On-device AI · offline").assertIsDisplayed()
        composeRule.onNodeWithText("Cloud AI · figures computed by PesaLens").assertIsDisplayed()
    }

    @Test
    fun transactionRow_showsNameDateAndAmount() {
        val item = PreviewFixtures.foodTransactions[2]
        composeRule.setContent { PesaLensTheme(darkTheme = false) { TransactionRow(item) } }
        composeRule.onNodeWithText("Lunches").assertIsDisplayed()
        composeRule.onNodeWithText("3–9 Nov · 6 entries").assertIsDisplayed()
        composeRule.onNodeWithText("KSh 3,000").assertIsDisplayed()
    }
}
