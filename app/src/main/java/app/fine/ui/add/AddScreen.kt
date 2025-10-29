package app.fine.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fine.R
import app.fine.domain.model.CategoryPresets
import app.fine.ui.theme.FineTheme

@Composable
fun AddScreen(
    state: AddExpenseUiState,
    onCategorySelected: (Long) -> Unit,
    onCancel: () -> Unit,
    onRepeat: () -> Unit,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onTextChanged: (CaptureStep, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_add),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                when (state.step) {
                    CaptureStep.Initial -> CategorySelectionGrid(
                        state = state,
                        onCategorySelected = onCategorySelected
                    )

                    else -> ActiveStepCard(
                        state = state,
                        onCancel = onCancel,
                        onRepeat = onRepeat,
                        onContinue = onContinue,
                        onFinish = onFinish,
                        onMicToggle = onMicToggle,
                        onTextChanged = onTextChanged
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "fr")
@Composable
private fun AddScreenPreviewInitial() {
    FineTheme {
        AddScreen(
            state = AddExpenseUiState(
                categories = CategoryPresets.QuickCategoryNames.mapIndexed { index, name ->
                    CategoryOption(id = index.toLong() + 1, name = name)
                }
            ),
            onCategorySelected = {},
            onCancel = {},
            onRepeat = {},
            onContinue = {},
            onFinish = {},
            onMicToggle = {},
            onTextChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, locale = "fr")
@Composable
private fun AddScreenPreviewActive() {
    FineTheme {
        AddScreen(
            state = AddExpenseUiState(
                step = CaptureStep.What,
                isRecording = true,
                categories = CategoryPresets.QuickCategoryNames.mapIndexed { index, name ->
                    CategoryOption(id = index.toLong() + 1, name = name)
                },
                selectedCategoryId = 1,
                whatText = "Dejeuner client"
            ),
            onCategorySelected = {},
            onCancel = {},
            onRepeat = {},
            onContinue = {},
            onFinish = {},
            onMicToggle = {},
            onTextChanged = { _, _ -> }
        )
    }
}
