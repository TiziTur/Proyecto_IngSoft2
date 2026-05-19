package com.aprovecha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AprovechaColorScheme = lightColorScheme(
    primary = Verde80,
    onPrimary = Surface,
    primaryContainer = VerdeContainer,
    onPrimaryContainer = Verde80,
    secondary = Naranja80,
    onSecondary = Surface,
    secondaryContainer = NaranjaContainer,
    onSecondaryContainer = Naranja80,
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
        content = content
    )
}
