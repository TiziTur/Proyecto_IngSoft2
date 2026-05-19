package com.aprovecha.app.domain.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole

/**
 * Contrato del repositorio de autenticación.
 *
 * // @REQ-F01: El comercio (y el consumidor) debe poder registrarse en la plataforma.
 *
 * La implementación concreta vive en :core:data y es inyectada por Hilt.
 */
interface AuthRepository {

    // @REQ-F01: Registrar un nuevo usuario (comercio o consumidor)
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User>

    // @REQ-F01: Iniciar sesión con credenciales existentes
    suspend fun login(email: String, password: String): Result<User>

    /** Cerrar sesión del usuario actual */
    suspend fun logout()

    /** Obtener el usuario actualmente autenticado, null si no hay sesión */
    suspend fun getCurrentUser(): User?
}
