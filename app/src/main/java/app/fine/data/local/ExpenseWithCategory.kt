package app.fine.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategory(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity
)
