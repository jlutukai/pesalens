package lutukai.pesalens.feature.assistant.impl

import kotlin.test.assertEquals
import lutukai.pesalens.feature.assistant.impl.presentation.Prompt
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** The templates behind the ViewModel's [lutukai.pesalens.core.ui.text.UiText], resolved with the args it passes. */
@RunWith(RobolectricTestRunner::class)
class AssistantCopyTest {
    private fun string(id: Int, vararg args: Any): String = RuntimeEnvironment.getApplication().getString(id, *args)

    @Test
    fun budgetSaved_unallocated_matchesPrototype() = assertEquals(
        "KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated.",
        string(R.string.assistant_budget_saved_unallocated, "KSh 75,000", 6, "KSh 25,000"),
    )

    @Test
    fun budgetSaved_overIncome_matchesPrototype() = assertEquals(
        "KSh 75,000 budgeted across 6 categories · KSh 5,000 over income.",
        string(R.string.assistant_budget_saved_over_income, "KSh 75,000", 6, "KSh 5,000"),
    )

    @Test
    fun affordabilitySummary_matchesPrototype() = assertEquals(
        "I have KSh 30,000 set aside and can save KSh 10,000 a month.",
        string(R.string.assistant_afford_details_summary, "KSh 30,000", "KSh 10,000"),
    )

    @Test
    fun openingCategory_matchesPrototype() = assertEquals(
        "Opening Food and groceries for November 2026.",
        string(R.string.assistant_opening_category, "Food and groceries", "November 2026"),
    )

    @Test
    fun promptLabels_showTheTextTheyAsk() = Prompt.entries.forEach { assertEquals(it.query, string(it.label), "$it") }
}
