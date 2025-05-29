package com.example.gestionacademicaapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(cedula: String, clave: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = usuarioRepository.login(cedula, clave)
            result
                .onSuccess { user -> _loginState.value = LoginState.Success(user) }
                .onFailure { error ->
                    val message = when (error) {
                        is HttpException -> when (error.code()) {
                            401 -> "Credenciales inválidas"
                            else -> error.toUserMessage()
                        }

                        else -> error.toUserMessage()
                    }
                    _loginState.value = LoginState.Error(message)
                }
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val usuario: Usuario) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
