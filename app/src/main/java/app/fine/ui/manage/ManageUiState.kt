package app.fine.ui.manage

data class ManageUiState(
    val isExporting: Boolean = false,
    val isPurging: Boolean = false,
    val categories: List<CategoryUi> = emptyList(),
    val newCategoryName: String = "",
    val isSavingCategory: Boolean = false,
    val categoryError: String? = null,
    val editCategory: CategoryEditState? = null,
    val deleteCategory: CategoryUi? = null
)

data class CategoryUi(
    val id: Long,
    val name: String,
    val isDefault: Boolean
)

data class CategoryEditState(
    val category: CategoryUi,
    val name: String,
    val error: String? = null
)
