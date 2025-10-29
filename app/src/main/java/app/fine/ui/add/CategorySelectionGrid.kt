package app.fine.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.fine.ui.theme.FinePalette

@Composable
internal fun CategorySelectionGrid(
    state: AddExpenseUiState,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val definitions = remember { QuickCategoryDefinition.Definitions }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(definitions) { definition ->
            val option = state.categories.firstOrNull {
                it.name.equals(definition.name, ignoreCase = true)
            }
            CategoryTile(
                label = definition.displayName,
                icon = definition.icon,
                color = definition.accentColor,
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
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val displayColor = if (enabled) color else color.copy(alpha = 0.3f)

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = displayColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                tint = displayColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = displayColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private data class QuickCategoryDefinition(
    val name: String,
    val displayName: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    companion object {
        val Definitions: List<QuickCategoryDefinition> = listOf(
            QuickCategoryDefinition("Nourriture", "Nourriture", Icons.Filled.LocalDining, FinePalette.PersianGreen),
            QuickCategoryDefinition("Carburant", "Carburant", Icons.Filled.LocalGasStation, FinePalette.BurntSienna),
            QuickCategoryDefinition("Loyer", "Loyer", Icons.Filled.Home, FinePalette.BurntSienna),
            QuickCategoryDefinition("SDK", "SDK", Icons.Filled.Favorite, FinePalette.BurntSienna),
            QuickCategoryDefinition("NFK", "NFK", Icons.Filled.AccountBalance, FinePalette.BurntSienna),
            QuickCategoryDefinition("Telephone", "Telephone", Icons.Filled.LocalPhone, FinePalette.BurntSienna),
            QuickCategoryDefinition("Fibre", "Fibre", Icons.Filled.Wifi, FinePalette.SandyBrown),
            QuickCategoryDefinition("Payage", "Payage", Icons.Filled.Paid, FinePalette.BurntSienna),
            QuickCategoryDefinition("Gardiennage", "Gardiennage", Icons.Filled.Security, FinePalette.BurntSienna),
            QuickCategoryDefinition("Scolarite", "Scolarité", Icons.Filled.School, FinePalette.BurntSienna),
            QuickCategoryDefinition("Aide", "Aide", Icons.Filled.EmojiPeople, FinePalette.BurntSienna),
            QuickCategoryDefinition("Voiture", "Voiture", Icons.Filled.DirectionsCar, FinePalette.BurntSienna),
            QuickCategoryDefinition("EE", "EE", Icons.Filled.Bolt, FinePalette.Saffron),
            QuickCategoryDefinition("Gaz", "Gaz", Icons.Filled.Whatshot, FinePalette.Charcoal),
            QuickCategoryDefinition("Perso", "Perso", Icons.Filled.AttachMoney, FinePalette.BurntSienna)
        )
    }
}
