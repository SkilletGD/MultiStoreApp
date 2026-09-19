package com.skillet.multistoreapp.presentation.registerCustomer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.UserRole
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

data class UiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMesage: String? = null,
)

sealed interface CustomerEvent {
    data class OnFirstNameChanged(
        val value: String,
    ) : CustomerEvent

    data class OnLastNameChanged(
        val value: String,
    ) : CustomerEvent

    data class OnEmailChanged(
        val value: String,
    ) : CustomerEvent

    data class OnPasswordChanged(
        val value: String,
    ) : CustomerEvent

    data class OnConfirmPasswordChanged(
        val value: String,
    ) : CustomerEvent

    data object OnRegisterClick : CustomerEvent
}

sealed interface CustomerEffect {
    data class ShowMessage(
        val message: String,
    ) : CustomerEffect

    data object NavigateToHome : CustomerEffect
}

@HiltViewModel
class RegisterCustomerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomerEffect>()
    val effect: SharedFlow<CustomerEffect> = _effect.asSharedFlow()


    fun onEvent(event: CustomerEvent) {
        when (event) {
            is CustomerEvent.OnFirstNameChanged -> updateFirstName(event.value)
            is CustomerEvent.OnLastNameChanged -> updateLastName(event.value)
            is CustomerEvent.OnEmailChanged -> updateEmail(event.value)
            is CustomerEvent.OnPasswordChanged -> updatePassword(event.value)
            is CustomerEvent.OnConfirmPasswordChanged -> updateConfirmPassword(event.value)
            is CustomerEvent.OnRegisterClick -> registerCustomer()
        }
    }

    private fun updateFirstName(value: String) {
        _uiState.update {
            current -> current.copy(
                firstName = value,
                errorMesage = null
            )
        }
    }

    private fun updateLastName(value: String) {
        _uiState.update {
            current -> current.copy(
                lastName = value,
                errorMesage = null
            )
        }
    }

    private fun updateEmail(value: String) {
        _uiState.update {
            current -> current.copy(
                email = value,
                errorMesage = null
            )
        }
    }


    private fun updatePassword(value: String) {
        _uiState.update {
            current -> current.copy(
                password = value,
                errorMesage = null
            )
        }
    }

    private fun updateConfirmPassword(value: String) {
        _uiState.update {
            current -> current.copy(
                confirmPassword = value,
                errorMesage = null
            )
        }
    }

    private fun registerCustomer() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val firstNameTrimed: String = _uiState.value.firstName.trim()
            val lastNameTrimed: String = _uiState.value.lastName.trim()
            val emailTrimed: String = _uiState.value.email.trim()
            val passwordTrimed: String = _uiState.value.password.trim()
            val confirmPasswordTrimed: String = _uiState.value.confirmPassword.trim()

            if (firstNameTrimed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("El nombre no puede estar vacío"))
                return@launch
            }

            if (lastNameTrimed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("El apellido no puede estar vacío"))
                return@launch
            }

            if (emailTrimed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("El correo no puede estar vacío"))
                return@launch
            }

            if (passwordTrimed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("La contraseña no puede estar vacía"))
                return@launch
            }

            if (confirmPasswordTrimed.isEmpty()) {
                _effect.emit(CustomerEffect.ShowMessage("La contraseña no puede estar vacía"))
                return@launch
            }

            if (passwordTrimed != confirmPasswordTrimed) {
                _effect.emit(CustomerEffect.ShowMessage("Las contraseñas no coinciden"))
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMesage = null
            )

            val result = authRepository.registerUser(
                firstName = firstNameTrimed,
                lastName = lastNameTrimed,
                email = emailTrimed,
                password = passwordTrimed,
                role = UserRole.CUSTOMER,
                phone = ""

            )

            result.onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            firstName = "",
                            lastName = "",
                            email = "",
                            password = "",
                            confirmPassword = "",
                            isLoading = false,
                            errorMesage = null
                        )
                    }
                    _effect.emit(CustomerEffect.ShowMessage("Registro exitoso"))
                    _effect.emit(CustomerEffect.NavigateToHome)
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMesage = e.message ?: "No se pudo registrar el usuario"
                        )
                    }
                    _effect.emit(CustomerEffect.ShowMessage(e.message ?: "No se pudo registrar el usuario"))

                }

        }
    }
}