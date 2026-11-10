package lutukai.pesalens.core.ui.catalog.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.ui.format.formatShort

/** [available] and [monthly] are digits only ("30000"), as the amount fields emit them. */
@Immutable
data class AffordabilityFormUi(
    val price: Money,
    val available: String,
    val monthly: String,
    val availableMissing: Boolean,
    val monthlyMissing: Boolean,
    val submitted: Boolean,
)

@Immutable
data class AffordabilityUi(
    val price: Money,
    val available: Money,
    val monthly: Money,
    val outcome: AffordabilityOutcome,
    val percentSetAside: Int,
    /** From [monthBlocks]: at most 18 months plus an optional "+N" overflow chip. */
    val monthBlocks: ImmutableList<String>,
)

enum class StepDirection { Down, Up }

private const val MaxMonthBlocks = 18

/** Short month labels for start+1 .. start+min(months, 18), then "+N" for the months beyond 18. Empty when months <= 0. */
fun monthBlocks(start: YearMonth, months: Int): ImmutableList<String> {
    if (months <= 0) return persistentListOf()
    val shown = minOf(months, MaxMonthBlocks)
    val labels = (1..shown).map { start.plus(it, DateTimeUnit.MONTH).formatShort() }
    return (if (months > MaxMonthBlocks) labels + "+${months - MaxMonthBlocks}" else labels).toImmutableList()
}
