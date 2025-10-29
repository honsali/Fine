package app.fine.ui.manage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fine.R
import app.fine.ui.theme.FineTheme

@Composable
fun ManageScreen(
    state: ManageUiState,
    showConfirmDialog: Boolean,
    onExport: () -> Unit,
    onPurgeRequest: () -> Unit,
    onConfirmPurge: () -> Unit,
    onDismissDialog: () -> Unit,
    onNewCategoryNameChange: (String) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (CategoryUi) -> Unit,
    onDeleteCategory: (CategoryUi) -> Unit,
    onEditCategoryNameChange: (String) -> Unit,
    onConfirmEditCategory: () -> Unit,
    onDismissEditCategory: () -> Unit,
    onConfirmDeleteCategory: () -> Unit,
    onDismissDeleteCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.manage_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CategoryCard(
                    state = state,
                    onNewCategoryNameChange = onNewCategoryNameChange,
                    onAddCategory = onAddCategory,
                    onEditCategory = onEditCategory,
                    onDeleteCategory = onDeleteCategory
                )

                ExportCard(state = state, onExport = onExport)

                PurgeCard(
                    state = state,
                    onPurgeRequest = onPurgeRequest
                )

                AboutCard()
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(text = stringResource(R.string.manage_purge_confirm_title)) },
            text = { Text(text = stringResource(R.string.manage_purge_confirm_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmPurge) {
                    Text(text = stringResource(R.string.manage_purge_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(text = stringResource(R.string.manage_purge_confirm_no))
                }
            }
        )
    }

    state.editCategory?.let { editState ->
        AlertDialog(
            onDismissRequest = onDismissEditCategory,
            title = { Text(text = stringResource(R.string.manage_category_edit_title)) },
            text = {
                OutlinedTextField(
                    value = editState.name,
                    onValueChange = onEditCategoryNameChange,
                    label = { Text(text = stringResource(R.string.manage_category_name_label)) },
                    isError = editState.error != null,
                    supportingText = editState.error?.let { error ->
                        {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmEditCategory,
                    enabled = !state.isSavingCategory
                ) {
                    Text(text = stringResource(R.string.manage_category_edit_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEditCategory) {
                    Text(text = stringResource(R.string.manage_category_edit_cancel))
                }
            }
        )
    }

    state.deleteCategory?.let { category ->
        AlertDialog(
            onDismissRequest = onDismissDeleteCategory,
            title = { Text(text = stringResource(R.string.manage_category_delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.manage_category_delete_message,
                        category.name
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDeleteCategory,
                    enabled = !state.isSavingCategory
                ) {
                    Text(text = stringResource(R.string.manage_category_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteCategory) {
                    Text(text = stringResource(R.string.manage_category_delete_cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    state: ManageUiState,
    onNewCategoryNameChange: (String) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (CategoryUi) -> Unit,
    onDeleteCategory: (CategoryUi) -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_categories_title),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = state.newCategoryName,
                onValueChange = onNewCategoryNameChange,
                label = { Text(text = stringResource(R.string.manage_category_name_label)) },
                isError = state.categoryError != null,
                supportingText = state.categoryError?.let { error ->
                    {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onAddCategory,
                enabled = state.newCategoryName.isNotBlank() && !state.isSavingCategory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.manage_category_add))
            }

            if (state.categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.manage_category_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.categories.forEachIndexed { index, category ->
                        CategoryRow(
                            category = category,
                            isBusy = state.isSavingCategory,
                            onEditCategory = onEditCategory,
                            onDeleteCategory = onDeleteCategory
                        )
                        if (index != state.categories.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: CategoryUi,
    isBusy: Boolean,
    onEditCategory: (CategoryUi) -> Unit,
    onDeleteCategory: (CategoryUi) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (category.isDefault) {
                Text(
                    text = stringResource(R.string.manage_category_default_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { onEditCategory(category) },
                enabled = !category.isDefault && !isBusy
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.manage_category_edit)
                )
            }
            IconButton(
                onClick = { onDeleteCategory(category) },
                enabled = !category.isDefault && !isBusy
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.manage_category_delete)
                )
            }
        }
    }
}

@Composable
private fun ExportCard(
    state: ManageUiState,
    onExport: () -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_export_description),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onExport,
                enabled = !state.isExporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (state.isExporting) {
                        stringResource(R.string.manage_export_in_progress)
                    } else {
                        stringResource(R.string.manage_export_button)
                    }
                )
            }
        }
    }
}

@Composable
private fun PurgeCard(
    state: ManageUiState,
    onPurgeRequest: () -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_purge_description),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = onPurgeRequest,
                enabled = !state.isPurging,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (state.isPurging) {
                        stringResource(R.string.manage_purge_in_progress)
                    } else {
                        stringResource(R.string.manage_purge_button)
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_about_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.manage_about_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(locale = "fr")
@Composable
private fun ManageScreenPreview() {
    FineTheme {
        ManageScreen(
            state = ManageUiState(
                categories = listOf(
                    CategoryUi(id = 1, name = "Divers", isDefault = true),
                    CategoryUi(id = 2, name = "Transport", isDefault = false)
                )
            ),
            showConfirmDialog = false,
            onExport = {},
            onPurgeRequest = {},
            onConfirmPurge = {},
            onDismissDialog = {},
            onNewCategoryNameChange = {},
            onAddCategory = {},
            onEditCategory = {},
            onDeleteCategory = {},
            onEditCategoryNameChange = {},
            onConfirmEditCategory = {},
            onDismissEditCategory = {},
            onConfirmDeleteCategory = {},
            onDismissDeleteCategory = {}
        )
    }
}
