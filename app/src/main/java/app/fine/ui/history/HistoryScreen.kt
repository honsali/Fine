package app.fine.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fine.R
import app.fine.domain.model.Category
import app.fine.domain.model.Expense
import app.fine.domain.model.MonthlyExpenses
import app.fine.ui.theme.FineTheme
import app.fine.util.MoneyFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onDeleteExpense: (Long) -> Unit,
    onUpdateExpense: (Long, String, String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormatter = MONTH_FORMATTER
    var editTarget by remember { mutableStateOf<Expense?>(null) }
    var editDescription by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf("") }
    var editAmount by remember { mutableStateOf("") }
    var editCategoryId by remember { mutableStateOf<Long?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(editTarget?.id) {
        val target = editTarget ?: return@LaunchedEffect
        editDescription = target.description
        editDate = EDIT_DATE_FORMAT.format(target.date)
        editAmount = MoneyFormatter.formatPlain(target.amountMinor)
        editCategoryId = target.category.id
        categoryMenuExpanded = false
    }

    LaunchedEffect(state.categories) {
        if (editTarget != null) {
            val currentId = editCategoryId
            val available = currentId != null && state.categories.any { it.id == currentId }
            if (!available) {
                editCategoryId = state.categories.firstOrNull()?.id
            }
        }
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
                    items(state.months, key = { it.yearMonth.toString() }) { month ->
                        MonthlyHistoryCard(
                            month = month,
                            monthFormatter = monthFormatter,
                            onExpenseClick = { expense -> editTarget = expense }
                        )
                    }
                }
            }
        }
    }

    editTarget?.let { expense ->
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text(text = stringResource(R.string.history_edit_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text(text = stringResource(R.string.history_edit_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text(text = stringResource(R.string.history_edit_date)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text(text = stringResource(R.string.history_edit_amount)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val categories = state.categories
                    val selectedCategory = categories.firstOrNull { it.id == editCategoryId }
                    Box {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = stringResource(R.string.history_edit_category)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = categories.isNotEmpty()) {
                                    categoryMenuExpanded = !categoryMenuExpanded
                                },
                            enabled = categories.isNotEmpty(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        editCategoryId = category.id
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val categoryId = editCategoryId ?: state.categories.firstOrNull()?.id
                        if (categoryId != null) {
                            onUpdateExpense(expense.id, editDescription, editDate, editAmount, categoryId)
                            editTarget = null
                        }
                    },
                    enabled = state.categories.isNotEmpty()
                ) {
                    Text(text = stringResource(R.string.history_edit_confirm))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { editTarget = null }) {
                        Text(text = stringResource(R.string.history_edit_cancel))
                    }
                    Button(
                        onClick = {
                            onDeleteExpense(expense.id)
                            editTarget = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = stringResource(R.string.history_edit_delete))
                    }
                }
            }
        )
    }
}

@Composable
private fun MonthlyHistoryCard(
    month: MonthlyExpenses,
    monthFormatter: DateTimeFormatter,
    onExpenseClick: (Expense) -> Unit,
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
                    header.replaceFirstChar { if (it.isLowerCase()) it.titlecase(LOCALE_FR) else it.toString() },
                    MoneyFormatter.format(month.totalMinor)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            month.expenses.forEachIndexed { index, expense ->
                ExpenseRow(
                    expense = expense,
                    onClick = { onExpenseClick(expense) }
                )
                if (index != month.expenses.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    onClick: () -> Unit
) {
    val categoryIcon = CategoryIconMap[expense.category.name] ?: Icons.Filled.AttachMoney
    val amountMajor = (expense.amountMinor / 100).toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = expense.date.dayOfMonth.toString().padStart(2, '0'),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .width(36.dp)
        )
        Icon(
            imageVector = categoryIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(20.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = amountMajor,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 64.dp)
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
            Expense(
                1,
                "Dejeuner client",
                LocalDate.of(2025, 10, 12),
                12000,
                Instant.now(),
                "voice",
                Category(1, "Divers")
            ),
            Expense(
                2,
                "Taxi",
                LocalDate.of(2025, 10, 10),
                4500,
                Instant.now(),
                "voice",
                Category(2, "Transport")
            )
        )
    )
    FineTheme {
        HistoryScreen(
            state = HistoryUiState(
                months = listOf(mockMonth),
                categories = listOf(
                    Category(1, "Divers"),
                    Category(2, "Transport")
                )
            ),
            onDeleteExpense = {},
            onUpdateExpense = { _, _, _, _, _ -> }
        )
    }
}

private val CategoryIconMap: Map<String, androidx.compose.ui.graphics.vector.ImageVector> = mapOf(
    "Nourriture" to Icons.Filled.LocalDining,
    "Carburant" to Icons.Filled.LocalGasStation,
    "Loyer" to Icons.Filled.Home,
    "SDK" to Icons.Filled.Favorite,
    "NFK" to Icons.Filled.AccountBalance,
    "Telephone" to Icons.Filled.LocalPhone,
    "Fibre" to Icons.Filled.Wifi,
    "Payage" to Icons.Filled.Paid,
    "Gardiennage" to Icons.Filled.Security,
    "Scolarite" to Icons.Filled.School,
    "Aide" to Icons.Filled.EmojiPeople,
    "Voiture" to Icons.Filled.DirectionsCar,
    "EE" to Icons.Filled.Bolt,
    "Gaz" to Icons.Filled.Whatshot,
    "Perso" to Icons.Filled.AttachMoney,
    "Divers" to Icons.Filled.AttachMoney
)

private val LOCALE_FR: Locale = Locale.forLanguageTag("fr-FR")
private val MONTH_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("LLLL yyyy", LOCALE_FR)
private val EDIT_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_FR)
