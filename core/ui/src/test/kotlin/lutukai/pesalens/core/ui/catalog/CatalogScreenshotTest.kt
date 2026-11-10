package lutukai.pesalens.core.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.R
import lutukai.pesalens.core.ui.catalog.model.NoticeActionUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.preview.PreviewFixtures
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

/** Writes PNGs under `build/outputs/roborazzi/` when run via `recordRoborazziDebug`. 412dp wide, as the prototype phone. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h1800dp-xhdpi")
class CatalogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun bubbles_light() = capture("bubbles", Variant.Light) { Bubbles() }
    @Test fun bubbles_dark() = capture("bubbles", Variant.Dark) { Bubbles() }
    @Test fun bubbles_fontScale2() = capture("bubbles", Variant.Large) { Bubbles() }

    @Test fun spendingCard_light() = capture("spending_card", Variant.Light) { SpendingSheet() }
    @Test fun spendingCard_dark() = capture("spending_card", Variant.Dark) { SpendingSheet() }
    @Test fun spendingCard_fontScale2() = capture("spending_card", Variant.Large) { SpendingSheet() }

    @Test fun transactionRows_light() = capture("transaction_rows", Variant.Light) { TransactionSheet() }
    @Test fun transactionRows_dark() = capture("transaction_rows", Variant.Dark) { TransactionSheet() }
    @Test fun transactionRows_fontScale2() = capture("transaction_rows", Variant.Large) { TransactionSheet() }

    @Test fun notice_noData() = capture("notice_no_data", Variant.Light) { Notice(NoticeKind.NoData) }
    @Test fun notice_unsupported() = capture("notice_unsupported", Variant.Light) { Notice(NoticeKind.Unsupported) }
    @Test fun notice_modelUnavailable() = capture("notice_model_unavailable", Variant.Light) { Notice(NoticeKind.ModelUnavailable) }
    @Test fun notice_cloudOffline() = capture("notice_cloud_offline", Variant.Light) { Notice(NoticeKind.CloudOffline) }
    @Test fun notice_invalidResponse() = capture("notice_invalid_response", Variant.Light) { Notice(NoticeKind.InvalidResponse) }
    @Test fun notice_saveFailed() = capture("notice_save_failed", Variant.Light) { Notice(NoticeKind.SaveFailed) }
    @Test fun notices_dark() = capture("notices", Variant.Dark) { AllNotices() }
    @Test fun notices_fontScale2() = capture("notices", Variant.Large) { AllNotices() }

    private enum class Variant(val suffix: String, val dark: Boolean, val fontScale: Float) {
        Light("light", false, 1f),
        Dark("dark", true, 1f),
        Large("fontScale2", false, 2f),
    }

    private fun capture(name: String, variant: Variant, content: @Composable () -> Unit) {
        RuntimeEnvironment.setFontScale(variant.fontScale)
        // The loading indicator animates forever; step the clock manually so the rule can settle.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PesaLensTheme(variant.dark) {
                Column(Modifier.background(PesaLensTheme.colors.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    content()
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(200)
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/catalog_${name}_${variant.suffix}.png")
    }
}

@Composable
private fun Bubbles() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        ModePill(AssistantMode.Cloud)
        ModePill(AssistantMode.OnDevice)
    }
    ModeBanner(AssistantMode.Cloud)
    ModeBanner(AssistantMode.OnDevice)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistantBubble(
            "Hi! I can show where your money went, estimate how long a purchase will take to save for, or draft a budget. " +
                "What would you like to look at?",
        )
        SuggestionRow(
            persistentListOf(
                stringResource(R.string.core_ui_suggest_where_money_went),
                stringResource(R.string.core_ui_suggest_afford_laptop),
                stringResource(R.string.core_ui_suggest_create_budget),
            ),
            onClick = {},
        )
    }
    UserBubble("Where did my money go this month?")
    ThinkingBubble("Looking at your spending…")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistantBubble("Here is where your money went in November.")
        MessageCaption(AssistantMode.Cloud)
        MessageCaption(AssistantMode.OnDevice)
    }
}

@Composable
private fun SpendingSheet() {
    SpendingChartCard(PreviewFixtures.novemberSpending, onCategoryClick = {})
}

@Composable
private fun TransactionSheet() {
    Column {
        PreviewFixtures.foodTransactions.forEach { TransactionRow(it) }
    }
}

@Composable
private fun Notice(kind: NoticeKind) {
    val month = "March 2026"
    val (title, body, actions) = when (kind) {
        NoticeKind.NoData -> Triple(
            stringResource(R.string.core_ui_notice_no_data_title, month),
            stringResource(R.string.core_ui_notice_no_data_body),
            listOf(stringResource(R.string.core_ui_notice_no_data_action, "November 2026")),
        )
        NoticeKind.Unsupported -> Triple(
            stringResource(R.string.core_ui_notice_unsupported_title),
            stringResource(R.string.core_ui_notice_unsupported_body),
            emptyList(),
        )
        NoticeKind.ModelUnavailable -> Triple(
            stringResource(R.string.core_ui_notice_model_unavailable_title),
            stringResource(R.string.core_ui_notice_model_unavailable_body),
            listOf(stringResource(R.string.core_ui_notice_model_unavailable_action_setup), stringResource(R.string.core_ui_notice_use_cloud_ai)),
        )
        NoticeKind.CloudOffline -> Triple(
            stringResource(R.string.core_ui_notice_offline_title),
            stringResource(R.string.core_ui_notice_offline_body),
            listOf(stringResource(R.string.core_ui_notice_try_again), stringResource(R.string.core_ui_notice_set_up_on_device_ai)),
        )
        NoticeKind.InvalidResponse -> Triple(
            stringResource(R.string.core_ui_notice_invalid_title),
            stringResource(R.string.core_ui_notice_invalid_body),
            listOf(stringResource(R.string.core_ui_notice_try_again), stringResource(R.string.core_ui_notice_use_on_device_ai)),
        )
        NoticeKind.SaveFailed -> Triple(
            stringResource(R.string.core_ui_notice_save_failed_title),
            stringResource(R.string.core_ui_notice_save_failed_body),
            listOf(stringResource(R.string.core_ui_notice_try_again)),
        )
    }
    StatusNotice(
        kind = kind,
        title = title,
        body = body,
        actions = actions.mapIndexed { i, label -> NoticeActionUi(label, "action$i") }.let { persistentListOf(*it.toTypedArray()) },
        onAction = {},
    )
}

@Composable
private fun AllNotices() {
    NoticeKind.entries.forEach { Notice(it) }
}
