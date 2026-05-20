@file:Suppress("LongMethod", "WildcardImport", "MaxLineLength", "MagicNumber")

package com.aprovecha.app.feature.products.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// @REQ-F04: Detalle de pack con botón "Reservar ahora"

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
            CircularProgressIndicator(color = Color(0xFF2E7D32))
        }
        return
    }

    val foodPack = pack!!
    val discountPct = ((1 - foodPack.discountPrice / foodPack.originalPrice) * 100).toInt()
    val saving = foodPack.originalPrice - foodPack.discountPrice

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {

        // ── Hero con gradiente verde ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))))
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
                color = Color(0xFFE65100),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
            ) {
                Text(
                    "-$discountPct%",
                    color = Color.White,
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
                    Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(16.dp))
                    Text("Comercio #${foodPack.commerceId}", fontSize = 13.sp, color = Color(0xFF757575))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(16.dp))
                    Text("~2.3 km", fontSize = 13.sp, color = Color(0xFF757575))
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Descripción
            Text(
                text = foodPack.description.ifBlank { "Pack sorpresa de alimentos frescos con descuento. ¡Rescatá comida y ahorrá!" },
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF424242)
            )

            // Precios
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp),
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
                            color = Color(0xFFBDBDBD),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            "$${foodPack.discountPrice.toInt()}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Surface(
                        color = Color(0xFFE65100).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "Ahorrás $${saving.toInt()}",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Info adicional
            Text(
                "${foodPack.quantity} unidades disponibles",
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón reservar
            Button(
                onClick = { viewModel.reservePack(packId = foodPack.id) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = reserveState !is ReserveUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                if (reserveState is ReserveUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Reservar ahora 🛒", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }
    }
}
