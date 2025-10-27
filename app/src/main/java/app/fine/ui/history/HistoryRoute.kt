package app.fine.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(state = uiState)
}
