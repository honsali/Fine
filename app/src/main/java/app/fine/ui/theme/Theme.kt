package app.fine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FieryPrimary = Color(0xFFC1121F)
private val FieryBurgundy = Color(0xFF780000)
private val FierySand = Color(0xFFFDF0D5)
private val OceanDeep = Color(0xFF003049)
private val OceanSoft = Color(0xFF669BBC)

private val LightColors = lightColorScheme(
    primary = FieryPrimary,
    onPrimary = Color.White,
    primaryContainer = FieryBurgundy,
    onPrimaryContainer = FierySand,
    secondary = OceanSoft,
    onSecondary = Color.White,
    secondaryContainer = OceanSoft,
    onSecondaryContainer = OceanDeep,
    tertiary = OceanDeep,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = OceanDeep,
    surface = Color.White,
    onSurface = OceanDeep,
    surfaceVariant = FierySand,
    onSurfaceVariant = OceanDeep,
    outline = OceanSoft,
    error = Color(0xFFBA1B1B),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = OceanSoft,
    onPrimary = OceanDeep,
    primaryContainer = OceanDeep,
    onPrimaryContainer = FierySand,
    secondary = FieryPrimary,
    onSecondary = FierySand,
    secondaryContainer = FieryBurgundy,
    onSecondaryContainer = FierySand,
    tertiary = FieryPrimary,
    onTertiary = FierySand,
    background = OceanDeep,
    onBackground = Color.White,
    surface = OceanDeep,
    onSurface = FierySand,
    surfaceVariant = FieryBurgundy,
    onSurfaceVariant = FierySand,
    outline = OceanSoft,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val AppTypography = Typography()

@Composable
fun FineTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
