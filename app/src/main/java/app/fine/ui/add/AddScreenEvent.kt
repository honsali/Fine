package app.fine.ui.add

sealed interface AddScreenEvent {
    data class ShowMessage(val message: String) : AddScreenEvent
}
