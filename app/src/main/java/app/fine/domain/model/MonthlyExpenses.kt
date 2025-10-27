package app.fine.domain.model

import java.time.YearMonth

data class MonthlyExpenses(
    val yearMonth: YearMonth,
    val totalMinor: Long,
    val expenses: List<Expense>
)
