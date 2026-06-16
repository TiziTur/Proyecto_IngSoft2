@file:Suppress("MagicNumber")

package com.aprovecha.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
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
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            RoleCard(
                option = RoleOption(
                    icon = Icons.Default.Storefront,
                    title = "Comercio",
                    subtitle = "Publico mis excedentes",
                    selectedColor = MaterialTheme.colorScheme.primary,
                    onClick = { onRoleSelected(UserRole.COMMERCE) }
                ),
                selected = selectedRole == UserRole.COMMERCE,
                modifier = Modifier.weight(1f).fillMaxHeight()
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
