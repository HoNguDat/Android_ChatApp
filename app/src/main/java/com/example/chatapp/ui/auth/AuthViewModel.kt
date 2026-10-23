
package com.example.chatapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value =
                AuthState.Error("Vui lòng nhập đầy đủ thông tin!")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val result = repository.login(email.trim(), pass)

                _authState.value = if (result.isSuccess) {
                    AuthState.Success
                } else {
                    AuthState.Error(
                        result.exceptionOrNull()?.localizedMessage
                            ?: "Đăng nhập thất bại"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Đã xảy ra lỗi khi đăng nhập"
                )
            }
        }
    }

    fun register(name: String, email: String, pass: String, phone: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank() || phone.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(name, email, pass, phone)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Đăng ký thất bại"
                )
            }
        }
    }
}
