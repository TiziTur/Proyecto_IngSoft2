@file:Suppress("LongMethod", "WildcardImport", "MaxLineLength", "MagicNumber")

package com.aprovecha.app.feature.reservations.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
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
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus

// @REQ-F02: Home del comercio — ver packs publicados y estadísticas

@Composable
fun HomeCommerceScreen(
    onPublishPack: () -> Unit,
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val packsState by viewModel.commercePacksState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCommercePacks() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // ── Header verde ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF2E7D32), Color(0xFF388E3C))))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hola, Panadería 👋", fontSize = 13.sp, color = Color(0xFFA5D6A7))
                    Text("Panel Comercio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                }
            }
        }

        // ── Stats ─────────────────────────────────────────────────────────────
        val packs = (packsState as? CommercePacksState.Success)?.packs ?: emptyList()
        val active = packs.count { it.status == PackStatus.AVAILABLE }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("$active", "Packs activos", Color(0xFF2E7D32), Modifier.weight(1f))
            StatCard("${packs.size}", "Total publicados", Color(0xFFE65100), Modifier.weight(1f))
            StatCard("🌱", "Kg rescatados", Color(0xFF2E7D32), Modifier.weight(1f))
        }

        // ── Sección packs ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mis Packs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onPublishPack,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Publicar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Lista de packs
        when (val state = packsState) {
            is CommercePacksState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                }
            }
            is CommercePacksState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is CommercePacksState.Success -> {
                if (state.packs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🍱", fontSize = 48.sp)
                            Text("Publicá tu primer pack", color = Color(0xFF9E9E9E))
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.packs, key = { it.id }) { pack ->
                            CommercePackCard(pack = pack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontSize = if (value.length <= 4) 24.sp else 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = Color(0xFF757575))
        }
    }
}

@Composable
private fun CommercePackCard(pack: FoodPack) {
    val isActive = pack.status == PackStatus.AVAILABLE
    val badgeColor = if (isActive) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
    val badgeBg = if (isActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val badgeLabel = if (isActive) "ACTIVO" else pack.status.name
    val discountPct = ((1 - pack.discountPrice / pack.originalPrice) * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF8F00))),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🍞", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(pack.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("${pack.quantity} unidades · -$discountPct%", fontSize = 12.sp, color = Color(0xFF757575))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$${pack.discountPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                    Text(
                        "$${pack.originalPrice.toInt()}",
                        fontSize = 12.sp,
                        color = Color(0xFFBDBDBD),
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        }
    }
}
