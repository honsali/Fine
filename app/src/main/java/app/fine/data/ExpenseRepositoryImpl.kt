package app.fine.data

import app.fine.data.local.ExpenseDao
import app.fine.data.local.ExpenseEntity
import app.fine.domain.model.Expense
import app.fine.domain.model.MonthlyExpenses
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
    private val ioDispatcher: CoroutineDispatcher
) : ExpenseRepository {

    override fun observeExpenses(): Flow<List<Expense>> =
        dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeMonthlyExpenses(): Flow<List<MonthlyExpenses>> =
        observeExpenses().map { expenses ->
            expenses
                .groupBy { YearMonth.from(it.date) }
                .toList()
                .sortedByDescending { it.first }
                .map { (yearMonth, monthlyExpenses) ->
                    MonthlyExpenses(
                        yearMonth = yearMonth,
                        totalMinor = monthlyExpenses.sumOf { it.amountMinor },
                        expenses = monthlyExpenses.sortedWith(
                            compareByDescending<Expense> { it.date }
                                .thenByDescending { it.createdAt }
                        )
                    )
                }
        }

    override suspend fun addExpense(
        description: String,
        date: LocalDate,
        amountMinor: Long,
        source: String
    ): Result<Long> = runCatching {
        require(description.isNotBlank()) { "Description manquante." }
        require(description.length <= 500) { "Description trop longue." }
        require(amountMinor > 0) { "Montant invalide." }
        withContext(ioDispatcher) {
            val entity = ExpenseEntity(
                description = description,
                date = DATE_FORMATTER.format(date),
                amountMinor = amountMinor,
                createdAt = Instant.now().toEpochMilli(),
                source = source
            )
            dao.insert(entity)
        }
    }

    override suspend fun updateExpense(
        id: Long,
        description: String,
        date: LocalDate,
        amountMinor: Long
    ): Result<Unit> = runCatching {
        require(description.isNotBlank()) { "Description manquante." }
        require(description.length <= 500) { "Description trop longue." }
        require(amountMinor > 0) { "Montant invalide." }
        withContext(ioDispatcher) {
            val existing = dao.getById(id) ?: error("Depense introuvable.")
            dao.update(
                existing.copy(
                    description = description,
                    date = DATE_FORMATTER.format(date),
                    amountMinor = amountMinor
                )
            )
        }
    }

    override suspend fun deleteExpense(id: Long): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            dao.delete(id)
        }
    }

    override suspend fun purgeAll() {
        withContext(ioDispatcher) {
            dao.deleteAll()
        }
    }

    override suspend fun exportCsv(outputStream: OutputStream): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.appendLine("date;description;amount_mad")
                val entities = dao.getAllForExport()
                entities.forEach { entity ->
                    writer.appendLine(
                        listOf(
                            entity.date,
                            escapeCsv(entity.description),
                            formatAmount(entity.amountMinor)
                        ).joinToString(separator = ";")
                    )
                }
            }
        }
    }

    private fun ExpenseEntity.toDomain(): Expense = Expense(
        id = id,
        description = description,
        date = LocalDate.parse(date, DATE_FORMATTER),
        amountMinor = amountMinor,
        createdAt = Instant.ofEpochMilli(createdAt),
        source = source
    )

    private fun escapeCsv(value: String): String =
        if (value.contains(';') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }

    private fun formatAmount(amountMinor: Long): String = buildString {
        append(amountMinor / 100)
        append('.')
        append((amountMinor % 100).toString().padStart(2, '0'))
    }

    companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    }
}
