package app.fine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = FinePalette.BurntSienna,
    onPrimary = Color.White,
    primaryContainer = FinePalette.SandyBrown,
    onPrimaryContainer = FinePalette.Charcoal,
    secondary = FinePalette.PersianGreen,
    onSecondary = Color.White,
    secondaryContainer = FinePalette.Saffron,
    onSecondaryContainer = FinePalette.Charcoal,
    tertiary = FinePalette.Charcoal,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = FinePalette.Charcoal,
    surface = Color.White,
    onSurface = FinePalette.Charcoal,
    surfaceVariant = FinePalette.Saffron,
    onSurfaceVariant = FinePalette.Charcoal,
    outline = FinePalette.PersianGreen,
    error = Color(0xFFBA1B1B),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = FinePalette.PersianGreen,
    onPrimary = FinePalette.Charcoal,
    primaryContainer = FinePalette.Charcoal,
    onPrimaryContainer = FinePalette.Saffron,
    secondary = FinePalette.BurntSienna,
    onSecondary = FinePalette.Saffron,
    secondaryContainer = FinePalette.BurntSienna,
    onSecondaryContainer = Color.White,
    tertiary = FinePalette.BurntSienna,
    onTertiary = FinePalette.Saffron,
    background = FinePalette.Charcoal,
    onBackground = Color.White,
    surface = FinePalette.Charcoal,
    onSurface = FinePalette.Saffron,
    surfaceVariant = FinePalette.Charcoal,
    onSurfaceVariant = FinePalette.Saffron,
    outline = FinePalette.PersianGreen,
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
