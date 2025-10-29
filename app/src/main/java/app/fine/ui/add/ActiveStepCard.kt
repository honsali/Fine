package app.fine.ui.add

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import app.fine.R

@Composable
internal fun ActiveStepCard(
    state: AddExpenseUiState,
    onCancel: () -> Unit,
    onRepeat: () -> Unit,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onTextChanged: (CaptureStep, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategoryName = state.categories
        .firstOrNull { it.id == state.selectedCategoryId }
        ?.name

    Card(
        modifier = modifier.fillMaxWidth(),
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

            val outlinedButtonColors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContainerColor = Color.Transparent
            )
            val buttonTextStyle = MaterialTheme.typography.bodySmall

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = outlinedButtonColors,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = buttonTextStyle
                    )
                }

                if (state.step == CaptureStep.HowMuch) {
                    val isFinishEnabled = textValue.isNotBlank() && !state.isSaving
                    OutlinedButton(
                        onClick = onFinish,
                        enabled = isFinishEnabled,
                        colors = outlinedButtonColors,
                        border = BorderStroke(
                            0.5.dp,
                            if (isFinishEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.action_finish),
                            style = buttonTextStyle
                        )
                    }
                } else {
                    val isContinueEnabled = textValue.isNotBlank()
                    OutlinedButton(
                        onClick = onContinue,
                        enabled = isContinueEnabled,
                        colors = outlinedButtonColors,
                        border = BorderStroke(
                            0.5.dp,
                            if (isContinueEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.action_continue),
                            style = buttonTextStyle
                        )
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
                    if (isRecording) {
                        R.string.add_recording_toggle_stop_desc
                    } else {
                        R.string.add_recording_toggle_start_desc
                    }
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
