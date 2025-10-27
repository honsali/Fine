package app.fine.ui.add

data class AddExpenseUiState(
    val step: CaptureStep = CaptureStep.Initial,
    val isRecording: Boolean = false,
    val whatText: String = "",
    val whenText: String = "",
    val howMuchText: String = "",
    val whatError: String? = null,
    val whenError: String? = null,
    val howMuchError: String? = null,
    val isSaving: Boolean = false
)

enum class CaptureStep {
    Initial,
    What,
    When,
    HowMuch
}
