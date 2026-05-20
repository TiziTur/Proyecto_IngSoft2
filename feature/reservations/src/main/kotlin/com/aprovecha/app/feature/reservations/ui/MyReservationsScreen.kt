@file:Suppress("LongMethod", "CognitiveComplexMethod", "WildcardImport", "MaxLineLength", "MagicNumber")

package com.aprovecha.app.feature.reservations.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import java.time.format.DateTimeFormatter

// @REQ-F04: Ver mis reservas activas
// @REQ-F06: Cancelar reserva desde esta pantalla

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    onBack: () -> Unit,
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val reservationsState by viewModel.reservationsState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadUserReservations() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Tabs Activas / Historial
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = Color(0xFF2E7D32)
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("Activas", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Historial", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold)
                }
            }

            when (val state = reservationsState) {
                is ReservationsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2E7D32))
                    }
                }
                is ReservationsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ReservationsUiState.Success -> {
                    val active = state.reservations.filter { it.status == ReservationStatus.RESERVED }
                    val history = state.reservations.filter {
                        it.status == ReservationStatus.WITHDRAWN || it.status == ReservationStatus.CANCELLED
                    }
                    val displayed = if (activeTab == 0) active else history

                    if (displayed.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(48.dp))
                                Text(
                                    if (activeTab == 0) "No tenés reservas activas" else "Sin historial",
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayed, key = { it.id }) { reservation ->
                                ReservationCard(
                                    reservation = reservation,
                                    onCancel = { viewModel.cancelReservation(reservation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(reservation: Reservation, onCancel: () -> Unit) {
    val statusColor = when (reservation.status) {
        ReservationStatus.RESERVED -> Color(0xFFE65100)
        ReservationStatus.WITHDRAWN -> Color(0xFF2E7D32)
        ReservationStatus.CANCELLED -> Color(0xFF9E9E9E)
    }
    val statusLabel = when (reservation.status) {
        ReservationStatus.RESERVED -> "RESERVADO"
        ReservationStatus.WITHDRAWN -> "RETIRADO"
        ReservationStatus.CANCELLED -> "CANCELADO"
    }
    val fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm")

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
            // Icono gradiente
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🛒", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pack #${reservation.packId}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("Reservado el ${reservation.fechaReserva.format(fmt)}", fontSize = 12.sp, color = Color(0xFF757575))
            }

            if (reservation.status == ReservationStatus.RESERVED) {
                IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = Color(0xFFEF5350)
                    )
                }
            }
        }
    }
}
