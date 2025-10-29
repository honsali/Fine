package app.fine.data

import android.database.sqlite.SQLiteConstraintException
import app.fine.data.local.CategoryEntity
import app.fine.data.local.ExpenseDao
import app.fine.data.local.ExpenseEntity
import app.fine.data.local.ExpenseWithCategory
import app.fine.domain.model.Category
import app.fine.domain.model.CategoryPresets
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
        dao.observeAll().map { items -> items.map { it.toDomain() } }

    override fun observeMonthlyExpenses(): Flow<List<MonthlyExpenses>> =
        observeExpenses().map { expenses ->
            expenses
                .groupBy { YearMonth.from(it.date) }
                .toList()
                .sortedByDescending { it.first }
                .map { (yearMonth, groupedExpenses) ->
                    MonthlyExpenses(
                        yearMonth = yearMonth,
                        totalMinor = groupedExpenses.sumOf { it.amountMinor },
                        expenses = groupedExpenses.sortedWith(
                            compareByDescending<Expense> { it.date }
                                .thenByDescending { it.createdAt }
                        )
                    )
                }
        }

    override fun observeCategories(): Flow<List<Category>> =
        dao.observeCategories().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addExpense(
        description: String,
        date: LocalDate,
        amountMinor: Long,
        categoryId: Long?,
        source: String
    ): Result<Long> = runCatching {
        require(description.isNotBlank()) { "Description manquante." }
        require(description.length <= 500) { "Description trop longue." }
        require(amountMinor > 0) { "Montant invalide." }
        withContext(ioDispatcher) {
            val resolvedCategoryId = categoryId?.let { existingCategoryIdOrThrow(it) }
                ?: ensureCategoryInternal(CategoryPresets.DefaultCategoryName).id
            val entity = ExpenseEntity(
                description = description,
                date = DATE_FORMATTER.format(date),
                amountMinor = amountMinor,
                createdAt = Instant.now().toEpochMilli(),
                source = source,
                categoryId = resolvedCategoryId
            )
            dao.insert(entity)
        }
    }

    override suspend fun updateExpense(
        id: Long,
        description: String,
        date: LocalDate,
        amountMinor: Long,
        categoryId: Long
    ): Result<Unit> = runCatching {
        require(description.isNotBlank()) { "Description manquante." }
        require(description.length <= 500) { "Description trop longue." }
        require(amountMinor > 0) { "Montant invalide." }
        withContext(ioDispatcher) {
            val existing = dao.getExpenseById(id) ?: error("Depense introuvable.")
            val resolvedCategoryId = existingCategoryIdOrThrow(categoryId)
            dao.update(
                existing.copy(
                    description = description,
                    date = DATE_FORMATTER.format(date),
                    amountMinor = amountMinor,
                    categoryId = resolvedCategoryId
                )
            )
        }
    }

    override suspend fun deleteExpense(id: Long): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            dao.delete(id)
        }
    }

    override suspend fun addCategory(name: String): Result<Long> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Nom de categorie vide." }
        require(trimmed.length <= 100) { "Nom de categorie trop long." }
        withContext(ioDispatcher) {
            dao.getCategoryByName(trimmed)?.let { error("Categorie deja existante.") }
            dao.insertCategory(CategoryEntity(name = trimmed))
        }
    }

    override suspend fun updateCategory(id: Long, name: String): Result<Unit> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Nom de categorie vide." }
        require(trimmed.length <= 100) { "Nom de categorie trop long." }
        withContext(ioDispatcher) {
            val existing = dao.getCategoryById(id) ?: error("Categorie introuvable.")
            ensureNotDefault(existing)
            dao.getCategoryByName(trimmed)?.takeIf { it.id != id }?.let {
                error("Categorie deja existante.")
            }
            dao.updateCategory(existing.copy(name = trimmed))
        }
    }

    override suspend fun deleteCategory(id: Long): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val target = dao.getCategoryById(id) ?: error("Categorie introuvable.")
            ensureNotDefault(target)
            val fallback = ensureCategoryInternal(CategoryPresets.DefaultCategoryName)
            dao.reassignExpensesToCategory(target.id, fallback.id)
            dao.deleteCategory(target.id)
        }
    }

    override suspend fun ensureCategory(name: String): Result<Long> = runCatching {
        withContext(ioDispatcher) {
            ensureCategoryInternal(name).id
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
                writer.appendLine("date;description;amount_mad;category")
                val rows = dao.getAllForExport()
                rows.forEach { row ->
                    val expense = row.expense
                    writer.appendLine(
                        listOf(
                            expense.date,
                            escapeCsv(expense.description),
                            formatAmount(expense.amountMinor),
                            escapeCsv(row.category.name)
                        ).joinToString(separator = ";")
                    )
                }
            }
        }
    }

    private suspend fun ensureCategoryInternal(name: String): CategoryEntity {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Nom de categorie vide." }
        dao.getCategoryByName(trimmed)?.let { return it }
        return try {
            val id = dao.insertCategory(CategoryEntity(name = trimmed))
            dao.getCategoryById(id) ?: error("Categorie introuvable.")
        } catch (constraint: SQLiteConstraintException) {
            dao.getCategoryByName(trimmed) ?: throw constraint
        }
    }

    private suspend fun existingCategoryIdOrThrow(categoryId: Long): Long =
        dao.getCategoryById(categoryId)?.id ?: error("Categorie introuvable.")

    private fun ensureNotDefault(category: CategoryEntity) {
        check(!CategoryPresets.isProtected(category.name)) {
            "Impossible de modifier cette categorie."
        }
    }

    private fun ExpenseWithCategory.toDomain(): Expense = Expense(
        id = expense.id,
        description = expense.description,
        date = LocalDate.parse(expense.date, DATE_FORMATTER),
        amountMinor = expense.amountMinor,
        createdAt = Instant.ofEpochMilli(expense.createdAt),
        source = expense.source,
        category = category.toDomain()
    )

    private fun CategoryEntity.toDomain(): Category =
        Category(id = id, name = name)

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
