package com.example.gestionacademicaapp.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.data.response.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(cedula: String, clave: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val response = usuarioRepository.login(context, cedula, clave)
            when (response) {
                is ApiResponse.Success -> _loginState.value = LoginState.Success(response.data)
                is ApiResponse.Error -> {
                    val message = when (response.code) {
                        401 -> "Credenciales inválidas"
                        else -> response.message ?: "Error desconocido"
                    }
                    _loginState.value = LoginState.Error(message)
                }
            }
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val usuario: Usuario) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
