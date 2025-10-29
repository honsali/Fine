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
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private data class QuickCategoryDefinition(
    val name: String,
    val displayName: String,
    val icon: ImageVector
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
