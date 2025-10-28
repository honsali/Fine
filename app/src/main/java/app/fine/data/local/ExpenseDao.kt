package app.fine.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    @Query(
        """
            SELECT * FROM expenses
            ORDER BY date DESC, created_at DESC
        """
    )
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query(
        """
            SELECT * FROM expenses
            WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month
            ORDER BY date DESC, created_at DESC
        """
    )
    fun observeByMonth(year: String, month: String): Flow<List<ExpenseEntity>>

    @Query(
        """
            SELECT COALESCE(SUM(amount_minor), 0) FROM expenses
            WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month
        """
    )
    fun sumForMonth(year: String, month: String): Flow<Long>

    @Query(
        """
            SELECT * FROM expenses
            ORDER BY date ASC, created_at ASC
        """
    )
    suspend fun getAllForExport(): List<ExpenseEntity>
}
