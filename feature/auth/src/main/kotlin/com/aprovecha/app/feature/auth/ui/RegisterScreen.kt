package com.aprovecha.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aprovecha.app.domain.model.UserRole

// @REQ-F01: Pantalla de registro con selección de rol COMMERCE / CONSUMER

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
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo / Nombre del local") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mín. 6 caracteres)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Selección de rol
            Text(
                text = "¿Cómo vas a usar Aprovecha!?",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF212121)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleCard(
                    icon = Icons.Default.Person,
                    title = "Consumidor",
                    subtitle = "Compro packs con descuento",
                    selected = selectedRole == UserRole.CONSUMER,
                    selectedColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedRole = UserRole.CONSUMER }
                )
                RoleCard(
                    icon = Icons.Default.Storefront,
                    title = "Comercio",
                    subtitle = "Publico mis excedentes",
                    selected = selectedRole == UserRole.COMMERCE,
                    selectedColor = Color(0xFFE65100),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedRole = UserRole.COMMERCE }
                )
            }

            // Error
            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botón registrar
            Button(
                onClick = {
                    if (selectedRole == null) return@Button
                    viewModel.register(email, password, name, selectedRole!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                enabled = uiState !is AuthUiState.Loading && selectedRole != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Registrarme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) selectedColor else Color(0xFFE0E0E0)
    val bgColor = if (selected) selectedColor.copy(alpha = 0.08f) else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) selectedColor else Color(0xFF9E9E9E),
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (selected) selectedColor else Color(0xFF212121)
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = Color(0xFF757575),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }