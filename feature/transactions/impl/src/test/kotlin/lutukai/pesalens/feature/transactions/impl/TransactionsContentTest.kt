package lutukai.pesalens.feature.transactions.impl

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import kotlin.test.assertEquals
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.core.testing.assertFullyDisplayed
import lutukai.pesalens.core.testing.assertTextNotTruncated
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsContent
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsIntent
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsUiState
import lutukai.pesalens.feature.transactions.impl.presentation.toContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h1200dp-420dpi")
class TransactionsContentTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(state: TransactionsUiState, intents: MutableList<TransactionsIntent> = mutableListOf()) {
        compose.setContent { PesaLensTheme(darkTheme = false) { TransactionsContent(state, intents::add) } }
    }

    @Test
    fun content_showsCopyAndSendsBack() {
        val intents = mutableListOf<TransactionsIntent>()
        show(foodContent(), intents)
        compose.onNodeWithText("Food and groceries").assertIsDisplayed()
        compose.onNodeWithText("November 2026 · 1–9 Nov").assertIsDisplayed()
        compose.onNodeWithText("Sample").assertIsDisplayed()
        compose.onNodeWithText("4 sample transactions · 23% of spending").assertIsDisplayed()
        compose.onNodeWithText("3–9 Nov · 6 entries").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to conversation").performClick()
        compose.onNodeWithText("Back to conversation").performScrollTo().performClick()
        assertEquals(listOf<TransactionsIntent>(TransactionsIntent.Back, TransactionsIntent.Back), intents)
    }

    @Test
    fun error_showsCalloutAndRetry() {
        val intents = mutableListOf<TransactionsIntent>()
        show(TransactionsUiState.Error(AppError.NotConnected), intents)
        compose.onNodeWithText("Transactions can't be shown right now.").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(listOf<TransactionsIntent>(TransactionsIntent.Retry), intents)
    }

    @Test
    fun empty_usesNoDataNotice() {
        show(TransactionsUiState.Empty("Food and groceries", november))
        compose.onNodeWithText("No spending data for November 2026").assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TransactionsExpandedContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun content_backButtonHiddenOnExpanded() {
        compose.setContent { PesaLensTheme(darkTheme = false) { TransactionsContent(foodContent(), {}) } }
        compose.onNodeWithText("Food and groceries").assertIsDisplayed()
        // Only the top-bar arrow carries the description; the bottom button (same words) is gone.
        compose.onNodeWithContentDescription("Back to conversation").assertIsDisplayed()
        compose.onNodeWithText("Back to conversation").assertDoesNotExist()
    }
}

internal val november = kotlinx.datetime.YearMonth(2026, 11)

internal fun foodContent(): TransactionsUiState.Content =
    lutukai.pesalens.core.testing.SampleFinance.categoryTransactions("food").toContent()

/** At 200% the drill-down's top bar grows: the title and subtitle are shown in full, not clipped or ellipsized. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = ScreenshotSizes.Phone)
class TransactionsLargeFontContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun content_fontScale2_topBarShowsTitleAndSubtitle() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PesaLensTheme(darkTheme = false) { TransactionsContent(foodContent(), {}) }
            }
        }
        compose.onNodeWithText("Food and groceries").assertFullyDisplayed().assertTextNotTruncated()
        compose.onNodeWithText("November 2026 · 1–9 Nov").assertFullyDisplayed().assertTextNotTruncated()
        compose.onNodeWithContentDescription("Back to conversation").assertFullyDisplayed()
    }
}
