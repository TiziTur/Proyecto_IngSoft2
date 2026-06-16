package com.aprovecha.app.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            _user.value = authRepository.getCurrentUser()
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _user.value = null
            onComplete()
        }
    }
}
