package app.fine.ui.add

sealed interface AddExpenseEvent {
    data class ExpenseSaved(val description: String, val amountMinor: Long) : AddExpenseEvent
    data class ShowError(val message: String) : AddExpenseEvent
}
