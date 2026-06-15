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
