@file:Suppress("MagicNumber")

package com.aprovecha.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Cards: 24dp · Botones primarios: 28dp (pill) · Inputs: 16dp ·
// Bottom nav: 32dp · Badges/chips: usar RoundedCornerShape(50) directamente.
val AprovechaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
