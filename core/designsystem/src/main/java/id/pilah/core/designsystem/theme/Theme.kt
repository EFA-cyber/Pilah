package id.pilah.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PilahGreen,
    onPrimary = PilahNeutral99,
    primaryContainer = PilahGreenLight,
    onPrimaryContainer = PilahGreenDark,
    secondary = PilahAmber,
    secondaryContainer = PilahAmberLight,
    background = PilahNeutral99,
    surface = PilahNeutral99,
    onBackground = PilahNeutral10,
    onSurface = PilahNeutral10,
)

private val DarkColors = darkColorScheme(
    primary = PilahGreenLight,
    onPrimary = PilahGreenDark,
    primaryContainer = PilahGreenDark,
    onPrimaryContainer = PilahGreenLight,
    secondary = PilahAmberLight,
    secondaryContainer = PilahAmber,
    background = PilahNeutral10,
    surface = PilahNeutral20,
    onBackground = PilahNeutral90,
    onSurface = PilahNeutral90,
)

@Composable
fun PilahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PilahTypography,
        content = content,
    )
}
