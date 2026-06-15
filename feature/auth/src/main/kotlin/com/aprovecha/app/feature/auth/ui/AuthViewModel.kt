package com.aprovecha.app.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// @REQ-F01: ViewModel de autenticación

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val role: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // @REQ-F01: Login
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Completá todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.login(email, password)) {
                is Result.Success -> AuthUiState.Success(result.data.role.name)
                is Result.Error -> AuthUiState.Error(result.exception.message ?: "Error al iniciar sesión")
                else -> AuthUiState.Error("Error inesperado")
            }
        }
    }

    // @REQ-F01: Registro con selección de rol COMMERCE / CONSUMER
    fun register(email: String, password: String, name: String, role: UserRole) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Completá todos los campos")
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.register(email, password, name = name, role)) {
                is Result.Success -> AuthUiState.Success(result.data.role.name)
                is Result.Error -> AuthUiState.Error(result.exception.message ?: "Error al registrarse")
                else -> AuthUiState.Error("Error inesperado")
            }
        }
    }

    // @REQ-F01: Cierre de sesión
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
