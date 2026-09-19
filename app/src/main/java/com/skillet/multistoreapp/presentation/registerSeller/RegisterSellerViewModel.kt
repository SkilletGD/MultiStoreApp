package com.skillet.multistoreapp.presentation.registerSeller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.AppUser
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

data class UIState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMesage: String? = null
)

sealed interface SellerEvent{
    data class OnFirstNameChanged(
        val value: String,
    ) : SellerEvent
    data class OnLastNameChanged(
        val value: String,
    ) : SellerEvent

    data class OnEmailChanged(
        val value: String,
    ) : SellerEvent
    data class OnPasswordChanged(
        val value: String,
    ) : SellerEvent
    data class OnConfirmPasswordChanged(
        val value: String,
    ) : SellerEvent
    data class OnPhoneChanged(
        val value: String,
    ) : SellerEvent
    data object OnNextClick : SellerEvent
}

sealed interface SellerEffect {
    data class ShowMessage(
        val message: String
    ) : SellerEffect
    data class NavigateToRegisterScreen(
        val sellerId: String
    ) : SellerEffect

}

@HiltViewModel
class RegisterSellerViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerEffect>()
    val effect: SharedFlow<SellerEffect> = _effect.asSharedFlow()

    fun onEvent(event: SellerEvent){
        when(event){
            is SellerEvent.OnFirstNameChanged -> updateFirstName(event.value)
            is SellerEvent.OnLastNameChanged -> updateLastName(event.value)
            is SellerEvent.OnEmailChanged -> updateEmail(event.value)
            is SellerEvent.OnPasswordChanged -> updatePassword(event.value)
            is SellerEvent.OnConfirmPasswordChanged -> updateConfirmPassword(event.value)
            is SellerEvent.OnPhoneChanged -> updatePhone(event.value)
            is SellerEvent.OnNextClick -> registerSeller()
        }
    }

    private fun updateFirstName(value: String) {
        _uiState.update { current ->
            current.copy(
                firstName = value,
                errorMesage = null
            )
        }
    }
    private fun updateLastName(value: String) {
        _uiState.update { current ->
            current.copy(
                lastName = value,
                errorMesage = null
            )
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
    private fun updateConfirmPassword(value: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = value,
                errorMesage = null
            )
        }
    }
    private fun updatePhone(value: String) {
        _uiState.update { current ->
            current.copy(
                phone = value,
                errorMesage = null
            )
        }
    }

    private fun registerSeller(){
        if(_uiState.value.isLoading) return

        viewModelScope.launch {

            //validaciones
            val firstNameTrimed: String = _uiState.value.firstName.trim()
            val lastNameTrimed: String = _uiState.value.lastName.trim()
            val emailTrimed: String = _uiState.value.email.trim()
            val phoneTrimed: String = _uiState.value.phone.trim()
            val passwordTrimed: String = _uiState.value.password.trim()
            val confirmPasswordTrimed: String = _uiState.value.confirmPassword.trim()

            if(firstNameTrimed.isEmpty()){
                _effect.emit(SellerEffect.ShowMessage("Ingresa tu nombre/s"))
                return@launch
            }
            if(lastNameTrimed.isEmpty()){
                _effect.emit(SellerEffect.ShowMessage("Ingresa tus apellidos"))
                return@launch
            }
            if(emailTrimed.isEmpty()){
                _effect.emit(SellerEffect.ShowMessage("Ingresa tu correo"))
                return@launch
            }
            if(phoneTrimed.isEmpty()){
                _effect.emit(SellerEffect.ShowMessage("Ingresa tu teléfono"))
                return@launch
            }
            if(passwordTrimed.isEmpty()){
                _effect.emit(SellerEffect.ShowMessage("Ingresa una contraseña"))
                return@launch
            }
            if(confirmPasswordTrimed.isEmpty()){
                _effect.emit(SellerEffect.ShowMessage("Ingresa una contraseña"))
                return@launch
            }
            if(passwordTrimed != confirmPasswordTrimed){
                _effect.emit(SellerEffect.ShowMessage("Las contraseñas no coinciden"))
                return@launch
            }

            //pasamos a estado de carga
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMesage = null
                )
            }

            //inicia proceso de registro
            val result: Result<AppUser> = authRepository.registerUser(
                firstName = firstNameTrimed,
                lastName = lastNameTrimed,
                email = emailTrimed,
                password = passwordTrimed,
                phone = phoneTrimed,
                role = UserRole.SELLER
            )

            //resultados de la operacion
            //si sale bien
            result.onSuccess {sellerId ->
                _uiState.update { current ->
                    current.copy(
                        firstName = "",
                        lastName = "",
                        email = "",
                        password = "",
                        confirmPassword = "",
                        phone = "",
                        isLoading = false,
                        errorMesage = null
                    )
                }
                _effect.emit(SellerEffect.ShowMessage("La cuenta de vendedor se a creado con éxito"))
                _effect.emit(SellerEffect.NavigateToRegisterScreen(sellerId.uid))

                //si sale mal
            }.onFailure {e ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMesage = e.message ?: "No se pudo registrar al vendedor"
                    )
                }
                _effect.emit(SellerEffect.ShowMessage(e.message ?: "No se pudo registrar alvededor"))
            }
        }
    }
}