package app.fine.ui.history

import app.fine.domain.model.MonthlyExpenses

data class HistoryUiState(
    val months: List<MonthlyExpenses> = emptyList()
)
