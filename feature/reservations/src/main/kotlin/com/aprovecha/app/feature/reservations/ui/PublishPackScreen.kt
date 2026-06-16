@file:Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod", "WildcardImport", "MagicNumber")

package com.aprovecha.app.feature.reservations.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import java.time.LocalDateTime

// @REQ-F02: Formulario de publicación de pack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishPackScreen(
    onBack: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val actionState by viewModel.actionState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success) {
            onPublishSuccess()
            viewModel.resetAction()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar Pack") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Nombre del pack
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del pack") },
                placeholder = { Text("Ej: Cajita Panificados") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("¿Qué incluye el pack?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Precios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = originalPrice,
                    onValueChange = { originalPrice = it },
                    label = { Text("Precio original ($)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = discountPrice,
                    onValueChange = { discountPrice = it },
                    label = { Text("Precio descuento ($)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Descuento calculado
            val origD = originalPrice.toDoubleOrNull() ?: 0.0
            val discD = discountPrice.toDoubleOrNull() ?: 0.0
            if (origD > 0 && discD > 0 && discD < origD) {
                val pct = ((1 - discD / origD) * 100).toInt()
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "✅ Descuento del $pct% — ahorro de $${(origD - discD).toInt()}",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (discD >= origD && origD > 0) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "⚠ El precio descuento debe ser menor al original",
                        color = Color(0xFFB00020),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Cantidad
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Cantidad disponible") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Error de acción
            if (actionState is ActionState.Error) {
                Text(
                    (actionState as ActionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))

            // Botón publicar
            val isValid = name.isNotBlank()
                && description.isNotBlank()
                && origD > 0 && discD > 0 && discD < origD
                && (quantity.toIntOrNull() ?: 0) > 0

            Button(
                onClick = {
                    val pack = FoodPack(
                        commerceId = 0L, // se sobreescribe con el comercio de la sesion en el ViewModel
                        name = name,
                        description = description,
                        originalPrice = origD,
                        discountPrice = discD,
                        quantity = quantity.toIntOrNull() ?: 1,
                        status = PackStatus.AVAILABLE,
                        expirationTime = LocalDateTime.now().plusHours(6)
                    )
                    viewModel.publishPack(pack)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                enabled = isValid && actionState !is ActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (actionState is ActionState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Publicar Pack 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
