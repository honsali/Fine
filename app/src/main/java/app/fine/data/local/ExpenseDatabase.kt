package app.fine.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
