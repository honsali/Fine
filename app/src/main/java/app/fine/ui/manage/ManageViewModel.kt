package app.fine.ui.manage

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fine.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ManageEvent>()
    val events = _events.asSharedFlow()

    fun exportTo(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val outcome = repository.exportCsv(outputStream)
                        outcome.getOrThrow()
                    } ?: error("Impossible d'ouvrir le fichier.")
                }
            }
            result
                .onSuccess {
                    _events.emit(ManageEvent.Success("Export CSV réussi."))
                }
                .onFailure { throwable ->
                    _events.emit(
                        ManageEvent.Error(throwable.message ?: "Export impossible.")
                    )
                }
            _uiState.update { it.copy(isExporting = false) }
        }
    }

    fun purgeAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurging = true) }
            runCatching { repository.purgeAll() }
                .onSuccess {
                    _events.emit(ManageEvent.Success("Historique purgé."))
                }
                .onFailure { throwable ->
                    _events.emit(
                        ManageEvent.Error(throwable.message ?: "Purge impossible.")
                    )
                }
            _uiState.update { it.copy(isPurging = false) }
        }
    }
}
