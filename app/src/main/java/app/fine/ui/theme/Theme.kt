package app.fine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BurntSienna = Color(0xFFE76F51)
private val Charcoal = Color(0xFF264653)
private val PersianGreen = Color(0xFF2A9D8F)
private val Saffron = Color(0xFFE9C46A)
private val SandyBrown = Color(0xFFF4A261)

private val LightColors = lightColorScheme(
    primary = BurntSienna,
    onPrimary = Color.White,
    primaryContainer = SandyBrown,
    onPrimaryContainer = Charcoal,
    secondary = PersianGreen,
    onSecondary = Color.White,
    secondaryContainer = Saffron,
    onSecondaryContainer = Charcoal,
    tertiary = Charcoal,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = Saffron,
    onSurfaceVariant = Charcoal,
    outline = PersianGreen,
    error = Color(0xFFBA1B1B),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = PersianGreen,
    onPrimary = Charcoal,
    primaryContainer = Charcoal,
    onPrimaryContainer = Saffron,
    secondary = BurntSienna,
    onSecondary = Saffron,
    secondaryContainer = BurntSienna,
    onSecondaryContainer = Color.White,
    tertiary = BurntSienna,
    onTertiary = Saffron,
    background = Charcoal,
    onBackground = Color.White,
    surface = Charcoal,
    onSurface = Saffron,
    surfaceVariant = Charcoal,
    onSurfaceVariant = Saffron,
    outline = PersianGreen,
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
