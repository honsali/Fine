package app.fine.ui.manage

sealed interface ManageEvent {
    data class Success(val message: String) : ManageEvent
    data class Error(val message: String) : ManageEvent
}
