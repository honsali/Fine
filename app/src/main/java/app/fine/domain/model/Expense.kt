package app.fine.domain.model

import java.time.Instant
import java.time.LocalDate

data class Expense(
    val id: Long,
    val description: String,
    val date: LocalDate,
    val amountMinor: Long,
    val createdAt: Instant,
    val source: String
)
