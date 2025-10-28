package app.fine.data

import app.fine.domain.model.Expense
import app.fine.domain.model.MonthlyExpenses
import java.io.OutputStream
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>
    fun observeMonthlyExpenses(): Flow<List<MonthlyExpenses>>
    suspend fun addExpense(
        description: String,
        date: LocalDate,
        amountMinor: Long,
        source: String = "voice"
    ): Result<Long>

    suspend fun updateExpense(
        id: Long,
        description: String,
        date: LocalDate,
        amountMinor: Long
    ): Result<Unit>

    suspend fun deleteExpense(id: Long): Result<Unit>

    suspend fun purgeAll()
    suspend fun exportCsv(outputStream: OutputStream): Result<Unit>
}
