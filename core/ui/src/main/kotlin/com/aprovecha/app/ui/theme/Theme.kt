package com.aprovecha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AprovechaColorScheme = lightColorScheme(
    primary = Bosque70,
    onPrimary = Surface,
    primaryContainer = BosqueContainer,
    onPrimaryContainer = Bosque90,
    secondary = Lima,
    onSecondary = OnLima,
    secondaryContainer = LimaContainer,
    onSecondaryContainer = OnLima,
    tertiary = Coral,
    onTertiary = Surface,
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = Coral,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    outline = Divider
)

@Composable
fun AprovechaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AprovechaColorScheme,
        typography = AprovechaTypography,
        shapes = AprovechaShapes,
        content = content
    )
}
