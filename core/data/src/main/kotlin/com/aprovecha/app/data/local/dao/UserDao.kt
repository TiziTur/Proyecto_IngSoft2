package com.aprovecha.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aprovecha.app.data.local.entity.UserEntity

/**
 * DAO para operaciones de acceso a datos de usuarios.
 *
 * // @REQ-F01: El comercio (y consumidor) debe poder registrarse en la plataforma.
 */
@Dao
interface UserDao {

    // @REQ-F01: Insertar nuevo usuario — falla si el email ya existe (ABORT)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    // @REQ-F01: Buscar usuario por email para login
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Buscar usuario por ID
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?
}
