package app.fine.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fine.R
import app.fine.domain.model.Expense
import app.fine.domain.model.MonthlyExpenses
import app.fine.ui.theme.FineTheme
import app.fine.util.MoneyFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale("fr", "FR"))
    }

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
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            if (state.months.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.months) { month ->
                        MonthlyHistoryCard(
                            month = month,
                            dateFormatter = dateFormatter,
                            monthFormatter = monthFormatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyHistoryCard(
    month: MonthlyExpenses,
    dateFormatter: DateTimeFormatter,
    monthFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val header = monthFormatter.format(month.yearMonth.atDay(1))
            Text(
                text = stringResource(
                    R.string.history_month_header,
                    header.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("fr", "FR")) else it.toString() },
                    MoneyFormatter.format(month.totalMinor)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            month.expenses.forEach { expense ->
                ExpenseRow(
                    expense = expense,
                    dateFormatter = dateFormatter
                )
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    dateFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = dateFormatter.format(expense.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = MoneyFormatter.format(expense.amountMinor),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.alignByBaseline()
        )
    }
}

@Preview(locale = "fr")
@Composable
private fun HistoryScreenPreview() {
    val mockMonth = MonthlyExpenses(
        yearMonth = YearMonth.of(2025, 10),
        totalMinor = 345000,
        expenses = listOf(
            Expense(1, "Déjeuner client", LocalDate.of(2025, 10, 12), 12000, Instant.now(), "voice"),
            Expense(2, "Taxi", LocalDate.of(2025, 10, 10), 4500, Instant.now(), "voice")
        )
    )
    FineTheme {
        HistoryScreen(
            state = HistoryUiState(months = listOf(mockMonth))
        )
    }
}
