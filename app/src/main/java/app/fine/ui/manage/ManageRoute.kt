package app.fine.ui.manage

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ManageRoute(
    viewModel: ManageViewModel,
    onMessage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmPurge by remember { mutableStateOf(false) }
    val fileNameFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTo(context.contentResolver, uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ManageEvent.Success -> onMessage(event.message)
                is ManageEvent.Error -> onMessage(event.message)
            }
        }
    }

    ManageScreen(
        state = uiState,
        showConfirmDialog = confirmPurge,
        onExport = {
            val formattedDate = fileNameFormatter.format(LocalDate.now())
            exportLauncher.launch("fine-expenses-$formattedDate.csv")
        },
        onPurgeRequest = { confirmPurge = true },
        onConfirmPurge = {
            confirmPurge = false
            viewModel.purgeAll()
        },
        onDismissDialog = { confirmPurge = false },
        onNewCategoryNameChange = viewModel::onNewCategoryNameChange,
        onAddCategory = viewModel::createCategory,
        onEditCategory = viewModel::startEditCategory,
        onDeleteCategory = viewModel::requestDeleteCategory,
        onEditCategoryNameChange = viewModel::onEditCategoryNameChange,
        onConfirmEditCategory = viewModel::confirmEditCategory,
        onDismissEditCategory = viewModel::dismissEditCategory,
        onConfirmDeleteCategory = viewModel::confirmDeleteCategory,
        onDismissDeleteCategory = viewModel::dismissDeleteCategory
    )
}
