package app.fine.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fine.data.ExpenseRepository
import app.fine.domain.model.CategoryPresets
import app.fine.parser.AmountParser
import app.fine.parser.DateParser
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events = _events.asSharedFlow()

    init {
        ensurePresetCategories()
        observeCategories()
    }

    private fun ensurePresetCategories() {
        viewModelScope.launch {
            CategoryPresets.ProtectedCategoryDisplayNames.forEach { name ->
                repository.ensureCategory(name)
            }
        }
    }

    private fun observeCategories() {
        val quickNames = CategoryPresets.QuickCategoryNames.map { it.lowercase() }
        val orderMap = CategoryPresets.QuickCategoryNames.withIndex()
            .associate { it.value.lowercase() to it.index }

        viewModelScope.launch {
            repository.observeCategories().collectLatest { categories ->
                val quickCategories = categories
                    .filter { quickNames.contains(it.name.lowercase()) }
                    .sortedBy { orderMap[it.name.lowercase()] ?: Int.MAX_VALUE }
                    .map { CategoryOption(id = it.id, name = it.name) }

                _uiState.update { state ->
                    val newState = state.copy(categories = quickCategories)
                    if (newState.selectedCategoryId != null &&
                        quickCategories.none { it.id == newState.selectedCategoryId }
                    ) {
                        newState.copy(selectedCategoryId = null)
                    } else {
                        newState
                    }
                }
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        val categories = _uiState.value.categories
        if (categories.none { it.id == categoryId }) return
        _uiState.value = AddExpenseUiState(
            step = CaptureStep.What,
            isRecording = true,
            categories = categories,
            selectedCategoryId = categoryId
        )
    }

    fun cancel() {
        val categories = _uiState.value.categories
        _uiState.value = AddExpenseUiState(categories = categories)
    }

    fun repeatStep() {
        _uiState.update { state ->
            when (state.step) {
                CaptureStep.What -> state.copy(
                    whatText = "",
                    whatError = null,
                    isRecording = true
                )

                CaptureStep.When -> state.copy(
                    whenText = "",
                    whenError = null,
                    isRecording = true
                )

                CaptureStep.HowMuch -> state.copy(
                    howMuchText = "",
                    howMuchError = null,
                    isRecording = true
                )

                CaptureStep.Initial -> state
            }
        }
    }

    fun onContinue() {
        val state = _uiState.value
        when (state.step) {
            CaptureStep.What -> {
                if (state.whatText.isBlank()) {
                    _uiState.update { it.copy(whatError = "Decris la depense avant de continuer.") }
                } else {
                    _uiState.update {
                        it.copy(
                            step = CaptureStep.When,
                            whenError = null,
                            isRecording = true
                        )
                    }
                }
            }

            CaptureStep.When -> {
                if (state.whenText.isBlank()) {
                    _uiState.update { it.copy(whenError = "Indique la date avant de continuer.") }
                } else {
                    _uiState.update {
                        it.copy(
                            step = CaptureStep.HowMuch,
                            howMuchError = null,
                            isRecording = true
                        )
                    }
                }
            }

            else -> Unit
        }
    }

    fun onFinish() {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId
        if (categoryId == null) {
            viewModelScope.launch {
                _events.emit(AddExpenseEvent.ShowError("Choisis une categorie."))
            }
            return
        }

        if (state.howMuchText.isBlank()) {
            _uiState.update { it.copy(howMuchError = "Indique le montant avant de terminer.") }
            return
        }
        val description = state.whatText.trim()
        if (description.isEmpty()) {
            _uiState.update { it.copy(whatError = "Decris la depense avant de terminer.") }
            return
        }

        val parsedDate = DateParser.parse(
            input = state.whenText,
            referenceDate = LocalDateProvider.now()
        )
        val date = parsedDate.getOrElse { error ->
            _uiState.update { it.copy(whenError = error.message ?: "Date non reconnue.") }
            return
        }

        val amountResult = AmountParser.parse(state.howMuchText)
        val amountMinor = amountResult.getOrElse { error ->
            _uiState.update { it.copy(howMuchError = error.message ?: "Montant non reconnu.") }
            return
        }

        _uiState.update {
            it.copy(
                isRecording = false,
                isSaving = true,
                whatError = null,
                whenError = null,
                howMuchError = null
            )
        }

        viewModelScope.launch {
            val result = repository.addExpense(
                description = description,
                date = date,
                amountMinor = amountMinor,
                categoryId = categoryId
            )
            result
                .onSuccess {
                    val categories = _uiState.value.categories
                    _uiState.value = AddExpenseUiState(categories = categories)
                    _events.emit(
                        AddExpenseEvent.ExpenseSaved(
                            description = description,
                            amountMinor = amountMinor
                        )
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            howMuchError = throwable.message ?: "Sauvegarde impossible."
                        )
                    }
                    _events.emit(
                        AddExpenseEvent.ShowError(
                            throwable.message ?: "Sauvegarde impossible."
                        )
                    )
                }
        }
    }

    fun onTextChanged(step: CaptureStep, value: String) {
        _uiState.update {
            when (step) {
                CaptureStep.What -> it.copy(whatText = value, whatError = null)
                CaptureStep.When -> it.copy(whenText = value, whenError = null)
                CaptureStep.HowMuch -> it.copy(howMuchText = value, howMuchError = null)
                CaptureStep.Initial -> it
            }
        }
    }

    fun onSpeechUpdate(step: CaptureStep, text: String) {
        if (step == CaptureStep.Initial) return
        onTextChanged(step, text)
    }

    fun setRecordingActive(active: Boolean) {
        _uiState.update { it.copy(isRecording = active) }
    }
}

private object LocalDateProvider {
    private const val ZONE_ID = "Africa/Casablanca"
    private val zone = ZoneId.of(ZONE_ID)
    fun now() = java.time.LocalDate.now(zone)
}
