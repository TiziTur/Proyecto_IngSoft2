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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aprovecha.app.ui.theme.Bosque50

// @REQ-F04: Detalle de pack con botón "Reservar pack completo"

@Composable
fun PackDetailScreen(
    packId: Long,
    onBack: () -> Unit,
    onReserveSuccess: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val pack by viewModel.selectedPack.collectAsState()
    val reserveState by viewModel.reserveState.collectAsState()
    val favoritePackIds by viewModel.favoritePackIds.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var pickupTermsAccepted by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableIntStateOf(1) }

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

        // ── Hero ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (foodPack.photoUrl != null) {
                AsyncImage(
                    model = foodPack.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, Bosque50))
                        )
                )
            }
            // Botones overlay
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                IconButton(onClick = { pack?.id?.let { viewModel.toggleFavorite(it) } }) {
                    val isFav = pack?.id?.let { it in favoritePackIds } == true
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFav) MaterialTheme.colorScheme.tertiary else Color.White
                    )
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
                .navigationBarsPadding()
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

            // Selector de cantidad
            Text("Cantidad a reservar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedIconButton(
                    onClick = { if (selectedQuantity > 1) selectedQuantity-- },
                    enabled = selectedQuantity > 1
                ) {
                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "$selectedQuantity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedIconButton(
                    onClick = { if (selectedQuantity < foodPack.quantity) selectedQuantity++ },
                    enabled = selectedQuantity < foodPack.quantity
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "de ${foodPack.quantity} disponibles",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PickupTermsNotice(retiroDesde = foodPack.retiroDesde, retiroHasta = foodPack.retiroHasta)
            PickupTermsCheckbox(
                checked = pickupTermsAccepted,
                onCheckedChange = { pickupTermsAccepted = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón reservar
            Button(
                onClick = { viewModel.reservePack(packId = foodPack.id, cantidad = selectedQuantity) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = pickupTermsAccepted && reserveState !is ReserveUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (reserveState is ReserveUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        "Reservar $selectedQuantity ${if (selectedQuantity == 1) "unidad" else "unidades"} por $${(foodPack.discountPrice * selectedQuantity).toInt()} 🛒",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PickupTermsNotice(retiroDesde: String, retiroHasta: String) {
    val horario = if (retiroDesde.isNotBlank() && retiroHasta.isNotBlank())
        "entre las $retiroDesde y las $retiroHasta hs"
    else
        "en el horario acordado con el comercio"
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
                "Al rescatar esta comida, te comprometés a retirarla hoy $horario. " +
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
