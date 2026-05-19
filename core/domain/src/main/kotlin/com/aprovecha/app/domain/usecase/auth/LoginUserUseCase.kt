package com.aprovecha.app.domain.usecase.auth

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.repository.AuthRepository

/**
 * Caso de uso: Autenticar un usuario existente.
 *
 * // @REQ-F01: Implícito — el registro habilita el login posterior.
 *
 * @param authRepository Repositorio de autenticación inyectado por Hilt
 */
class LoginUserUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Ejecuta el inicio de sesión.
     *
     * // @REQ-F01: Login de usuario registrado con email y contraseña
     *
     * @return [Result.Success] con el [User] autenticado, o [Result.Error]
     */
    @Requirement("REQ-F01", "Login de usuario con email y contraseña")
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error(IllegalArgumentException("Email y contraseña son requeridos"))
        }
        return authRepository.login(email.trim(), password)
    }
}
