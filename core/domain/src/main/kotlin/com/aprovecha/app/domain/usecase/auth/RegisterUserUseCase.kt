package com.aprovecha.app.domain.usecase.auth

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository

/**
 * Caso de uso: Registrar un nuevo usuario en la plataforma.
 *
 * // @REQ-F01: El comercio debe poder registrarse en la plataforma.
 * Aplica también al registro de consumidores, ya que comparten el mismo flujo
 * con diferente [UserRole].
 *
 * @param authRepository Repositorio de autenticación inyectado por Hilt
 */
class RegisterUserUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Ejecuta el registro de un nuevo usuario.
     *
     * // @REQ-F01: Registro de usuario con email, contraseña, nombre y rol
     *
     * @param email Email del usuario (debe ser único en el sistema)
     * @param password Contraseña en texto plano (se hashea en la capa de datos)
     * @param name Nombre o razón social
     * @param role [UserRole.COMMERCE] o [UserRole.CONSUMER]
     * @return [Result.Success] con el [User] creado, o [Result.Error] con el motivo
     */
    @Requirement("REQ-F01", "Registro de nuevo usuario con selección de rol")
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return Result.Error(IllegalArgumentException("Los campos no pueden estar vacíos"))
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return Result.Error(
                IllegalArgumentException("La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres")
            )
        }
        return authRepository.register(email.trim(), password, name.trim(), role)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
