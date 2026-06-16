@file:Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod", "WildcardImport", "MagicNumber")

package com.aprovecha.app.feature.reservations.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import com.aprovecha.app.ui.theme.Bosque50
import java.io.File
import java.time.LocalDateTime

// @REQ-F02: Formulario de publicación de pack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishPackScreen(
    onBack: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val actionState by viewModel.actionState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var showRetiroDesdeDialog by remember { mutableStateOf(false) }
    var showRetiroHastaDialog by remember { mutableStateOf(false) }
    val retiroDesdeState = rememberTimePickerState(initialHour = 19, initialMinute = 0)
    val retiroHastaState = rememberTimePickerState(initialHour = 21, initialMinute = 0)
    var retiroDesde by remember { mutableStateOf("") }
    var retiroHasta by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = tempCameraUri
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val dest = File(context.filesDir, "pack_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            photoUri = Uri.fromFile(dest)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.filesDir, "pack_cam_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showRetiroDesdeDialog) {
        AlertDialog(
            onDismissRequest = { showRetiroDesdeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    retiroDesde = "%02d:%02d".format(retiroDesdeState.hour, retiroDesdeState.minute)
                    showRetiroDesdeDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRetiroDesdeDialog = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = retiroDesdeState) }
        )
    }

    if (showRetiroHastaDialog) {
        AlertDialog(
            onDismissRequest = { showRetiroHastaDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    retiroHasta = "%02d:%02d".format(retiroHastaState.hour, retiroHastaState.minute)
                    showRetiroHastaDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRetiroHastaDialog = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = retiroHastaState) }
        )
    }

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

            // Foto del pack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto del pack",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, Bosque50)
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍱", fontSize = 48.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cámara")
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }

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

            // Horario de retiro
            Text("Horario de retiro", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Desde",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                    OutlinedButton(
                        onClick = { showRetiroDesdeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (retiroDesde.isBlank()) "Elegir hora" else retiroDesde,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Hasta",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                    OutlinedButton(
                        onClick = { showRetiroHastaDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (retiroHasta.isBlank()) "Elegir hora" else retiroHasta,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

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
                && retiroDesde.isNotBlank() && retiroHasta.isNotBlank()

            Button(
                onClick = {
                    val pack = FoodPack(
                        commerceId = 0L,
                        name = name,
                        description = description,
                        originalPrice = origD,
                        discountPrice = discD,
                        quantity = quantity.toIntOrNull() ?: 1,
                        photoUrl = photoUri?.toString(),
                        status = PackStatus.AVAILABLE,
                        expirationTime = LocalDateTime.now().plusHours(6),
                        retiroDesde = retiroDesde,
                        retiroHasta = retiroHasta
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

            Spacer(Modifier.height(80.dp))
        }
    }
}
