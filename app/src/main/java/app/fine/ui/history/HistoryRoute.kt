package app.fine.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel,
    onMessage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HistoryEvent.Message -> onMessage(event.text)
            }
        }
    }

    HistoryScreen(
        state = uiState,
        onDeleteExpense = viewModel::deleteExpense,
        onUpdateExpense = viewModel::updateExpense
    )
}
