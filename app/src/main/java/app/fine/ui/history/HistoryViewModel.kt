package app.fine.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fine.data.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    repository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> =
        repository.observeMonthlyExpenses()
            .map { HistoryUiState(months = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState()
            )
}
