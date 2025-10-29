package app.fine.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    @Transaction
    @Query(
        """
            SELECT * FROM expenses
            ORDER BY date DESC, created_at DESC
        """
    )
    fun observeAll(): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query(
        """
            SELECT * FROM expenses
            WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month
            ORDER BY date DESC, created_at DESC
        """
    )
    fun observeByMonth(year: String, month: String): Flow<List<ExpenseWithCategory>>

    @Query(
        """
            SELECT COALESCE(SUM(amount_minor), 0) FROM expenses
            WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month
        """
    )
    fun sumForMonth(year: String, month: String): Flow<Long>

    @Transaction
    @Query(
        """
            SELECT * FROM expenses
            ORDER BY date ASC, created_at ASC
        """
    )
    suspend fun getAllForExport(): List<ExpenseWithCategory>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM expenses WHERE category_id = :categoryId")
    suspend fun countExpensesForCategory(categoryId: Long): Long

    @Query("UPDATE expenses SET category_id = :targetCategoryId WHERE category_id = :sourceCategoryId")
    suspend fun reassignExpensesToCategory(sourceCategoryId: Long, targetCategoryId: Long)
}
