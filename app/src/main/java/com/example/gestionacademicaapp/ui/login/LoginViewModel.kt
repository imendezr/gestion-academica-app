package com.example.gestionacademicaapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<SingleUiState<Usuario>>()
    val loginState: LiveData<SingleUiState<Usuario>> get() = _loginState

    fun login(cedula: String, clave: String) {
        viewModelScope.launch {
            _loginState.value = SingleUiState.Loading
            val result = usuarioRepository.login(cedula, clave)
            result
                .onSuccess { user -> _loginState.value = SingleUiState.Success(user) }
                .onFailure { error ->
                    val message = when (error) {
                        is HttpException -> when (error.code()) {
                            401 -> "Credenciales inválidas"
                            else -> error.toUserMessage()
                        }

                        else -> error.toUserMessage()
                    }
                    _loginState.value = SingleUiState.Error(message)
                }
        }
    }
}
