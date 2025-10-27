package app.fine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fine.data.ExpenseRepository
import app.fine.ui.add.AddExpenseViewModel
import app.fine.ui.history.HistoryViewModel
import app.fine.ui.manage.ManageViewModel

class FineViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AddExpenseViewModel::class.java -> AddExpenseViewModel(repository)
        HistoryViewModel::class.java -> HistoryViewModel(repository)
        ManageViewModel::class.java -> ManageViewModel(repository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}
