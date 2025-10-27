package app.fine.ui.add

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fine.R
import app.fine.ui.theme.FineTheme

@Composable
fun AddScreen(
    state: AddExpenseUiState,
    onStart: () -> Unit,
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
                    CaptureStep.Initial -> InitialStep(onStart)
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
private fun InitialStep(onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.add_intro_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.add_intro_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.action_start))
            }
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
    val headline = when (state.step) {
        CaptureStep.What -> stringResource(R.string.add_step_what_title)
        CaptureStep.When -> stringResource(R.string.add_step_when_title)
        CaptureStep.HowMuch -> stringResource(R.string.add_step_howmuch_title)
        CaptureStep.Initial -> ""
    }
    val placeholder = when (state.step) {
        CaptureStep.What -> stringResource(R.string.add_placeholder_what)
        CaptureStep.When -> stringResource(R.string.add_placeholder_when)
        CaptureStep.HowMuch -> stringResource(R.string.add_placeholder_howmuch)
        CaptureStep.Initial -> ""
    }
    val supportingError = when (state.step) {
        CaptureStep.What -> state.whatError
        CaptureStep.When -> state.whenError
        CaptureStep.HowMuch -> state.howMuchError
        CaptureStep.Initial -> null
    }
    val textValue = when (state.step) {
        CaptureStep.What -> state.whatText
        CaptureStep.When -> state.whenText
        CaptureStep.HowMuch -> state.howMuchText
        CaptureStep.Initial -> ""
    }
    val keyboardOptions = if (state.step == CaptureStep.HowMuch) {
        KeyboardOptions(keyboardType = KeyboardType.Decimal)
    } else {
        KeyboardOptions.Default
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            val recordingStatus = when {
                state.isRecording -> R.string.add_recording_active
                textValue.isNotBlank() -> R.string.add_recording_finished
                else -> R.string.add_recording_none
            }

            MicToggleButton(
                isRecording = state.isRecording,
                enabled = !state.isSaving,
                statusLabel = recordingStatus,
                onToggle = onMicToggle
            )

            OutlinedTextField(
                value = textValue,
                onValueChange = { onTextChanged(state.step, it) },
                label = { Text(text = placeholder) },
                placeholder = { Text(text = placeholder) },
                singleLine = state.step != CaptureStep.What,
                isError = supportingError != null,
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth()
            )
            supportingError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                    val canRepeat = textValue.isNotBlank() || !state.isRecording
                    Button(
                        onClick = onRepeat,
                        enabled = canRepeat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(text = stringResource(R.string.action_repeat))
                    }
                }

                if (state.step == CaptureStep.HowMuch) {
                    Button(
                        onClick = onFinish,
                        enabled = !state.isSaving && textValue.isNotBlank(),
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
private fun MicToggleButton(
    isRecording: Boolean,
    enabled: Boolean,
    @StringRes statusLabel: Int,
    onToggle: (Boolean) -> Unit
) {
    val background = if (isRecording) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = background
        )
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
            IconButton(
                onClick = { onToggle(!isRecording) },
                enabled = enabled
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

@Preview(showBackground = true, locale = "fr")
@Composable
private fun AddScreenPreviewInitial() {
    FineTheme {
        AddScreen(
            state = AddExpenseUiState(),
            onStart = {},
            onCancel = {},
            onRepeat = {},
            onContinue = {},
            onFinish = {},
            onMicToggle = {},
            onTextChanged = { _, _ -> }
        )
    }
}
