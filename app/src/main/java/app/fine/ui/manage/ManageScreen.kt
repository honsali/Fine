package app.fine.ui.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.manage_title),
            style = MaterialTheme.typography.headlineSmall
        )

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
                    enabled = !state.isExporting
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
                    enabled = !state.isPurging
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

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = {
                Text(text = stringResource(R.string.manage_purge_confirm_title))
            },
            text = {
                Text(text = stringResource(R.string.manage_purge_confirm_body))
            },
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
}

@Preview(locale = "fr")
@Composable
private fun ManageScreenPreview() {
    FineTheme {
        ManageScreen(
            state = ManageUiState(),
            showConfirmDialog = false,
            onExport = {},
            onPurgeRequest = {},
            onConfirmPurge = {},
            onDismissDialog = {}
        )
    }
}
