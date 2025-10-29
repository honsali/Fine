package app.fine.data

import app.fine.domain.model.Category
import app.fine.domain.model.Expense
import app.fine.domain.model.MonthlyExpenses
import java.io.OutputStream
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>
    fun observeMonthlyExpenses(): Flow<List<MonthlyExpenses>>
    fun observeCategories(): Flow<List<Category>>
    suspend fun addExpense(
        description: String,
        date: LocalDate,
        amountMinor: Long,
        categoryId: Long? = null,
        source: String = "voice"
    ): Result<Long>

    suspend fun updateExpense(
        id: Long,
        description: String,
        date: LocalDate,
        amountMinor: Long,
        categoryId: Long
    ): Result<Unit>

    suspend fun deleteExpense(id: Long): Result<Unit>
    suspend fun addCategory(name: String): Result<Long>
    suspend fun updateCategory(id: Long, name: String): Result<Unit>
    suspend fun deleteCategory(id: Long): Result<Unit>
    suspend fun ensureCategory(name: String): Result<Long>

    suspend fun purgeAll()
    suspend fun exportCsv(outputStream: OutputStream): Result<Unit>
}
