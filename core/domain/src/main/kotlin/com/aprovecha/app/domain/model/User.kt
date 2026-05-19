package com.aprovecha.app.domain.model

/**
 * Modelo de dominio para un usuario de la plataforma.
 *
 * // @REQ-F01: El comercio (y el consumidor) debe poder registrarse en la plataforma.
 */
data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val role: UserRole,
    val avatarUri: String? = null
)
