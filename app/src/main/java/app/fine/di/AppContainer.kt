package app.fine.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.fine.data.ExpenseRepository
import app.fine.data.ExpenseRepositoryImpl
import app.fine.data.local.ExpenseDatabase
import app.fine.domain.model.CategoryPresets
import kotlinx.coroutines.Dispatchers

class AppContainer(context: Context) {

    private val database: ExpenseDatabase = Room.databaseBuilder(
        context,
        ExpenseDatabase::class.java,
        "expenses.db"
        // Personal project: destructive migrations are acceptable. Export data before upgrading.
    )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CategoryPresets.ProtectedCategoryDisplayNames.forEach { name ->
                    db.execSQL("INSERT INTO categories(name) VALUES (?)", arrayOf(name))
                }
            }
        })
        .build()

    val expenseRepository: ExpenseRepository =
        ExpenseRepositoryImpl(
            dao = database.expenseDao(),
            ioDispatcher = Dispatchers.IO
        )
}
