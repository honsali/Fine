package app.fine.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.title_add),
            style = MaterialTheme.typography.headlineMedium
        )

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

@Composable
private fun InitialStep(onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.add_intro_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onStart) {
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge
            )

            MicToggleButton(
                isRecording = state.isRecording,
                enabled = !state.isSaving,
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
                    TextButton(onClick = onCancel) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                    val canRepeat = textValue.isNotBlank()
                    OutlinedButton(
                        onClick = onRepeat,
                        enabled = canRepeat
                    ) {
                        Text(text = stringResource(R.string.action_repeat))
                    }
                }

                if (state.step == CaptureStep.HowMuch) {
                    Button(
                        onClick = onFinish,
                        enabled = !state.isSaving && textValue.isNotBlank()
                    ) {
                        Text(text = stringResource(R.string.action_finish))
                    }
                } else {
                    Button(
                        onClick = onContinue,
                        enabled = textValue.isNotBlank()
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
    onToggle: (Boolean) -> Unit
) {
    val labelId = if (isRecording) {
        R.string.action_stop_recording
    } else {
        R.string.action_start_recording
    }
    val background = if (isRecording) {
        MaterialTheme.colorScheme.primaryContainer
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
                text = stringResource(labelId),
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(
                onClick = { onToggle(!isRecording) },
                enabled = enabled
            ) {
                val icon = if (isRecording) {
                    Icons.Filled.MicOff
                } else {
                    Icons.Filled.Mic
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isRecording) MaterialTheme.colorScheme.primary else Color.Unspecified
                )
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
