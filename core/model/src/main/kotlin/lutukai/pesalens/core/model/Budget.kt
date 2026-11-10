package lutukai.pesalens.core.model

import kotlinx.datetime.YearMonth

data class BudgetLine(val category: Category, val limit: Money)

data class BudgetDraft(val month: YearMonth, val income: Money, val lines: List<BudgetLine>)
