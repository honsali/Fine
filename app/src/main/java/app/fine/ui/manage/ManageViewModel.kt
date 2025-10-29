package app.fine.ui.manage

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fine.data.ExpenseRepository
import app.fine.domain.model.Category
import app.fine.domain.model.CategoryPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    init {
        ensurePresetCategories()
        viewModelScope.launch {
            repository.observeCategories().collectLatest { categories ->
                _uiState.update { state ->
                    state.copy(categories = categories.map { it.toUi() })
                }
            }
        }
    }

    private fun ensurePresetCategories() {
        viewModelScope.launch {
            CategoryPresets.ProtectedCategoryDisplayNames.forEach { name ->
                repository.ensureCategory(name)
            }
        }
    }

    fun onNewCategoryNameChange(value: String) {
        _uiState.update {
            it.copy(newCategoryName = value, categoryError = null)
        }
    }

    fun createCategory() {
        val input = _uiState.value.newCategoryName.trim()
        if (input.isEmpty()) {
            _uiState.update { it.copy(categoryError = "Nom requis.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCategory = true, categoryError = null) }
            repository.addCategory(input)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            newCategoryName = "",
                            isSavingCategory = false
                        )
                    }
                    _events.emit(ManageEvent.Success("Categorie ajoutee."))
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingCategory = false,
                            categoryError = throwable.message ?: "Ajout impossible."
                        )
                    }
                }
        }
    }

    fun startEditCategory(category: CategoryUi) {
        if (category.isDefault) {
            emitError("Impossible de modifier cette categorie.")
            return
        }
        _uiState.update {
            it.copy(
                editCategory = CategoryEditState(
                    category = category,
                    name = category.name
                )
            )
        }
    }

    fun onEditCategoryNameChange(value: String) {
        _uiState.update { state ->
            state.editCategory?.let {
                state.copy(editCategory = it.copy(name = value, error = null))
            } ?: state
        }
    }

    fun confirmEditCategory() {
        val dialog = _uiState.value.editCategory ?: return
        val trimmed = dialog.name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(editCategory = dialog.copy(error = "Nom requis."))
            }
            return
        }

        if (trimmed == dialog.category.name) {
            _uiState.update { it.copy(editCategory = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCategory = true) }
            repository.updateCategory(dialog.category.id, trimmed)
                .onSuccess {
                    _uiState.update { it.copy(isSavingCategory = false, editCategory = null) }
                    _events.emit(ManageEvent.Success("Categorie modifiee."))
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingCategory = false,
                            editCategory = dialog.copy(error = throwable.message ?: "Modification impossible.")
                        )
                    }
                }
        }
    }

    fun dismissEditCategory() {
        _uiState.update { it.copy(editCategory = null) }
    }

    fun requestDeleteCategory(category: CategoryUi) {
        if (category.isDefault) {
            emitError("Impossible de supprimer cette categorie.")
            return
        }
        _uiState.update { it.copy(deleteCategory = category) }
    }

    fun confirmDeleteCategory() {
        val target = _uiState.value.deleteCategory ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCategory = true) }
            repository.deleteCategory(target.id)
                .onSuccess {
                    _events.emit(ManageEvent.Success("Categorie supprimee."))
                    _uiState.update { it.copy(isSavingCategory = false, deleteCategory = null) }
                }
                .onFailure { throwable ->
                    _events.emit(ManageEvent.Error(throwable.message ?: "Suppression impossible."))
                    _uiState.update { it.copy(isSavingCategory = false, deleteCategory = null) }
                }
        }
    }

    fun dismissDeleteCategory() {
        _uiState.update { it.copy(deleteCategory = null) }
    }

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
                    _events.emit(ManageEvent.Success("Export CSV reussi."))
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
                    _events.emit(ManageEvent.Success("Historique purge."))
                }
                .onFailure { throwable ->
                    _events.emit(
                        ManageEvent.Error(throwable.message ?: "Purge impossible.")
                    )
                }
            _uiState.update { it.copy(isPurging = false) }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(ManageEvent.Error(message))
        }
    }

    private fun Category.toUi(): CategoryUi =
        CategoryUi(
            id = id,
            name = name,
            isDefault = CategoryPresets.isProtected(name)
        )
}
