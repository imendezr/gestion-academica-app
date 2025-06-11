package com.example.gestionacademicaapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.ui.common.state.ErrorType
import com.example.gestionacademicaapp.ui.common.state.UiState
import com.example.gestionacademicaapp.utils.mapErrorType
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Usuario>>(UiState.Success(null))
    val loginState: StateFlow<UiState<Usuario>> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.emit(UiState.Loading)
            try {
                val result = usuarioRepository.login(email, password)
                result.fold(
                    onSuccess = { user ->
                        _loginState.emit(UiState.Success(user))
                    },
                    onFailure = { error ->
                        val (message, errorType) = when (error) {
                            is HttpException -> when (error.code()) {
                                401 -> "Usuario o contraseña incorrectos" to ErrorType.VALIDATION
                                else -> error.toUserMessage() to mapErrorType(error)
                            }
                            else -> error.toUserMessage() to mapErrorType(error)
                        }
                        _loginState.emit(UiState.Error(message, errorType))
                    }
                )
            } catch (e: Exception) {
                _loginState.emit(UiState.Error(e.toUserMessage(), mapErrorType(e)))
            }
        }
    }
}
