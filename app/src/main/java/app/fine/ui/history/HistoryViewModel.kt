package app.fine.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fine.data.ExpenseRepository
import app.fine.parser.AmountParser
import app.fine.parser.DateParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<HistoryEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<HistoryUiState> =
        combine(
            repository.observeMonthlyExpenses(),
            repository.observeCategories()
        ) { months, categories ->
            HistoryUiState(months = months, categories = categories)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
                .onSuccess {
                    _events.emit(HistoryEvent.Message("Depense supprimee."))
                }
                .onFailure { throwable ->
                    _events.emit(
                        HistoryEvent.Message(throwable.message ?: "Suppression impossible.")
                    )
                }
        }
    }

    fun updateExpense(
        id: Long,
        description: String,
        dateText: String,
        amountText: String,
        categoryId: Long
    ) {
        viewModelScope.launch {
            val sanitizedDescription = description.trim()
            if (sanitizedDescription.isEmpty()) {
                _events.emit(HistoryEvent.Message("Description vide."))
                return@launch
            }

            val date = DateParser.parse(dateText).getOrElse { error ->
                _events.emit(HistoryEvent.Message(error.message ?: "Date non reconnue."))
                return@launch
            }

            val amountMinor = AmountParser.parse(amountText).getOrElse { error ->
                _events.emit(HistoryEvent.Message(error.message ?: "Montant non reconnu."))
                return@launch
            }

            repository.updateExpense(
                id = id,
                description = sanitizedDescription,
                date = date,
                amountMinor = amountMinor,
                categoryId = categoryId
            ).onSuccess {
                _events.emit(HistoryEvent.Message("Depense mise a jour."))
            }.onFailure { throwable ->
                _events.emit(
                    HistoryEvent.Message(throwable.message ?: "Mise a jour impossible.")
                )
            }
        }
    }
}
