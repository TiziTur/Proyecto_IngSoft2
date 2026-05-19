package com.aprovecha.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para persistencia local de usuarios.
 *
 * // @REQ-F01: El comercio (y consumidor) debe poder registrarse en la plataforma.
 * El email tiene índice único para garantizar que no existan dos cuentas con el mismo email.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    /** Contraseña hasheada con SHA-256 — nunca se almacena en texto plano */
    val passwordHash: String,
    val nombre: String,
    /** Rol del usuario: "CONSUMER" o "COMMERCE" */
    val role: String,
    val avatarUri: String? = null
)
