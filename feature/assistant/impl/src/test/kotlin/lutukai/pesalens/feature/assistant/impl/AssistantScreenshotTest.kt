package lutukai.pesalens.feature.assistant.impl

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.preview.PreviewFixtures
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantContent
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantDialog
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantUiState
import lutukai.pesalens.feature.assistant.impl.presentation.ChatItem
import lutukai.pesalens.feature.assistant.impl.presentation.DetailPlaceholder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DrawAttempts = 10

/** A phone tall enough to show a whole conversation without scrolling, so a shot compares to the prototype top to bottom. */
private const val TallPhone = "w412dp-h1500dp-420dpi"
private const val TallBudgetPhone = "w412dp-h2000dp-420dpi"

/**
 * Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`.
 *
 * The shots are split over several small classes: Robolectric's native renderer returned undrawn frames for some
 * shots once a single class took more than about six of them.
 */
abstract class AssistantScreenshots {
    @get:Rule
    val compose = createComposeRule()

    private val cloud = AssistantMode.Cloud
    protected val november = PreviewFixtures.month

    protected fun user(id: Long, text: String) = ChatItem.User(id, UiText.Raw(text))

    protected fun state(
        vararg items: ChatItem,
        mode: AssistantMode = cloud,
        dialog: AssistantDialog = AssistantDialog.None,
        modelReady: Boolean = false,
    ) = AssistantUiState(
        currentMonth = november,
        mode = mode,
        modelReady = modelReady,
        items = persistentListOf(*items),
        dialog = dialog,
    )

    protected fun spending() = listOf(
        user(1, "Where did my money go this month?"),
        ChatItem.SpendingResult(2, cloud, PreviewFixtures.novemberSpending, PreviewFixtures.novemberSpending.categories[1]),
    )

    protected fun notice(kind: NoticeKind, mode: AssistantMode = cloud, month: kotlinx.datetime.YearMonth? = null) = state(
        user(1, "Show me my spending for January"),
        ChatItem.Notice(2, mode, kind, month),
        mode = mode,
    )

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(): Boolean {
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

    protected fun capture(name: String, dark: Boolean, fontScale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                PesaLensTheme(darkTheme = dark) { content() }
            }
        }
        for (attempt in 1..DrawAttempts) {
            compose.waitForIdle()
            if (hasDrawn()) break
            // Nothing invalidates a frame the renderer dropped, so ask for another one.
            compose.runOnUiThread { (compose.onRoot().fetchSemanticsNode().root as? View)?.invalidate() }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    protected fun shot(name: String, dark: Boolean, state: AssistantUiState, fontScale: Float = 1f) =
        capture(name + if (dark) "_dark" else "_light", dark, fontScale) { AssistantContent(state, {}) }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AssistantEmptyScreenshotTest : AssistantScreenshots() {
    @Test fun emptyCloud_light() = shot("assistant_empty_cloud", false, state())
    @Test fun emptyCloud_dark() = shot("assistant_empty_cloud", true, state())
    @Test fun emptyOnDevice_light() = shot("assistant_empty_on_device", false, state(mode = AssistantMode.OnDevice))
    @Test fun emptyOnDevice_dark() = shot("assistant_empty_on_device", true, state(mode = AssistantMode.OnDevice))
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class AssistantSpendingScreenshotTest : AssistantScreenshots() {
    @Test fun spending_light() = shot("assistant_spending", false, state(*spending().toTypedArray()))
    @Test fun spending_dark() = shot("assistant_spending", true, state(*spending().toTypedArray()))
    @Test fun spending_fontScale2() = shot("assistant_spending_fontScale2", false, state(*spending().toTypedArray()), fontScale = 2f)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AssistantPriceScreenshotTest : AssistantScreenshots() {
    private val price = state(user(1, "Can I afford a laptop?"), ChatItem.PriceQuestion(2, AssistantMode.Cloud))

    @Test fun price_light() = shot("assistant_price_question", false, price)
    @Test fun price_dark() = shot("assistant_price_question", true, price)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class AssistantDetailsScreenshotTest : AssistantScreenshots() {
    private val details = state(
        user(1, "KSh 90,000"),
        ChatItem.AffordabilityDetails(2, AssistantMode.Cloud, PreviewFixtures.affordabilityForm(tried = true)),
    )

    @Test fun details_light() = shot("assistant_details_missing", false, details)
    @Test fun details_dark() = shot("assistant_details_missing", true, details)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallBudgetPhone)
class AssistantAffordabilityScreenshotTest : AssistantScreenshots() {
    private val result = state(
        user(1, "I have KSh 30,000 set aside and can save KSh 10,000 a month."),
        ChatItem.AffordabilityResult(2, AssistantMode.Cloud, PreviewFixtures.affordability()),
    )

    @Test fun afford_light() = shot("assistant_affordability", false, result)
    @Test fun afford_dark() = shot("assistant_affordability", true, result)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallBudgetPhone)
class AssistantBudgetScreenshotTest : AssistantScreenshots() {
    private val draft = state(user(1, "Help me create a budget"), ChatItem.Budget(2, AssistantMode.Cloud, PreviewFixtures.budget()))
    private val over = state(user(1, "Help me create a budget"), ChatItem.Budget(2, AssistantMode.Cloud, PreviewFixtures.overBudget()))

    @Test fun budget_light() = shot("assistant_budget_draft", false, draft)
    @Test fun budget_dark() = shot("assistant_budget_draft", true, draft)
    @Test fun budgetOver_light() = shot("assistant_budget_over", false, over)
    @Test fun budgetOver_dark() = shot("assistant_budget_over", true, over)
    @Test fun budget_fontScale2() = shot("assistant_budget_draft_fontScale2", false, draft, fontScale = 2f)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallBudgetPhone)
class AssistantBudgetOutcomeScreenshotTest : AssistantScreenshots() {
    private val saved = state(
        user(1, "Help me create a budget"),
        ChatItem.Budget(2, AssistantMode.Cloud, PreviewFixtures.budget(status = BudgetStatus.Saved)),
        ChatItem.BudgetSaved(3, AssistantMode.Cloud, november, UiText.Raw("KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated.")),
    )
    private val discarded = state(
        user(1, "Help me create a budget"),
        ChatItem.Budget(2, AssistantMode.Cloud, PreviewFixtures.budget(status = BudgetStatus.Discarded)),
        ChatItem.AssistantText(3, AssistantMode.Cloud, UiText.Raw("Budget discarded. Nothing was saved."), afterCancel = true),
    )

    @Test fun saved_light() = shot("assistant_budget_saved", false, saved)
    @Test fun saved_dark() = shot("assistant_budget_saved", true, saved)
    @Test fun discarded_light() = shot("assistant_budget_discarded", false, discarded)
    @Test fun discarded_dark() = shot("assistant_budget_discarded", true, discarded)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AssistantNoticeAScreenshotTest : AssistantScreenshots() {
    @Test fun noData_light() = shot("assistant_notice_no_data", false, notice(NoticeKind.NoData, month = kotlinx.datetime.YearMonth(2026, 1)))
    @Test fun noData_dark() = shot("assistant_notice_no_data", true, notice(NoticeKind.NoData, month = kotlinx.datetime.YearMonth(2026, 1)))
    @Test fun unsupported_light() = shot("assistant_notice_unsupported", false, notice(NoticeKind.Unsupported))
    @Test fun unsupported_dark() = shot("assistant_notice_unsupported", true, notice(NoticeKind.Unsupported))
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AssistantNoticeBScreenshotTest : AssistantScreenshots() {
    @Test fun modelUnavailable_light() = shot("assistant_notice_model_unavailable", false, notice(NoticeKind.ModelUnavailable, AssistantMode.OnDevice))
    @Test fun modelUnavailable_dark() = shot("assistant_notice_model_unavailable", true, notice(NoticeKind.ModelUnavailable, AssistantMode.OnDevice))
    @Test fun cloudOffline_light() = shot("assistant_notice_cloud_offline", false, notice(NoticeKind.CloudOffline))
    @Test fun cloudOffline_dark() = shot("assistant_notice_cloud_offline", true, notice(NoticeKind.CloudOffline))
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AssistantNoticeCScreenshotTest : AssistantScreenshots() {
    @Test fun invalidResponse_light() = shot("assistant_notice_invalid_response", false, notice(NoticeKind.InvalidResponse))
    @Test fun invalidResponse_dark() = shot("assistant_notice_invalid_response", true, notice(NoticeKind.InvalidResponse))
    @Test fun saveFailed_light() = shot("assistant_notice_save_failed", false, notice(NoticeKind.SaveFailed))
    @Test fun saveFailed_dark() = shot("assistant_notice_save_failed", true, notice(NoticeKind.SaveFailed))
}

/** A separate class: a dialog window left over from one test confuses the next test's root capture in the same class. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AssistantReviewDialogScreenshotTest : AssistantScreenshots() {
    private val review = state(
        user(1, "Help me create a budget"),
        ChatItem.Budget(2, AssistantMode.Cloud, PreviewFixtures.budget()),
        dialog = AssistantDialog.ReviewBudget(2),
    )

    @Test fun reviewDialog_light() = dialog(false)
    @Test fun reviewDialog_dark() = dialog(true)

    private fun dialog(dark: Boolean) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { AssistantContent(review, {}) } }
        compose.waitForIdle()
        // The dialog is its own window and root; capture that one.
        compose.onAllNodes(isRoot()).onLast().captureRoboImage("src/test/screenshots/assistant_review_dialog_${if (dark) "dark" else "light"}.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class AssistantLandscapeScreenshotTest : AssistantScreenshots() {
    private val chat = state(*spending().toTypedArray())

    @Test fun empty_light() = shot("assistant_empty_landscape", false, state())
    @Test fun empty_dark() = shot("assistant_empty_landscape", true, state())
    @Test fun spending_light() = shot("assistant_spending_landscape", false, chat)
    @Test fun spending_dark() = shot("assistant_spending_landscape", true, chat)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class AssistantFoldableScreenshotTest : AssistantScreenshots() {
    private val chat = state(*spending().toTypedArray())

    @Test fun empty_light() = shot("assistant_empty_foldable", false, state())
    @Test fun empty_dark() = shot("assistant_empty_foldable", true, state())
    @Test fun spending_light() = shot("assistant_spending_foldable", false, chat)
    @Test fun spending_dark() = shot("assistant_spending_foldable", true, chat)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class AssistantTabletScreenshotTest : AssistantScreenshots() {
    private val chat = state(*spending().toTypedArray())

    @Test fun empty_light() = shot("assistant_empty_tablet", false, state())
    @Test fun empty_dark() = shot("assistant_empty_tablet", true, state())
    @Test fun spending_light() = shot("assistant_spending_tablet", false, chat)
    @Test fun spending_dark() = shot("assistant_spending_tablet", true, chat)
    @Test fun detailPlaceholder_light() = capture("assistant_detail_placeholder_light", false) { DetailPlaceholder() }
}
