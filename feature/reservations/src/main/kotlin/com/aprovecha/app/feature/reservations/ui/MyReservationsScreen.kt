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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.ui.theme.Bosque50
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
    var showFilterMenu by remember { mutableStateOf(false) }
    var historyFilter by remember { mutableStateOf("TODOS") }

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
                    Box {
                        IconButton(
                            onClick = { if (activeTab == 1) showFilterMenu = true },
                            enabled = activeTab == 1
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint = if (historyFilter != "TODOS" && activeTab == 1)
                                    MaterialTheme.colorScheme.secondary
                                else
                                    MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            listOf("TODOS", "RETIRADOS", "CANCELADOS").forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option,
                                            fontWeight = if (option == historyFilter) FontWeight.Bold else FontWeight.Normal,
                                            color = if (option == historyFilter)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        historyFilter = option
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("Activas", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Historial", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold)
                }
            }

            // Show active filter chip when not showing all history
            if (activeTab == 1 && historyFilter != "TODOS") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filtro:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilterChip(
                        selected = true,
                        onClick = { historyFilter = "TODOS" },
                        label = { Text(historyFilter, fontSize = 12.sp) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Quitar filtro", modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            when (val state = reservationsState) {
                is ReservationsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    val filteredHistory = when (historyFilter) {
                        "RETIRADOS" -> history.filter { it.status == ReservationStatus.WITHDRAWN }
                        "CANCELADOS" -> history.filter { it.status == ReservationStatus.CANCELLED }
                        else -> history
                    }
                    val displayed = if (activeTab == 0) active else filteredHistory

                    if (displayed.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    if (activeTab == 0) "No tenés reservas activas"
                                    else if (historyFilter != "TODOS") "No hay reservas ${historyFilter.lowercase()}"
                                    else "Sin historial",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        ReservationStatus.RESERVED -> MaterialTheme.colorScheme.tertiary
        ReservationStatus.WITHDRAWN -> MaterialTheme.colorScheme.primary
        ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = when (reservation.status) {
        ReservationStatus.RESERVED -> "RESERVADO"
        ReservationStatus.WITHDRAWN -> "RETIRADO"
        ReservationStatus.CANCELLED -> "CANCELADO"
    }
    val fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Bosque50)),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🛒", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pack #${reservation.packId}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
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
                Text(
                    "Reservado el ${reservation.fechaReserva.format(fmt)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (reservation.status == ReservationStatus.RESERVED) {
                IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
