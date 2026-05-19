package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.entity.UserEntity
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import javax.inject.Inject

// @REQ-F01: Implementación del repositorio de autenticación

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    // @REQ-F01: Registro de nuevo usuario (COMMERCE o CONSUMER)
    override suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> = try {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            Result.Error(IllegalStateException("El email ya está registrado"))
        } else {
            val entity = UserEntity(
                email = email,
                passwordHash = password.hashCode().toString(),
                nombre = name,
                role = role.name
            )
            val id = userDao.insertUser(entity)
            Result.Success(entity.copy(id = id).toDomain())
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    // @REQ-F01: Login de usuario registrado
    override suspend fun login(email: String, password: String): Result<User> = try {
        val entity = userDao.getUserByEmail(email)
        if (entity == null) {
            Result.Error(IllegalArgumentException("Usuario no encontrado"))
        } else if (entity.passwordHash != password.hashCode().toString()) {
            Result.Error(IllegalArgumentException("Contraseña incorrecta"))
        } else {
            Result.Success(entity.toDomain())
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getCurrentUser(): User? = null

    override suspend fun logout() { /* MVP: sesión en memoria */ }
}

private fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    name = nombre,
    role = UserRole.valueOf(role)
)
