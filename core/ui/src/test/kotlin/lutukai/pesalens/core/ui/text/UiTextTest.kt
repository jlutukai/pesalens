package lutukai.pesalens.core.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.ui.R
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class UiTextTest {
    @get:Rule val rule = createComposeRule()

    private fun resolve(text: UiText): String {
        var result = ""
        rule.setContent { result = text.asString() }
        rule.waitForIdle()
        return result
    }

    @Test fun rawReturnsValue() = assertEquals("hello", resolve(UiText.Raw("hello")))

    @Test fun resResolvesString() =
        assertEquals("Sample data", resolve(UiText.Res(R.string.core_ui_sample_data)))

    @Test fun nestedUiTextArgsAreResolved() {
        val text = UiText.Res(
            R.string.core_ui_test_greeting,
            persistentListOf(UiText.Res(R.string.core_ui_sample_data), 7),
        )
        assertEquals("Hi Sample data 7", resolve(text))
    }
}
