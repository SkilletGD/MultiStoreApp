package com.skillet.multistoreapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMesage: String? = null,
)

sealed class LoginEvent {
    data class OnEmailChanged(
        val value: String,
    ) : LoginEvent()

    data class OnPasswordChanged(
        val value: String,
    ) : LoginEvent()

    data object OnLoginClick : LoginEvent()
}

sealed interface LoginEffect {
    data class ShowMessage(
        val message: String,
    ) : LoginEffect

    data class NavigateByRole(
        val role: String
    ) : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChanged -> updateEmail(event.value)
            is LoginEvent.OnPasswordChanged -> updatePassword(event.value)
            is LoginEvent.OnLoginClick -> loginUser()
        }
    }

    private fun updateEmail(value: String) {
        _uiState.update { current ->
            current.copy(
                email = value,
                errorMesage = null
            )
        }

    }

    private fun updatePassword(value: String) {
        _uiState.update { current ->
            current.copy(
                password = value,
                errorMesage = null
            )
        }
    }

    private fun loginUser() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val emailTrimed: String = _uiState.value.email.trim()
            val passwordTrimed: String = _uiState.value.password.trim()
            if (emailTrimed.isEmpty()) {
                _effect.emit(LoginEffect.ShowMessage("Ingrese su correo"))
                return@launch
            }
            if (passwordTrimed.isEmpty()) {
                _effect.emit(LoginEffect.ShowMessage("Ingrese su contraseña"))
                return@launch
            }
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMesage = null
                )
            }

            val result = authRepository.Login(
                email = emailTrimed,
                password = passwordTrimed
            )

            result.onSuccess {user ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMesage = null
                    )
                }
                _effect.emit(LoginEffect.ShowMessage("Inicio de sesion exitoso"))
                _effect.emit(LoginEffect.NavigateByRole(user.role))
            }.onFailure {e ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMesage = e.message ?: "No se puedo iniciar sesión"
                    )
                }
                _effect.emit(LoginEffect.ShowMessage(e.message ?: "No se puedo iniciar sesión"))
            }
        }
    }
}
