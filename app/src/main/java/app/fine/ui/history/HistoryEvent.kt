package app.fine.ui.history

sealed interface HistoryEvent {
    data class Message(val text: String) : HistoryEvent
}
