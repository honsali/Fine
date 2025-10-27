package app.fine.di

import android.content.Context
import androidx.room.Room
import app.fine.data.ExpenseRepository
import app.fine.data.ExpenseRepositoryImpl
import app.fine.data.local.ExpenseDatabase
import kotlinx.coroutines.Dispatchers

class AppContainer(context: Context) {

    private val database: ExpenseDatabase = Room.databaseBuilder(
        context,
        ExpenseDatabase::class.java,
        "expenses.db"
    ).fallbackToDestructiveMigration().build()

    val expenseRepository: ExpenseRepository =
        ExpenseRepositoryImpl(
            dao = database.expenseDao(),
            ioDispatcher = Dispatchers.IO
        )
}
