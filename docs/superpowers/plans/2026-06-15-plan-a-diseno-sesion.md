# Plan A — Sistema de diseño "Eco-moderno vibrante" + Sesión real + Fix selector de cantidad

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reemplazar la paleta/shapes actuales por la nueva identidad "Eco-moderno vibrante", implementar una arquitectura de sesión real basada en DataStore (sustituyendo los IDs hardcodeados `DEFAULT_USER_ID`/`DEFAULT_COMMERCE_ID`), y corregir el selector de cantidad engañoso en `PackDetailScreen`. Es el Plan A de 3 (Base), del cual dependen los Planes B (Perfil/Bottom Nav/Favoritos) y C (Notificaciones/Reservas a confirmar/Stats).

**Architecture:**
- Los archivos de tema (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`) se mueven de `app/.../ui/theme/` a `core/ui/.../ui/theme/` (mismo paquete `com.aprovecha.app.ui.theme`) para que los módulos `feature/*` puedan importar tokens de color/shape directamente — hoy `core/ui` ya es dependencia de `app`, `feature/auth`, `feature/products` y `feature/reservations`, pero los archivos de tema viven en `:app`, que ningún `feature/*` puede importar.
- `SessionManager` (nuevo, en `core/data`) persiste la sesión activa (`userId`, `email`, `name`, `role`) usando DataStore Preferences. `AuthRepositoryImpl` lo usa para implementar `login`/`register`/`logout`/`getCurrentUser` (hoy `getCurrentUser` siempre `null` y `logout` es no-op).
- `ProductsViewModel` y `ReservationsViewModel` reciben `AuthRepository` por Hilt y obtienen `userId`/`commerceId` desde `getCurrentUser()` en lugar de las constantes `DEFAULT_USER_ID`/`DEFAULT_COMMERCE_ID` (para `COMMERCE`, `commerceId == userId`, igual que el hardcode actual).
- `PackDetailScreen` deja de mostrar un selector +/- de cantidad (el modelo de `Reservation` no soporta cantidades parciales) y pasa a mostrar un texto informativo + botón "Reservar pack completo por $X".
- **Alcance del retema (sección 1 del spec):** este plan retema el sistema de diseño base (`core/ui/.../theme/*`) y las pantallas `PackDetailScreen`, `LoginScreen` y `RegisterScreen`. `HomeConsumerScreen`, `HomeCommerceScreen`, `MyReservationsScreen` y `PublishPackScreen` quedan con sus colores actuales y se retematizan en los Planes B/C, donde de todos modos se modifican para las nuevas funcionalidades (Perfil/Bottom Nav/Favoritos/Notificaciones/Stats).

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Hilt, Room, DataStore Preferences (`androidx.datastore:datastore-preferences`), MockK, JUnit4, kotlinx-coroutines-test.

---

## Task 1: Mover el sistema de tema a `core/ui` y aplicar la nueva paleta "Eco-moderno vibrante"

**Files:**
- Create: `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Color.kt`
- Create: `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Type.kt`
- Create: `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Shape.kt`
- Create: `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Theme.kt`
- Delete: `app/src/main/java/com/aprovecha/app/ui/theme/Color.kt`
- Delete: `app/src/main/java/com/aprovecha/app/ui/theme/Type.kt`
- Delete: `app/src/main/java/com/aprovecha/app/ui/theme/Theme.kt`

`MainActivity.kt` no necesita cambios: el paquete `com.aprovecha.app.ui.theme` no cambia, solo su ubicación física de módulo, y `:app` ya depende de `:core:ui`.

- [ ] **Step 1: Crear la nueva paleta de colores**

Crear `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Color.kt`:

```kotlin
@file:Suppress("MagicNumber")

package com.aprovecha.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Paleta principal — Bosque profundo ─────────────────────────────────────
val Bosque90 = Color(0xFF1B5E20)
val Bosque70 = Color(0xFF2E7D32)
val Bosque50 = Color(0xFF4CAF50)
val BosqueContainer = Color(0xFFD7F0DA)

// ── Acento vibrante — Lima eléctrico ────────────────────────────────────────
val Lima = Color(0xFFAEEA00)
val LimaContainer = Color(0xFFF3FFB0)
val OnLima = Color(0xFF1A1C19)

// ── Urgencia / descuento — Coral ─────────────────────────────────────────────
val Coral = Color(0xFFFF5252)
val CoralContainer = Color(0xFFFFE0E0)

// ── Neutros ───────────────────────────────────────────────────────────────────
val Background = Color(0xFFF1F8F4)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1A1C19)
val OnSurfaceVariant = Color(0xFF5F6F60)
val Divider = Color(0xFFDDEDE1)
val Error = Color(0xFFB3261E)
```

- [ ] **Step 2: Mover la tipografía sin cambios**

Crear `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Type.kt` con el mismo contenido que tenía `app/src/main/java/com/aprovecha/app/ui/theme/Type.kt`:

```kotlin
package com.aprovecha.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AprovechaTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
```

- [ ] **Step 3: Crear los nuevos shapes compartidos**

Crear `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Shape.kt`:

```kotlin
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
```

- [ ] **Step 4: Actualizar el `Theme.kt` con la nueva paleta y shapes**

Crear `core/ui/src/main/kotlin/com/aprovecha/app/ui/theme/Theme.kt`:

```kotlin
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
```

- [ ] **Step 5: Borrar los archivos de tema viejos en `:app`**

```bash
git rm app/src/main/java/com/aprovecha/app/ui/theme/Color.kt app/src/main/java/com/aprovecha/app/ui/theme/Type.kt app/src/main/java/com/aprovecha/app/ui/theme/Theme.kt
```

- [ ] **Step 6: Compilar para verificar que `:app` resuelve `AprovechaTheme` desde `:core:ui`**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (MainActivity.kt sigue importando `com.aprovecha.app.ui.theme.AprovechaTheme`, ahora resuelto desde `:core:ui`).

- [ ] **Step 7: Commit**

```bash
git add core/ui/src/main/kotlin/com/aprovecha/app/ui/theme
git commit -m "feat: mover sistema de tema a core/ui y aplicar paleta Eco-moderno vibrante"
```

---

## Task 2: Retemar `PackDetailScreen` y quitar el selector de cantidad (bug fix)

**Files:**
- Modify: `feature/products/src/main/kotlin/com/aprovecha/app/feature/products/ui/PackDetailScreen.kt`

El selector +/- de cantidad es engañoso: `Reservation` no tiene campo `quantity`, cada reserva cubre el pack completo. Se reemplaza por un texto informativo y el botón pasa a "Reservar pack completo por $X". De paso, se retema toda la pantalla con los nuevos tokens de `MaterialTheme.colorScheme`/`shapes` (Task 1).

- [ ] **Step 1: Reemplazar el archivo completo**

Reemplazar el contenido completo de `feature/products/src/main/kotlin/com/aprovecha/app/feature/products/ui/PackDetailScreen.kt` por:

```kotlin
@file:Suppress("LongMethod", "WildcardImport", "MaxLineLength", "MagicNumber")

package com.aprovecha.app.feature.products.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.ui.theme.Bosque50

// @REQ-F04: Detalle de pack con botón "Reservar pack completo"

private const val PICKUP_WINDOW_START = "19:00"
private const val PICKUP_WINDOW_END = "21:00"

@Composable
fun PackDetailScreen(
    packId: Long,
    onBack: () -> Unit,
    onReserveSuccess: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val pack by viewModel.selectedPack.collectAsState()
    val reserveState by viewModel.reserveState.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var pickupTermsAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(packId) { viewModel.loadPackDetail(packId) }

    LaunchedEffect(reserveState) {
        when (reserveState) {
            is ReserveUiState.Success -> {
                onReserveSuccess()
                viewModel.resetReserveState()
            }
            is ReserveUiState.Error -> showErrorDialog = true
            else -> {}
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false; viewModel.resetReserveState() },
            title = { Text("No se pudo reservar") },
            text = { Text((reserveState as? ReserveUiState.Error)?.message ?: "Error desconocido") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false; viewModel.resetReserveState() }) {
                    Text("OK")
                }
            }
        )
    }

    if (pack == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val foodPack = pack!!
    val discountPct = ((1 - foodPack.discountPrice / foodPack.originalPrice) * 100).toInt()
    val saving = foodPack.originalPrice - foodPack.discountPrice

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Hero con gradiente verde ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, Bosque50))
                )
        ) {
            // Botones overlay
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorito", tint = Color.White)
                }
            }

            // Badge descuento
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
            ) {
                Text(
                    "-$discountPct%",
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // ── Tarjeta de detalle ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(foodPack.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            // Info del comercio
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("Comercio #${foodPack.commerceId}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("~2.3 km", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Descripción
            Text(
                text = foodPack.description.ifBlank { "Pack sorpresa de alimentos frescos con descuento. ¡Rescatá comida y ahorrá!" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Precios
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "$${foodPack.originalPrice.toInt()}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            "$${foodPack.discountPrice.toInt()}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            "Ahorrás $${saving.toInt()}",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bug fix: se quita el selector +/- de cantidad (Reservation no
            // admite cantidades parciales, la reserva siempre es del pack completo).
            Text(
                "Este pack incluye ${foodPack.quantity} unidades — se reserva completo",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PickupTermsNotice()
            PickupTermsCheckbox(
                checked = pickupTermsAccepted,
                onCheckedChange = { pickupTermsAccepted = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón reservar
            Button(
                onClick = { viewModel.reservePack(packId = foodPack.id) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = pickupTermsAccepted && reserveState !is ReserveUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (reserveState is ReserveUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        "Reservar pack completo por $${foodPack.discountPrice.toInt()} 🛒",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PickupTermsNotice() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Al rescatar esta comida, te comprometés a retirarla hoy entre " +
                    "las $PICKUP_WINDOW_START y las $PICKUP_WINDOW_END hs. " +
                    "No hay devoluciones por llegada tardía.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PickupTermsCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Text("Entiendo y acepto el horario de retiro", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
```

- [ ] **Step 2: Compilar para verificar que el módulo `feature:products` resuelve `Bosque50` desde `core:ui`**

Run: `./gradlew :feature:products:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/products/src/main/kotlin/com/aprovecha/app/feature/products/ui/PackDetailScreen.kt
git commit -m "fix: quitar selector de cantidad enganoso y retemar PackDetailScreen"
```

---

## Task 3: Retemar `LoginScreen` con los nuevos tokens de `MaterialTheme`

**Files:**
- Modify: `feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/LoginScreen.kt`

`LoginScreenTokens` deja de tener colores hardcodeados (`Color(0xFF...)`); las dimensiones de shape pasan a `MaterialTheme.shapes.*`.

- [ ] **Step 1: Reemplazar el archivo completo**

Reemplazar el contenido completo de `feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/LoginScreen.kt` por:

```kotlin
@file:Suppress("MagicNumber")

package com.aprovecha.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.ui.theme.Bosque50

// @REQ-F01: Pantalla de login de usuario registrado

private object LoginScreenTokens {
    val HeaderHeight = 220.dp
    val ContainerPadding = 24.dp
    val PrimaryButtonHeight = 52.dp
    val LoadingSize = 20.dp
    const val LogoSizeSp = 48
    const val ActionTextSizeSp = 16
    const val DividerTextSizeSp = 13
}

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess((uiState as AuthUiState.Success).role)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LoginHeader()

        LoginForm(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            uiState = uiState,
            onLoginClick = { viewModel.login(email, password) },
            onGoToRegister = onGoToRegister
        )
    }
}

@Composable
private fun LoginHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LoginScreenTokens.HeaderHeight)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, Bosque50)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🌱", fontSize = LoginScreenTokens.LogoSizeSp.sp)
            Text(
                text = "Aprovecha!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rescatá comida, ahorrá dinero",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
@Suppress("LongMethod", "CognitiveComplexMethod", "LongParameterList")
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    uiState: AuthUiState,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(LoginScreenTokens.ContainerPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                    )
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.small
        )

        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(LoginScreenTokens.PrimaryButtonHeight),
            shape = MaterialTheme.shapes.large,
            enabled = uiState !is AuthUiState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(LoginScreenTokens.LoadingSize)
                )
            } else {
                Text(
                    text = "Ingresar",
                    fontWeight = FontWeight.Bold,
                    fontSize = LoginScreenTokens.ActionTextSizeSp.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  ¿No tenés cuenta?  ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = LoginScreenTokens.DividerTextSizeSp.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        OutlinedButton(
            onClick = onGoToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(LoginScreenTokens.PrimaryButtonHeight),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Crear cuenta",
                fontWeight = FontWeight.SemiBold,
                fontSize = LoginScreenTokens.ActionTextSizeSp.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/LoginScreen.kt
git commit -m "feat: retemar LoginScreen con paleta Eco-moderno vibrante"
```

---

## Task 4: Retemar `RegisterScreen` con los nuevos tokens de `MaterialTheme`

**Files:**
- Modify: `feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/RegisterScreen.kt`

Mismo criterio que Task 3. El color "Comercio" (antes naranja `#E65100`) pasa a `MaterialTheme.colorScheme.tertiary` (Coral), y "Consumidor" a `MaterialTheme.colorScheme.primary` (Bosque).

- [ ] **Step 1: Reemplazar el archivo completo**

Reemplazar el contenido completo de `feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/RegisterScreen.kt` por:

```kotlin
@file:Suppress("MagicNumber")

package com.aprovecha.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.domain.model.UserRole

// @REQ-F01: Pantalla de registro con selección de rol COMMERCE / CONSUMER

private object RegisterScreenTokens {
    val ContentPadding = 24.dp
    val VerticalSpacing = 16.dp
    val RoleSpacing = 12.dp
    val ButtonHeight = 52.dp
    val RoleCardBorder = 2.dp
    val RoleCardInnerPadding = 16.dp
    val RoleCardIconSize = 36.dp
    val LoadingSize = 20.dp
    const val RoleCardAlpha = 0.08f
    const val RoleTitleSizeSp = 14
    const val RoleSubtitleSizeSp = 11
    const val ButtonTextSizeSp = 16
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (role: String) -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess((uiState as AuthUiState.Success).role)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = onGoToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        RegisterForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            uiState = uiState,
            name = name,
            onNameChange = { name = it },
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            selectedRole = selectedRole,
            onRoleSelected = { selectedRole = it },
            onRegisterClick = {
                if (selectedRole != null) {
                    viewModel.register(email, password, name, selectedRole!!)
                }
            }
        )
    }
}

@Composable
@Suppress("LongMethod", "LongParameterList")
private fun RegisterForm(
    modifier: Modifier,
    uiState: AuthUiState,
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(RegisterScreenTokens.ContentPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(RegisterScreenTokens.VerticalSpacing)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre completo / Nombre del local") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña (mín. 6 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = MaterialTheme.shapes.small
        )

        Text(
            text = "¿Cómo vas a usar Aprovecha!?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RegisterScreenTokens.RoleSpacing)
        ) {
            RoleCard(
                option = RoleOption(
                    icon = Icons.Default.Person,
                    title = "Consumidor",
                    subtitle = "Compro packs con descuento",
                    selectedColor = MaterialTheme.colorScheme.primary,
                    onClick = { onRoleSelected(UserRole.CONSUMER) }
                ),
                selected = selectedRole == UserRole.CONSUMER,
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                option = RoleOption(
                    icon = Icons.Default.Storefront,
                    title = "Comercio",
                    subtitle = "Publico mis excedentes",
                    selectedColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { onRoleSelected(UserRole.COMMERCE) }
                ),
                selected = selectedRole == UserRole.COMMERCE,
                modifier = Modifier.weight(1f)
            )
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(RegisterScreenTokens.ButtonHeight),
            shape = MaterialTheme.shapes.large,
            enabled = uiState !is AuthUiState.Loading && selectedRole != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(RegisterScreenTokens.LoadingSize)
                )
            } else {
                Text(
                    text = "Registrarme",
                    fontWeight = FontWeight.Bold,
                    fontSize = RegisterScreenTokens.ButtonTextSizeSp.sp
                )
            }
        }
    }
}

private data class RoleOption(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val selectedColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun RoleCard(
    option: RoleOption,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) option.selectedColor else MaterialTheme.colorScheme.outline
    val backgroundColor = if (selected) {
        option.selectedColor.copy(alpha = RegisterScreenTokens.RoleCardAlpha)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                RegisterScreenTokens.RoleCardBorder,
                borderColor,
                MaterialTheme.shapes.small
            )
            .background(backgroundColor)
            .clickable(onClick = option.onClick)
            .padding(RegisterScreenTokens.RoleCardInnerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            tint = if (selected) option.selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(RegisterScreenTokens.RoleCardIconSize)
        )
        Text(
            text = option.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = RegisterScreenTokens.RoleTitleSizeSp.sp,
            color = if (selected) option.selectedColor else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = option.subtitle,
            fontSize = RegisterScreenTokens.RoleSubtitleSizeSp.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

- [ ] **Step 2: Compilar el módulo `feature:auth`**

Run: `./gradlew :feature:auth:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/RegisterScreen.kt
git commit -m "feat: retemar RegisterScreen con paleta Eco-moderno vibrante"
```

---

## Task 5: Agregar la dependencia de DataStore Preferences

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core/data/build.gradle.kts`

- [ ] **Step 1: Agregar versión y alias de librería**

En `gradle/libs.versions.toml`, en la sección `[versions]`, agregar (después de la línea `jacoco = "0.8.12"`):

```toml
datastorePreferences = "1.1.1"
```

En la sección `[libraries]`, agregar (después de la línea de `mockk-android`):

```toml
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
```

- [ ] **Step 2: Agregar la dependencia al módulo `core:data`**

En `core/data/build.gradle.kts`, dentro del bloque `dependencies { ... }`, agregar después de `implementation(libs.kotlinx.coroutines.android)`:

```kotlin
    // DataStore (sesión)
    implementation(libs.androidx.datastore.preferences)
```

- [ ] **Step 3: Sincronizar y compilar**

Run: `./gradlew :core:data:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml core/data/build.gradle.kts
git commit -m "build: agregar dependencia datastore-preferences para sesion"
```

---

## Task 6: Crear `SessionManager` (DataStore) con tests

**Files:**
- Create: `core/data/src/main/kotlin/com/aprovecha/app/data/local/datastore/SessionManager.kt`
- Test: `core/data/src/test/kotlin/com/aprovecha/app/data/local/datastore/SessionManagerTest.kt`

- [ ] **Step 1: Escribir los tests (que fallarán por falta de la clase)**

Crear `core/data/src/test/kotlin/com/aprovecha/app/data/local/datastore/SessionManagerTest.kt`:

```kotlin
package com.aprovecha.app.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// @REQ-F01: Tests de SessionManager (persistencia de sesion con DataStore)

class SessionManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun buildSessionManager(): SessionManager {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempFolder.root, "session_test.preferences_pb") }
        )
        return SessionManager(dataStore)
    }

    @Test
    fun `Given no session When getCurrentUser called Then returns null`() = runTest {
        val sessionManager = buildSessionManager()

        assertNull(sessionManager.getCurrentUser())
    }

    @Test
    fun `Given saved session When getCurrentUser called Then returns same user`() = runTest {
        val sessionManager = buildSessionManager()
        val user = User(id = 7L, email = "user@test.com", name = "Test User", role = UserRole.COMMERCE)

        sessionManager.saveSession(user)

        assertEquals(user, sessionManager.getCurrentUser())
    }

    @Test
    fun `Given saved session When clearSession called Then getCurrentUser returns null`() = runTest {
        val sessionManager = buildSessionManager()
        sessionManager.saveSession(User(id = 1L, email = "a@test.com", name = "A", role = UserRole.CONSUMER))

        sessionManager.clearSession()

        assertNull(sessionManager.getCurrentUser())
    }

    @Test
    fun `Given consumer session saved When new session saved Then getCurrentUser returns new user`() = runTest {
        val sessionManager = buildSessionManager()
        sessionManager.saveSession(User(id = 1L, email = "a@test.com", name = "A", role = UserRole.CONSUMER))

        val newUser = User(id = 2L, email = "b@test.com", name = "B", role = UserRole.COMMERCE)
        sessionManager.saveSession(newUser)

        assertEquals(newUser, sessionManager.getCurrentUser())
    }
}
```

- [ ] **Step 2: Ejecutar los tests para verificar que fallan**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.aprovecha.app.data.local.datastore.SessionManagerTest"`
Expected: FAIL — `Unresolved reference: SessionManager`

- [ ] **Step 3: Implementar `SessionManager`**

Crear `core/data/src/main/kotlin/com/aprovecha/app/data/local/datastore/SessionManager.kt`:

```kotlin
package com.aprovecha.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// @REQ-F01: Persistencia de la sesion activa (login/registro/logout)

private object SessionKeys {
    val USER_ID = longPreferencesKey("session_user_id")
    val USER_EMAIL = stringPreferencesKey("session_user_email")
    val USER_NAME = stringPreferencesKey("session_user_name")
    val USER_ROLE = stringPreferencesKey("session_user_role")
}

class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val currentUser: Flow<User?> = dataStore.data.map { prefs ->
        val id = prefs[SessionKeys.USER_ID]
        val email = prefs[SessionKeys.USER_EMAIL]
        val name = prefs[SessionKeys.USER_NAME]
        val role = prefs[SessionKeys.USER_ROLE]
        if (id != null && email != null && name != null && role != null) {
            User(id = id, email = email, name = name, role = UserRole.valueOf(role))
        } else {
            null
        }
    }

    suspend fun getCurrentUser(): User? = currentUser.first()

    suspend fun saveSession(user: User) {
        dataStore.edit { prefs ->
            prefs[SessionKeys.USER_ID] = user.id
            prefs[SessionKeys.USER_EMAIL] = user.email
            prefs[SessionKeys.USER_NAME] = user.name
            prefs[SessionKeys.USER_ROLE] = user.role.name
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
```

- [ ] **Step 4: Ejecutar los tests para verificar que pasan**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.aprovecha.app.data.local.datastore.SessionManagerTest"`
Expected: BUILD SUCCESSFUL, 4 tests pasados

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/kotlin/com/aprovecha/app/data/local/datastore core/data/src/test/kotlin/com/aprovecha/app/data/local/datastore
git commit -m "feat: agregar SessionManager respaldado por DataStore Preferences"
```

---

## Task 7: Proveer el `DataStore<Preferences>` vía Hilt

**Files:**
- Create: `core/data/src/main/kotlin/com/aprovecha/app/data/di/SessionModule.kt`

`SessionManager` ya tiene `@Inject constructor`, por lo que Hilt lo crea automáticamente una vez que `DataStore<Preferences>` está disponible como dependencia.

- [ ] **Step 1: Crear el módulo Hilt**

Crear `core/data/src/main/kotlin/com/aprovecha/app/data/di/SessionModule.kt`:

```kotlin
package com.aprovecha.app.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    private const val SESSION_PREFERENCES_NAME = "aprovecha_session"

    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(SESSION_PREFERENCES_NAME) }
        )
}
```

- [ ] **Step 2: Compilar**

Run: `./gradlew :core:data:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/data/src/main/kotlin/com/aprovecha/app/data/di/SessionModule.kt
git commit -m "feat: proveer DataStore<Preferences> para SessionManager via Hilt"
```

---

## Task 8: Conectar `AuthRepositoryImpl` con `SessionManager`

**Files:**
- Modify: `core/data/src/main/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImpl.kt`
- Modify: `core/data/src/test/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImplTest.kt`

`login`/`register` exitosos guardan la sesión; `logout` la limpia; `getCurrentUser` la lee. Esto resuelve `getCurrentUser()` (hoy siempre `null`) y `logout()` (hoy no-op).

- [ ] **Step 1: Actualizar el test existente para inyectar `SessionManager` mockeado**

Reemplazar el contenido completo de `core/data/src/test/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImplTest.kt` por:

```kotlin
package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.datastore.SessionManager
import com.aprovecha.app.data.local.entity.UserEntity
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// @REQ-F01: Tests de AuthRepositoryImpl

class AuthRepositoryImplTest {

    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        userDao = mockk()
        sessionManager = mockk(relaxed = true)
        repository = AuthRepositoryImpl(userDao, sessionManager)
    }

    private fun buildUserEntity(
        id: Long = 1L,
        email: String = "test@test.com",
        password: String = "pass123",
        name: String = "Test User",
        role: String = "CONSUMER"
    ) = UserEntity(
        id = id,
        email = email,
        passwordHash = password.hashCode().toString(),
        nombre = name,
        role = role
    )

    // ── register ─────────────────────────────────────────────────────────────

    /**
     * Given: email no existe en BD
     * When: se registra nuevo usuario CONSUMER
     * Then: Result.Success con user correcto
     */
    @Test
    fun `Given new email When register consumer Then returns Success with user`() = runTest {
        val email = "nuevo@test.com"
        coEvery { userDao.getUserByEmail(email) } returns null
        coEvery { userDao.insertUser(any()) } returns 5L

        val result = repository.register(email, "pass123", "Nuevo Usuario", UserRole.CONSUMER)

        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(email, user.email)
        assertEquals("Nuevo Usuario", user.name)
        assertEquals(UserRole.CONSUMER, user.role)
        assertEquals(5L, user.id)
    }

    /**
     * Given: email no existe en BD
     * When: se registra nuevo comercio
     * Then: Result.Success con rol COMMERCE
     */
    @Test
    fun `Given new email When register commerce Then returns Success with COMMERCE role`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } returns 10L

        val result = repository.register("comercio@test.com", "pass", "Mi Local", UserRole.COMMERCE)

        assertTrue(result is Result.Success)
        assertEquals(UserRole.COMMERCE, (result as Result.Success).data.role)
    }

    /**
     * Given: email ya registrado
     * When: se intenta registrar con el mismo email
     * Then: Result.Error con mensaje de email duplicado
     */
    @Test
    fun `Given existing email When register called Then returns Error with duplicate message`() = runTest {
        val existingUser = buildUserEntity(email = "existente@test.com")
        coEvery { userDao.getUserByEmail("existente@test.com") } returns existingUser

        val result = repository.register("existente@test.com", "pass", "User", UserRole.CONSUMER)

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error.message!!.contains("registrado"))
    }

    /**
     * Given: DAO lanza excepción
     * When: register falla por error de DB
     * Then: Result.Error con la excepción
     */
    @Test
    fun `Given DAO throws When register called Then returns Result Error`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } throws RuntimeException("DB constraint violation")

        val result = repository.register("test@test.com", "pass", "User", UserRole.CONSUMER)

        assertTrue(result is Result.Error)
    }

    /**
     * Given: registro exitoso
     * When: se completa el registro
     * Then: sessionManager guarda la sesion del nuevo usuario
     */
    @Test
    fun `Given successful register When called Then sessionManager saveSession invoked with new user`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } returns 5L

        val result = repository.register("nuevo@test.com", "pass123", "Nuevo Usuario", UserRole.CONSUMER)

        val user = (result as Result.Success).data
        coVerify(exactly = 1) { sessionManager.saveSession(user) }
    }

    // ── login ─────────────────────────────────────────────────────────────────

    /**
     * Given: usuario registrado con credenciales correctas
     * When: login con email y password correctos
     * Then: Result.Success con datos del usuario
     */
    @Test
    fun `Given valid credentials When login called Then returns Success with user`() = runTest {
        val password = "pass123"
        val entity = buildUserEntity(password = password)
        coEvery { userDao.getUserByEmail(entity.email) } returns entity

        val result = repository.login(entity.email, password)

        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(entity.email, user.email)
        assertEquals(UserRole.CONSUMER, user.role)
    }

    /**
     * Given: email no registrado
     * When: login con email inexistente
     * Then: Result.Error con mensaje "no encontrado"
     */
    @Test
    fun `Given unknown email When login called Then returns Error user not found`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null

        val result = repository.login("noexiste@test.com", "pass")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message!!.contains("encontrado"))
    }

    /**
     * Given: email correcto pero password incorrecto
     * When: login con password equivocado
     * Then: Result.Error con mensaje de contraseña incorrecta
     */
    @Test
    fun `Given wrong password When login called Then returns Error wrong password`() = runTest {
        val entity = buildUserEntity(password = "correctPass")
        coEvery { userDao.getUserByEmail(entity.email) } returns entity

        val result = repository.login(entity.email, "wrongPass")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message!!.contains("Contraseña"))
    }

    /**
     * Given: login exitoso
     * When: se completa el login
     * Then: sessionManager guarda la sesion del usuario autenticado
     */
    @Test
    fun `Given successful login When called Then sessionManager saveSession invoked with user`() = runTest {
        val entity = buildUserEntity(password = "pass123")
        coEvery { userDao.getUserByEmail(entity.email) } returns entity

        val result = repository.login(entity.email, "pass123")

        val user = (result as Result.Success).data
        coVerify(exactly = 1) { sessionManager.saveSession(user) }
    }

    /**
     * Given: registro exitoso
     * When: se verifica llamada al DAO
     * Then: insertUser fue llamado exactamente una vez
     */
    @Test
    fun `Given successful register When insert called Then DAO insertUser called once`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } returns 1L

        repository.register("test@test.com", "pass", "User", UserRole.CONSUMER)

        coVerify(exactly = 1) { userDao.insertUser(any()) }
    }

    // ── logout / getCurrentUser ─────────────────────────────────────────────

    /**
     * Given: hay una sesion guardada
     * When: se llama getCurrentUser
     * Then: devuelve el usuario que retorna sessionManager
     */
    @Test
    fun `Given saved session When getCurrentUser called Then returns sessionManager user`() = runTest {
        val user = User(id = 3L, email = "session@test.com", name = "Session User", role = UserRole.COMMERCE)
        coEvery { sessionManager.getCurrentUser() } returns user

        val result = repository.getCurrentUser()

        assertEquals(user, result)
    }

    /**
     * Given: no hay sesion guardada
     * When: se llama getCurrentUser
     * Then: devuelve null
     */
    @Test
    fun `Given no saved session When getCurrentUser called Then returns null`() = runTest {
        coEvery { sessionManager.getCurrentUser() } returns null

        assertNull(repository.getCurrentUser())
    }

    /**
     * Given: hay una sesion activa
     * When: se llama logout
     * Then: sessionManager limpia la sesion
     */
    @Test
    fun `Given active session When logout called Then sessionManager clearSession invoked`() = runTest {
        repository.logout()

        coVerify(exactly = 1) { sessionManager.clearSession() }
    }
}
```

- [ ] **Step 2: Ejecutar los tests para verificar que fallan (constructor con 2 parámetros)**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.aprovecha.app.data.repository.AuthRepositoryImplTest"`
Expected: FAIL — `AuthRepositoryImpl(userDao, sessionManager)` no compila (constructor actual tiene 1 parámetro)

- [ ] **Step 3: Actualizar `AuthRepositoryImpl`**

Reemplazar el contenido completo de `core/data/src/main/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImpl.kt` por:

```kotlin
package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.datastore.SessionManager
import com.aprovecha.app.data.local.entity.UserEntity
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import javax.inject.Inject

// @REQ-F01: Implementación del repositorio de autenticación

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    // @REQ-F01: Registro de nuevo usuario (COMMERCE o CONSUMER)
    override suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> = try {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            Result.Error(IllegalStateException("El email ya está registrado"))
        } else {
            val entity = UserEntity(
                email = email,
                passwordHash = password.hashCode().toString(),
                nombre = name,
                role = role.name
            )
            val id = userDao.insertUser(entity)
            val user = entity.copy(id = id).toDomain()
            sessionManager.saveSession(user)
            Result.Success(user)
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    // @REQ-F01: Login de usuario registrado
    override suspend fun login(email: String, password: String): Result<User> = try {
        val entity = userDao.getUserByEmail(email)
        if (entity == null) {
            Result.Error(IllegalArgumentException("Usuario no encontrado"))
        } else if (entity.passwordHash != password.hashCode().toString()) {
            Result.Error(IllegalArgumentException("Contraseña incorrecta"))
        } else {
            val user = entity.toDomain()
            sessionManager.saveSession(user)
            Result.Success(user)
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    // @REQ-F01: Sesion activa (DataStore)
    override suspend fun getCurrentUser(): User? = sessionManager.getCurrentUser()

    override suspend fun logout() = sessionManager.clearSession()
}

private fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    name = nombre,
    role = UserRole.valueOf(role)
)
```

- [ ] **Step 4: Ejecutar los tests para verificar que pasan**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.aprovecha.app.data.repository.AuthRepositoryImplTest"`
Expected: BUILD SUCCESSFUL, 13 tests pasados

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImpl.kt core/data/src/test/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImplTest.kt
git commit -m "feat: conectar AuthRepositoryImpl con SessionManager (login/register/logout/getCurrentUser reales)"
```

---

## Task 9: Agregar `logout()` a `AuthViewModel`

**Files:**
- Modify: `feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/AuthViewModel.kt`
- Modify: `feature/auth/src/test/kotlin/com/aprovecha/app/feature/auth/AuthViewModelTest.kt`

- [ ] **Step 1: Agregar el test de `logout`**

Agregar el import `coVerify` y el nuevo test al final de la clase, en `feature/auth/src/test/kotlin/com/aprovecha/app/feature/auth/AuthViewModelTest.kt`.

Cambiar el bloque de imports de:

```kotlin
import io.mockk.coEvery
import io.mockk.mockk
```

a:

```kotlin
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
```

Y agregar esta sección antes del cierre final `}` de la clase (después del test `` `Given Success state When resetState called Then uiState returns to Idle` ``):

```kotlin

    // ─── Logout ───────────────────────────────────────────────────────────────

    /**
     * // @REQ-F01: Logout limpia la sesión y devuelve el uiState a Idle
     */
    @Test
    fun `Given active session When logout called Then authRepository logout invoked and uiState is Idle`() = runTest {
        coEvery { authRepository.logout() } returns Unit

        viewModel.logout()
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.logout() }
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }
```

- [ ] **Step 2: Ejecutar el test para verificar que falla**

Run: `./gradlew :feature:auth:testDebugUnitTest --tests "com.aprovecha.app.feature.auth.AuthViewModelTest"`
Expected: FAIL — `viewModel.logout()` no existe (error de compilación)

- [ ] **Step 3: Agregar `logout()` a `AuthViewModel`**

En `feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/AuthViewModel.kt`, agregar el siguiente método después de `register(...)` y antes de `resetState()`:

```kotlin
    // @REQ-F01: Cierre de sesión
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }
```

- [ ] **Step 4: Ejecutar los tests para verificar que pasan**

Run: `./gradlew :feature:auth:testDebugUnitTest --tests "com.aprovecha.app.feature.auth.AuthViewModelTest"`
Expected: BUILD SUCCESSFUL, 11 tests pasados

- [ ] **Step 5: Commit**

```bash
git add feature/auth/src/main/kotlin/com/aprovecha/app/feature/auth/ui/AuthViewModel.kt feature/auth/src/test/kotlin/com/aprovecha/app/feature/auth/AuthViewModelTest.kt
git commit -m "feat: agregar logout a AuthViewModel"
```

---

## Task 10: `ProductsViewModel` — reemplazar `DEFAULT_USER_ID` por sesión real

**Files:**
- Modify: `feature/products/src/main/kotlin/com/aprovecha/app/feature/products/ui/ProductsViewModel.kt`
- Modify: `feature/products/src/test/kotlin/com/aprovecha/app/feature/products/ProductsViewModelTest.kt`

`reservePack(packId, userId = DEFAULT_USER_ID)` pasa a `reservePack(packId)`, obteniendo el `userId` desde `authRepository.getCurrentUser()`. Si no hay sesión, `reserveState` emite `Error`.

- [ ] **Step 1: Reescribir `ProductsViewModelTest.kt`**

Reemplazar el contenido completo de `feature/products/src/test/kotlin/com/aprovecha/app/feature/products/ProductsViewModelTest.kt` por:

```kotlin
package com.aprovecha.app.feature.products

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.feature.products.ui.PacksUiState
import com.aprovecha.app.feature.products.ui.ProductsViewModel
import com.aprovecha.app.feature.products.ui.ReserveUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

// @REQ-F03, @REQ-F04

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var packRepository: PackRepository
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ProductsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        packRepository = mockk()
        reservationRepository = mockk()
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildPack(id: Long = 1L) = FoodPack(
        id = id,
        commerceId = 1L,
        name = "Pack Test",
        description = "Descripción",
        originalPrice = 500.0,
        discountPrice = 200.0,
        quantity = 3
    )

    private fun buildReservation(id: Long = 1L) = Reservation(
        id = id,
        packId = 1L,
        userId = 1L,
        status = ReservationStatus.RESERVED,
        fechaReserva = LocalDateTime.now()
    )

    private fun buildUser(id: Long = 1L) = User(
        id = id,
        email = "user@test.com",
        name = "Test User",
        role = UserRole.CONSUMER
    )

    // ── loadNearbyPacks ──────────────────────────────────────────────────────

    /**
     * Given: packRepository retorna lista de packs
     * When: ViewModel se inicializa
     * Then: packsState emite Success con la lista
     */
    @Test
    fun `Given available packs When ViewModel initializes Then packsState emits Success`() = runTest {
        val packs = listOf(buildPack(1L), buildPack(2L))
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(packs)

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.packsState.value
        assertTrue(state is PacksUiState.Success)
        assertEquals(2, (state as PacksUiState.Success).packs.size)
    }

    /**
     * Given: packRepository lanza excepción
     * When: ViewModel intenta cargar packs
     * Then: packsState emite Error
     */
    @Test
    fun `Given repository throws When loading packs Then packsState emits Error`() = runTest {
        every {
            packRepository.getAvailablePacksNearby(any(), any(), any())
        } returns kotlinx.coroutines.flow.flow { throw RuntimeException("Network error") }

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.packsState.value
        assertTrue(state is PacksUiState.Error)
        assertEquals("Network error", (state as PacksUiState.Error).message)
    }

    // ── loadPackDetail ───────────────────────────────────────────────────────

    /**
     * Given: pack existe en repositorio
     * When: se llama loadPackDetail con packId válido
     * Then: selectedPack emite el pack correcto
     */
    @Test
    fun `Given valid packId When loadPackDetail called Then selectedPack is set`() = runTest {
        val pack = buildPack(42L)
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { packRepository.getPackById(42L) } returns Result.Success(pack)

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        viewModel.loadPackDetail(42L)
        advanceUntilIdle()

        assertEquals(pack, viewModel.selectedPack.value)
    }

    /**
     * Given: pack no existe
     * When: loadPackDetail retorna Error
     * Then: selectedPack permanece null
     */
    @Test
    fun `Given invalid packId When loadPackDetail fails Then selectedPack remains null`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { packRepository.getPackById(any()) } returns Result.Error(NoSuchElementException())

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        viewModel.loadPackDetail(999L)
        advanceUntilIdle()

        assertNull(viewModel.selectedPack.value)
    }

    // ── reservePack ──────────────────────────────────────────────────────────

    /**
     * Given: hay sesión activa y la reserva es exitosa
     * When: se llama reservePack
     * Then: reserveState emite Success
     */
    @Test
    fun `Given successful reservation When reservePack called Then reserveState emits Success`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        coEvery { reservationRepository.createReservation(1L, 1L) } returns Result.Success(buildReservation())

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        viewModel.reservePack(1L)
        advanceUntilIdle()

        assertTrue(viewModel.reserveState.value is ReserveUiState.Success)
    }

    /**
     * Given: pack agotado
     * When: se llama reservePack y falla
     * Then: reserveState emite Error con mensaje
     */
    @Test
    fun `Given pack unavailable When reservePack fails Then reserveState emits Error`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        coEvery {
            reservationRepository.createReservation(any(), any())
        } returns Result.Error(IllegalStateException("Pack ya reservado (REQ-NF01)"))

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        viewModel.reservePack(1L)
        advanceUntilIdle()

        val state = viewModel.reserveState.value
        assertTrue(state is ReserveUiState.Error)
        assertTrue((state as ReserveUiState.Error).message.contains("Pack ya reservado"))
    }

    /**
     * Given: no hay sesión activa
     * When: se llama reservePack
     * Then: reserveState emite Error sin llamar al repositorio de reservas
     */
    @Test
    fun `Given no session When reservePack called Then reserveState emits Error`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        viewModel.reservePack(1L)
        advanceUntilIdle()

        val state = viewModel.reserveState.value
        assertTrue(state is ReserveUiState.Error)
        assertTrue((state as ReserveUiState.Error).message.contains("Sesión"))
    }

    /**
     * Given: reserveState es Success
     * When: se llama resetReserveState
     * Then: reserveState vuelve a Idle
     */
    @Test
    fun `Given Success state When resetReserveState called Then state returns to Idle`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        coEvery { reservationRepository.createReservation(any(), any()) } returns Result.Success(buildReservation())

        viewModel = ProductsViewModel(packRepository, reservationRepository, authRepository)
        viewModel.reservePack(1L)
        advanceUntilIdle()

        viewModel.resetReserveState()

        assertTrue(viewModel.reserveState.value is ReserveUiState.Idle)
    }
}
```

- [ ] **Step 2: Ejecutar los tests para verificar que fallan**

Run: `./gradlew :feature:products:testDebugUnitTest --tests "com.aprovecha.app.feature.products.ProductsViewModelTest"`
Expected: FAIL — `ProductsViewModel(packRepository, reservationRepository, authRepository)` no compila (constructor actual tiene 2 parámetros)

- [ ] **Step 3: Actualizar `ProductsViewModel`**

Reemplazar el contenido completo de `feature/products/src/main/kotlin/com/aprovecha/app/feature/products/ui/ProductsViewModel.kt` por:

```kotlin
package com.aprovecha.app.feature.products.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// @REQ-F03: Lista de packs disponibles
// @REQ-F04: Reservar un pack

sealed class PacksUiState {
    data object Loading : PacksUiState()
    data class Success(val packs: List<FoodPack>) : PacksUiState()
    data class Error(val message: String) : PacksUiState()
}

sealed class ReserveUiState {
    data object Idle : ReserveUiState()
    data object Loading : ReserveUiState()
    data object Success : ReserveUiState()
    data class Error(val message: String) : ReserveUiState()
}

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val packRepository: PackRepository,
    private val reservationRepository: ReservationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private companion object {
        const val DEFAULT_LATITUDE = -31.4
        const val DEFAULT_LONGITUDE = -64.18
        const val DEFAULT_RADIUS_KM = 5.0
        const val SESSION_ERROR_MESSAGE = "Sesión no encontrada. Iniciá sesión nuevamente."
    }

    private val _packsState = MutableStateFlow<PacksUiState>(PacksUiState.Loading)
    val packsState: StateFlow<PacksUiState> = _packsState.asStateFlow()

    private val _selectedPack = MutableStateFlow<FoodPack?>(null)
    val selectedPack: StateFlow<FoodPack?> = _selectedPack.asStateFlow()

    private val _reserveState = MutableStateFlow<ReserveUiState>(ReserveUiState.Idle)
    val reserveState: StateFlow<ReserveUiState> = _reserveState.asStateFlow()

    init {
        loadNearbyPacks()
    }

    // @REQ-F03: Cargar packs cercanos (MVP: radio default 5km)
    fun loadNearbyPacks(
        lat: Double = DEFAULT_LATITUDE,
        lng: Double = DEFAULT_LONGITUDE,
        radioKm: Double = DEFAULT_RADIUS_KM
    ) {
        viewModelScope.launch {
            packRepository.getAvailablePacksNearby(lat, lng, radioKm)
                .catch { _packsState.value = PacksUiState.Error(it.message ?: "Error") }
                .collect { packs -> _packsState.value = PacksUiState.Success(packs) }
        }
    }

    fun loadPackDetail(packId: Long) {
        viewModelScope.launch {
            when (val result = packRepository.getPackById(packId)) {
                is Result.Success -> _selectedPack.value = result.data
                is Result.Error -> { /* mantener null */ }
                else -> {}
            }
        }
    }

    // @REQ-F04: Reservar pack usando el usuario de la sesión activa
    fun reservePack(packId: Long) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id
            if (userId == null) {
                _reserveState.value = ReserveUiState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            _reserveState.value = ReserveUiState.Loading
            _reserveState.value = when (val result = reservationRepository.createReservation(packId, userId)) {
                is Result.Success -> ReserveUiState.Success
                is Result.Error -> ReserveUiState.Error(result.exception.message ?: "Error al reservar")
                else -> ReserveUiState.Error("Error inesperado")
            }
        }
    }

    fun resetReserveState() { _reserveState.value = ReserveUiState.Idle }
}
```

- [ ] **Step 4: Ejecutar los tests para verificar que pasan**

Run: `./gradlew :feature:products:testDebugUnitTest --tests "com.aprovecha.app.feature.products.ProductsViewModelTest"`
Expected: BUILD SUCCESSFUL, 7 tests pasados

- [ ] **Step 5: Verificar que `PackDetailScreen.kt` compila**

`PackDetailScreen.kt` ya llama a `viewModel.reservePack(packId = foodPack.id)` (Task 2), compatible con la nueva firma `reservePack(packId: Long)`.

Run: `./gradlew :feature:products:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add feature/products/src/main/kotlin/com/aprovecha/app/feature/products/ui/ProductsViewModel.kt feature/products/src/test/kotlin/com/aprovecha/app/feature/products/ProductsViewModelTest.kt
git commit -m "feat: ProductsViewModel obtiene el userId de la sesion real en reservePack"
```

---

## Task 11: `ReservationsViewModel` — reemplazar `DEFAULT_USER_ID`/`DEFAULT_COMMERCE_ID` por sesión real

**Files:**
- Modify: `feature/reservations/src/main/kotlin/com/aprovecha/app/feature/reservations/ui/ReservationsViewModel.kt`
- Modify: `feature/reservations/src/test/kotlin/com/aprovecha/app/feature/reservations/ReservationsViewModelTest.kt`
- Modify: `feature/reservations/src/main/kotlin/com/aprovecha/app/feature/reservations/ui/PublishPackScreen.kt`

`loadUserReservations(userId = DEFAULT_USER_ID)` → `loadUserReservations()`, `loadCommercePacks(commerceId = DEFAULT_COMMERCE_ID)` → `loadCommercePacks()`, ambos obteniendo el id desde `authRepository.getCurrentUser()`. `publishPack(pack)` sobreescribe `commerceId` con el de la sesión. Si no hay sesión, cada uno emite su estado `Error` correspondiente.

- [ ] **Step 1: Reescribir `ReservationsViewModelTest.kt`**

Reemplazar el contenido completo de `feature/reservations/src/test/kotlin/com/aprovecha/app/feature/reservations/ReservationsViewModelTest.kt` por:

```kotlin
package com.aprovecha.app.feature.reservations

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import com.aprovecha.app.feature.reservations.ui.ActionState
import com.aprovecha.app.feature.reservations.ui.CommercePacksState
import com.aprovecha.app.feature.reservations.ui.ReservationsUiState
import com.aprovecha.app.feature.reservations.ui.ReservationsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

// @REQ-F02, @REQ-F05, @REQ-F06

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var packRepository: PackRepository
    private lateinit var publishPackUseCase: PublishPackUseCase
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ReservationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        reservationRepository = mockk()
        packRepository = mockk()
        publishPackUseCase = mockk()
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildReservation(id: Long = 1L, status: ReservationStatus = ReservationStatus.RESERVED) =
        Reservation(
            id = id,
            packId = 1L,
            userId = 1L,
            status = status,
            fechaReserva = LocalDateTime.now()
        )

    private fun buildPack(id: Long = 1L) = FoodPack(
        id = id,
        commerceId = 1L,
        name = "Pack Tarde",
        description = "Pan del día",
        originalPrice = 500.0,
        discountPrice = 200.0,
        quantity = 3
    )

    private fun buildUser(id: Long = 1L) = User(
        id = id,
        email = "user@test.com",
        name = "Test User",
        role = UserRole.CONSUMER
    )

    private fun createViewModel(): ReservationsViewModel {
        every { reservationRepository.getReservationsByUser(any()) } returns flowOf(emptyList())
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        return ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
    }

    // ── loadUserReservations ─────────────────────────────────────────────────

    /**
     * Given: usuario con sesión activa tiene 2 reservas
     * When: se llama loadUserReservations
     * Then: reservationsState emite Success con las 2 reservas
     */
    @Test
    fun `Given user has reservations When loadUserReservations called Then state emits Success`() = runTest {
        val reservations = listOf(buildReservation(1L), buildReservation(2L))
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { reservationRepository.getReservationsByUser(1L) } returns flowOf(reservations)
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.loadUserReservations()
        advanceUntilIdle()

        val state = viewModel.reservationsState.value
        assertTrue(state is ReservationsUiState.Success)
        assertEquals(2, (state as ReservationsUiState.Success).reservations.size)
    }

    /**
     * Given: repositorio lanza excepción
     * When: loadUserReservations falla
     * Then: reservationsState emite Error
     */
    @Test
    fun `Given repository throws When loadUserReservations fails Then state emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every {
            reservationRepository.getReservationsByUser(any())
        } returns kotlinx.coroutines.flow.flow { throw RuntimeException("DB error") }
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.loadUserReservations()
        advanceUntilIdle()

        val state = viewModel.reservationsState.value
        assertTrue(state is ReservationsUiState.Error)
    }

    /**
     * Given: no hay sesión activa
     * When: se llama loadUserReservations
     * Then: reservationsState emite Error sin llamar al repositorio
     */
    @Test
    fun `Given no session When loadUserReservations called Then state emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.loadUserReservations()
        advanceUntilIdle()

        val state = viewModel.reservationsState.value
        assertTrue(state is ReservationsUiState.Error)
        assertTrue((state as ReservationsUiState.Error).message.contains("Sesión"))
    }

    // ── cancelReservation ────────────────────────────────────────────────────

    /**
     * Given: reserva RESERVED existe
     * When: se cancela la reserva
     * Then: actionState emite Success
     */
    @Test
    fun `Given RESERVED reservation When cancelReservation called Then actionState emits Success`() = runTest {
        coEvery { reservationRepository.cancelReservation(1L) } returns
            Result.Success(buildReservation(1L, ReservationStatus.CANCELLED))

        viewModel = createViewModel()
        viewModel.cancelReservation(1L)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is ActionState.Success)
    }

    /**
     * Given: reserva ya WITHDRAWN
     * When: se intenta cancelar
     * Then: actionState emite Error
     */
    @Test
    fun `Given WITHDRAWN reservation When cancelReservation called Then actionState emits Error`() = runTest {
        coEvery {
            reservationRepository.cancelReservation(1L)
        } returns Result.Error(IllegalStateException("No se puede cancelar una reserva ya retirada"))

        viewModel = createViewModel()
        viewModel.cancelReservation(1L)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("cancelar"))
    }

    // ── markAsWithdrawn ──────────────────────────────────────────────────────

    /**
     * Given: reserva RESERVED
     * When: se marca como retirada
     * Then: actionState emite Success
     */
    @Test
    fun `Given RESERVED reservation When markAsWithdrawn called Then actionState emits Success`() = runTest {
        coEvery { reservationRepository.markAsWithdrawn(1L) } returns
            Result.Success(buildReservation(1L, ReservationStatus.WITHDRAWN))

        viewModel = createViewModel()
        viewModel.markAsWithdrawn(1L)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is ActionState.Success)
    }

    /**
     * Given: reserva no existe
     * When: se intenta marcar como retirada
     * Then: actionState emite Error
     */
    @Test
    fun `Given nonexistent reservation When markAsWithdrawn called Then actionState emits Error`() = runTest {
        coEvery {
            reservationRepository.markAsWithdrawn(999L)
        } returns Result.Error(NoSuchElementException("Reserva no encontrada"))

        viewModel = createViewModel()
        viewModel.markAsWithdrawn(999L)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("encontrada"))
    }

    // ── publishPack ──────────────────────────────────────────────────────────

    /**
     * Given: pack válido y sesión activa de un comercio
     * When: se publica el pack
     * Then: actionState emite Success y se usa el commerceId de la sesión
     */
    @Test
    fun `Given valid pack When publishPack called Then actionState emits Success`() = runTest {
        val pack = buildPack()
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        coEvery { publishPackUseCase(pack.copy(commerceId = 1L)) } returns Result.Success(pack)

        viewModel = createViewModel()
        viewModel.publishPack(pack)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is ActionState.Success)
    }

    /**
     * Given: pack con precio inválido
     * When: publishPack falla validación
     * Then: actionState emite Error con mensaje de validación
     */
    @Test
    fun `Given invalid pack When publishPack fails validation Then actionState emits Error`() = runTest {
        val invalidPack = buildPack().copy(discountPrice = 600.0) // mayor que originalPrice
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        coEvery { publishPackUseCase(invalidPack.copy(commerceId = 1L)) } returns
            Result.Error(IllegalArgumentException("El precio de descuento debe ser menor al precio original"))

        viewModel = createViewModel()
        viewModel.publishPack(invalidPack)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("descuento"))
    }

    /**
     * Given: no hay sesión activa
     * When: se intenta publicar un pack
     * Then: actionState emite Error sin llamar al use case
     */
    @Test
    fun `Given no session When publishPack called Then actionState emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        every { reservationRepository.getReservationsByUser(any()) } returns flowOf(emptyList())
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.publishPack(buildPack())
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("Sesión"))
    }

    /**
     * Given: actionState es Success
     * When: se llama resetAction
     * Then: actionState vuelve a Idle
     */
    @Test
    fun `Given Success actionState When resetAction called Then returns to Idle`() = runTest {
        coEvery { reservationRepository.cancelReservation(any()) } returns
            Result.Success(buildReservation())

        viewModel = createViewModel()
        viewModel.cancelReservation(1L)
        advanceUntilIdle()

        viewModel.resetAction()
        assertTrue(viewModel.actionState.value is ActionState.Idle)
    }

    // ── loadCommercePacks ────────────────────────────────────────────────────

    /**
     * Given: comercio con sesión activa tiene packs publicados
     * When: se llama loadCommercePacks
     * Then: commercePacksState emite Success con los packs del comercio
     */
    @Test
    fun `Given commerce has packs When loadCommercePacks called Then commercePacksState emits Success`() = runTest {
        val packs = listOf(buildPack(1L), buildPack(2L))
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { reservationRepository.getReservationsByUser(any()) } returns flowOf(emptyList())
        every { packRepository.getPacksByCommerce(1L) } returns flowOf(packs)

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.loadCommercePacks()
        advanceUntilIdle()

        val state = viewModel.commercePacksState.value
        assertTrue(state is CommercePacksState.Success)
        assertEquals(2, (state as CommercePacksState.Success).packs.size)
    }

    /**
     * Given: repositorio de packs lanza excepción
     * When: loadCommercePacks falla
     * Then: commercePacksState emite Error
     */
    @Test
    fun `Given packRepository throws When loadCommercePacks called Then commercePacksState emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { reservationRepository.getReservationsByUser(any()) } returns flowOf(emptyList())
        every {
            packRepository.getPacksByCommerce(any())
        } returns kotlinx.coroutines.flow.flow { throw RuntimeException("DB error") }

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.loadCommercePacks()
        advanceUntilIdle()

        val state = viewModel.commercePacksState.value
        assertTrue(state is CommercePacksState.Error)
    }

    /**
     * Given: no hay sesión activa
     * When: se llama loadCommercePacks
     * Then: commercePacksState emite Error sin llamar al repositorio
     */
    @Test
    fun `Given no session When loadCommercePacks called Then commercePacksState emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)
        viewModel.loadCommercePacks()
        advanceUntilIdle()

        val state = viewModel.commercePacksState.value
        assertTrue(state is CommercePacksState.Error)
        assertTrue((state as CommercePacksState.Error).message.contains("Sesión"))
    }
}
```

- [ ] **Step 2: Ejecutar los tests para verificar que fallan**

Run: `./gradlew :feature:reservations:testDebugUnitTest --tests "com.aprovecha.app.feature.reservations.ReservationsViewModelTest"`
Expected: FAIL — `ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase, authRepository)` no compila (constructor actual tiene 3 parámetros) y `loadUserReservations()`/`loadCommercePacks()` sin argumentos no existen

- [ ] **Step 3: Actualizar `ReservationsViewModel`**

Reemplazar el contenido completo de `feature/reservations/src/main/kotlin/com/aprovecha/app/feature/reservations/ui/ReservationsViewModel.kt` por:

```kotlin
package com.aprovecha.app.feature.reservations.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// @REQ-F04, F05, F06: ViewModel para reservas y operaciones del comercio

sealed class ReservationsUiState {
    data object Loading : ReservationsUiState()
    data class Success(val reservations: List<Reservation>) : ReservationsUiState()
    data class Error(val message: String) : ReservationsUiState()
}

sealed class CommercePacksState {
    data object Loading : CommercePacksState()
    data class Success(val packs: List<FoodPack>) : CommercePacksState()
    data class Error(val message: String) : CommercePacksState()
}

sealed class ActionState {
    data object Idle : ActionState()
    data object Loading : ActionState()
    data object Success : ActionState()
    data class Error(val message: String) : ActionState()
}

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val packRepository: PackRepository,
    private val publishPackUseCase: PublishPackUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private companion object {
        const val GENERIC_ERROR_MESSAGE = "Error"
        const val UNEXPECTED_ERROR_MESSAGE = "Error inesperado"
        const val SESSION_ERROR_MESSAGE = "Sesión no encontrada. Iniciá sesión nuevamente."
    }

    private val _reservationsState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val reservationsState: StateFlow<ReservationsUiState> = _reservationsState.asStateFlow()

    private val _commercePacksState = MutableStateFlow<CommercePacksState>(CommercePacksState.Loading)
    val commercePacksState: StateFlow<CommercePacksState> = _commercePacksState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    // @REQ-F06: Cargar reservas del usuario de la sesión activa
    fun loadUserReservations() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id
            if (userId == null) {
                _reservationsState.value = ReservationsUiState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            reservationRepository.getReservationsByUser(userId)
                .catch {
                    _reservationsState.value = ReservationsUiState.Error(it.message ?: GENERIC_ERROR_MESSAGE)
                }
                .collect { list -> _reservationsState.value = ReservationsUiState.Success(list) }
        }
    }

    // Cargar packs del comercio de la sesión activa
    fun loadCommercePacks() {
        viewModelScope.launch {
            val commerceId = authRepository.getCurrentUser()?.id
            if (commerceId == null) {
                _commercePacksState.value = CommercePacksState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            packRepository.getPacksByCommerce(commerceId)
                .catch {
                    _commercePacksState.value = CommercePacksState.Error(it.message ?: GENERIC_ERROR_MESSAGE)
                }
                .collect { list -> _commercePacksState.value = CommercePacksState.Success(list) }
        }
    }

    // @REQ-F06: Cancelar reserva
    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = reservationRepository.cancelReservation(reservationId)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: "Error al cancelar")
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    // @REQ-F05: Marcar reserva como retirada
    fun markAsWithdrawn(reservationId: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = reservationRepository.markAsWithdrawn(reservationId)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: GENERIC_ERROR_MESSAGE)
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    // @REQ-F02: Publicar nuevo pack, usando el comercio de la sesión activa
    fun publishPack(pack: FoodPack) {
        viewModelScope.launch {
            val commerceId = authRepository.getCurrentUser()?.id
            if (commerceId == null) {
                _actionState.value = ActionState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = publishPackUseCase(pack.copy(commerceId = commerceId))) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: "Error al publicar")
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    fun resetAction() { _actionState.value = ActionState.Idle }
}
```

- [ ] **Step 4: Ejecutar los tests para verificar que pasan**

Run: `./gradlew :feature:reservations:testDebugUnitTest --tests "com.aprovecha.app.feature.reservations.ReservationsViewModelTest"`
Expected: BUILD SUCCESSFUL, 11 tests pasados

- [ ] **Step 5: Actualizar el `commerceId` hardcodeado en `PublishPackScreen.kt`**

En `feature/reservations/src/main/kotlin/com/aprovecha/app/feature/reservations/ui/PublishPackScreen.kt`, dentro del `onClick` del botón publicar (alrededor de la línea 182), cambiar:

```kotlin
                    val pack = FoodPack(
                        commerceId = 1L, // MVP: comercio hardcodeado
                        name = name,
```

por:

```kotlin
                    val pack = FoodPack(
                        commerceId = 0L, // se sobreescribe con el comercio de la sesion en el ViewModel
                        name = name,
```

- [ ] **Step 6: Verificar que `MyReservationsScreen.kt` y `HomeCommerceScreen.kt` compilan**

Ambos ya llaman a `viewModel.loadUserReservations()` / `viewModel.loadCommercePacks()` sin argumentos (compatibles con la nueva firma).

Run: `./gradlew :feature:reservations:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add feature/reservations/src/main/kotlin/com/aprovecha/app/feature/reservations/ui/ReservationsViewModel.kt feature/reservations/src/test/kotlin/com/aprovecha/app/feature/reservations/ReservationsViewModelTest.kt feature/reservations/src/main/kotlin/com/aprovecha/app/feature/reservations/ui/PublishPackScreen.kt
git commit -m "feat: ReservationsViewModel usa el userId/commerceId de la sesion real"
```

---

## Task 12: Verificación final — suite completa y cobertura JaCoCo

**Files:** ninguno (solo verificación)

- [ ] **Step 1: Ejecutar toda la suite de tests**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL — todos los tests de `core/data`, `feature/auth`, `feature/products`, `feature/reservations` pasan

- [ ] **Step 2: Generar el reporte JaCoCo**

Run: `./gradlew jacocoTestReport`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verificar el umbral de cobertura**

Run: `./gradlew jacocoTestCoverageVerification`
Expected: BUILD SUCCESSFUL — cobertura ≥70% instrucciones / ≥60% ramas (los Composables de `core/ui`, `feature/auth`, `feature/products`, `feature/reservations` están excluidos del reporte, ver `98853b8`)

Si falla por cobertura insuficiente, revisar qué clase nueva quedó sin cubrir (`SessionManager`, `AuthRepositoryImpl`, `ProductsViewModel`, `ReservationsViewModel`, `AuthViewModel`) y agregar los tests faltantes siguiendo el patrón `` `Given X When Y Then Z` `` usado en las Tasks 6-11.

- [ ] **Step 4: Build completo del proyecto**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL — confirma que `core/ui` (Task 1), `core/data` (Tasks 5-9), `feature/auth` (Tasks 3, 4, 9), `feature/products` (Tasks 2, 10) y `feature/reservations` (Task 11) compilan e integran correctamente

- [ ] **Step 5: Commit final (si hubo ajustes en Steps 3-4)**

Si los Steps 3 o 4 requirieron cambios adicionales (tests faltantes, fixes de compilación), commitearlos:

```bash
git add -A
git commit -m "test: ajustes finales de cobertura para Plan A"
```

Si no hubo cambios, no hay nada que commitear — Plan A queda completo con los commits de las Tasks 1-11.
