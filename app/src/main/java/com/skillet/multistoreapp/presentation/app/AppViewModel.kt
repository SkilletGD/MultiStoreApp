package com.skillet.multistoreapp.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppEffect{
    data object NavigateToLogin: AppEffect
}
@HiltViewModel
class AppViewModel @Inject constructor (
    private val authRepository: AuthRepository
): ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _effect = MutableSharedFlow<AppEffect>()
    val effect: SharedFlow<AppEffect> = _effect.asSharedFlow()


    init {
        checkSession()
    }
    private fun checkSession(){
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()

            _startDestination.value = when (user?.role) {
                "CUSTOMER" -> AppRoute.CutomerRoot.route
                "SELLER" -> AppRoute.SellerRoot.route
                null -> AppRoute.Login.route
                else -> AppRoute.Login.route
            }
        }
    }

    fun logOut(){
        authRepository.logOut()

        viewModelScope.launch {
            _effect.emit(AppEffect.NavigateToLogin)
        }
    }
}