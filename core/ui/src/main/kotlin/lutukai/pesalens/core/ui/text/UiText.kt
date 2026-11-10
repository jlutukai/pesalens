package lutukai.pesalens.core.ui.text

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: ImmutableList<Any> = persistentListOf()) : UiText

    data class Plural(
        @PluralsRes val id: Int,
        val count: Int,
        val args: ImmutableList<Any> = persistentListOf(),
    ) : UiText

    data class Raw(val value: String) : UiText
}

/** Resolves to a string; [UiText] arguments are resolved recursively. */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> stringResource(id, *args.resolve())
    is UiText.Plural -> pluralStringResource(id, count, *args.resolve())
}

@Composable
private fun List<Any>.resolve(): Array<Any> = Array(size) { i ->
    val arg = this[i]
    if (arg is UiText) arg.asString() else arg
}
