package app.fine.ui.add

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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

                    else -> ActiveStep(
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

@Composable
private fun CategorySelectionGrid(
    state: AddExpenseUiState,
    onCategorySelected: (Long) -> Unit
) {
    val definitions = remember { QuickCategoryDefinition.Definitions }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(definitions) { definition ->
            val option = state.categories.firstOrNull {
                it.name.equals(definition.name, ignoreCase = true)
            }
            CategoryTile(
                label = definition.displayName,
                icon = definition.icon,
                enabled = option != null,
                onClick = {
                    option?.let { onCategorySelected(it.id) }
                }
            )
        }
    }
}

@Composable
private fun CategoryTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun ActiveStep(
    state: AddExpenseUiState,
    onCancel: () -> Unit,
    onRepeat: () -> Unit,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onTextChanged: (CaptureStep, String) -> Unit
) {
    val selectedCategoryName = state.categories
        .firstOrNull { it.id == state.selectedCategoryId }
        ?.name

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedCategoryName?.let { name ->
                Text(
                    text = name.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val textValue = when (state.step) {
                CaptureStep.What -> state.whatText
                CaptureStep.When -> state.whenText
                CaptureStep.HowMuch -> state.howMuchText
                CaptureStep.Initial -> ""
            }

            val canRepeat = textValue.isNotBlank() || !state.isRecording

            val recordingStatus = when {
                state.isRecording -> R.string.add_recording_active
                textValue.isBlank() -> R.string.add_recording_none
                else -> R.string.add_recording_finished
            }

            MicToggleBar(
                isRecording = state.isRecording,
                statusLabel = recordingStatus,
                canRepeat = canRepeat,
                onRepeat = onRepeat,
                onToggle = onMicToggle
            )

            val placeholder = when (state.step) {
                CaptureStep.What -> stringResource(R.string.add_placeholder_what)
                CaptureStep.When -> stringResource(R.string.add_placeholder_when)
                CaptureStep.HowMuch -> stringResource(R.string.add_placeholder_howmuch)
                CaptureStep.Initial -> ""
            }

            val keyboardType = when (state.step) {
                CaptureStep.HowMuch -> KeyboardType.Number
                else -> KeyboardType.Text
            }

            val helperError = when (state.step) {
                CaptureStep.What -> state.whatError
                CaptureStep.When -> state.whenError
                CaptureStep.HowMuch -> state.howMuchError
                CaptureStep.Initial -> null
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { onTextChanged(state.step, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = placeholder) },
                isError = helperError != null,
                supportingText = helperError?.let { error ->
                    {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = stringResource(R.string.action_cancel))
                }

                if (state.step == CaptureStep.HowMuch) {
                    Button(
                        onClick = onFinish,
                        enabled = textValue.isNotBlank() && !state.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = stringResource(R.string.action_finish))
                    }
                } else {
                    Button(
                        onClick = onContinue,
                        enabled = textValue.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = stringResource(R.string.action_continue))
                    }
                }
            }
        }
    }
}

@Composable
private fun MicToggleBar(
    isRecording: Boolean,
    @StringRes statusLabel: Int,
    canRepeat: Boolean,
    onRepeat: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val background = if (isRecording) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(statusLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRepeat,
                    enabled = canRepeat,
                    modifier = Modifier.semantics { role = Role.Button }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = stringResource(R.string.action_repeat)
                    )
                }

                val toggleDescription = stringResource(
                    if (isRecording) R.string.add_recording_toggle_stop_desc else R.string.add_recording_toggle_start_desc
                )
                IconButton(
                    onClick = { onToggle(!isRecording) },
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = toggleDescription
                    }
                ) {
                    if (isRecording) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private data class QuickCategoryDefinition(
    val name: String,
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    companion object {
        val Definitions: List<QuickCategoryDefinition> = listOf(
            QuickCategoryDefinition("Nourriture", "Nourriture", Icons.Filled.LocalDining),
            QuickCategoryDefinition("Carburant", "Carburant", Icons.Filled.LocalGasStation),
            QuickCategoryDefinition("Loyer", "Loyer", Icons.Filled.Home),
            QuickCategoryDefinition("SDK", "SDK", Icons.Filled.Favorite),
            QuickCategoryDefinition("NFK", "NFK", Icons.Filled.AccountBalance),
            QuickCategoryDefinition("Telephone", "Telephone", Icons.Filled.LocalPhone),
            QuickCategoryDefinition("Fibre", "Fibre", Icons.Filled.Wifi),
            QuickCategoryDefinition("Payage", "Payage", Icons.Filled.Paid),
            QuickCategoryDefinition("Gardiennage", "Gardiennage", Icons.Filled.Security),
            QuickCategoryDefinition("Scolarite", "Scolarité", Icons.Filled.School),
            QuickCategoryDefinition("Aide", "Aide", Icons.Filled.EmojiPeople),
            QuickCategoryDefinition("Voiture", "Voiture", Icons.Filled.DirectionsCar),
            QuickCategoryDefinition("EE", "EE", Icons.Filled.Bolt),
            QuickCategoryDefinition("Gaz", "Gaz", Icons.Filled.Whatshot),
            QuickCategoryDefinition("Perso", "Perso", Icons.Filled.AttachMoney)
        )
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
