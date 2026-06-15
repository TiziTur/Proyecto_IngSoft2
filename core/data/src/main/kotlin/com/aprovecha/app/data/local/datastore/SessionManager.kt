package com.aprovecha.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// @REQ-F01: Persistencia de la sesion activa (login/registro/logout)

private object SessionKeys {
    val USER_ID = longPreferencesKey("session_user_id")
    val USER_EMAIL = stringPreferencesKey("session_user_email")
    val USER_NAME = stringPreferencesKey("session_user_name")
    val USER_ROLE = stringPreferencesKey("session_user_role")
}

class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val currentUser: Flow<User?> = dataStore.data.map { prefs ->
        val id = prefs[SessionKeys.USER_ID]
        val email = prefs[SessionKeys.USER_EMAIL]
        val name = prefs[SessionKeys.USER_NAME]
        val role = prefs[SessionKeys.USER_ROLE]
        if (id != null && email != null && name != null && role != null) {
            User(id = id, email = email, name = name, role = UserRole.valueOf(role))
        } else {
            null
        }
    }

    suspend fun getCurrentUser(): User? = currentUser.first()

    suspend fun saveSession(user: User) {
        dataStore.edit { prefs ->
            prefs[SessionKeys.USER_ID] = user.id
            prefs[SessionKeys.USER_EMAIL] = user.email
            prefs[SessionKeys.USER_NAME] = user.name
            prefs[SessionKeys.USER_ROLE] = user.role.name
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
