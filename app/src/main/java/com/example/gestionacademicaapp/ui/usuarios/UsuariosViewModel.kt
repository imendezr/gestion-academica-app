package com.example.gestionacademicaapp.ui.usuarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.ui.common.state.ListUiState
import com.example.gestionacademicaapp.ui.common.state.SingleUiState
import com.example.gestionacademicaapp.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _usuariosState = MutableLiveData<ListUiState<Usuario>>()
    val usuariosState: LiveData<ListUiState<Usuario>> get() = _usuariosState

    private val _actionState = MutableLiveData<SingleUiState<String>>()
    val actionState: LiveData<SingleUiState<String>> get() = _actionState

    var idUsuarioActual: Long = -1L

    init {
        fetchUsuarios()
    }

    private fun fetchUsuarios() {
        viewModelScope.launch {
            _usuariosState.value = ListUiState.Loading
            usuarioRepository.listar()
                .onSuccess { _usuariosState.value = ListUiState.Success(it) }
                .onFailure { _usuariosState.value = ListUiState.Error(it.toUserMessage()) }
        }
    }

    fun createUsuario(usuario: Usuario) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading
            usuarioRepository.insertar(usuario)
                .onSuccess {
                    fetchUsuarios()
                    _actionState.value = SingleUiState.Success("Usuario creado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun updateUsuario(usuario: Usuario) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading

            val usuarioFinal = if (usuario.idUsuario == idUsuarioActual) {
                usuario.copy(clave = "")
            } else if (usuario.clave?.isBlank() != false) {
                usuario.copy(clave = "")
            } else {
                usuario
            }

            usuarioRepository.modificar(usuarioFinal)
                .onSuccess {
                    fetchUsuarios()
                    _actionState.value = SingleUiState.Success("Usuario actualizado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun deleteUsuario(id: Long) {
        viewModelScope.launch {
            _actionState.value = SingleUiState.Loading

            if (id == idUsuarioActual) {
                _actionState.value =
                    SingleUiState.Error("No puedes eliminar tu propia cuenta mientras estás conectado.")
                return@launch
            }

            usuarioRepository.eliminar(id)
                .onSuccess {
                    fetchUsuarios()
                    _actionState.value = SingleUiState.Success("Usuario eliminado exitosamente")
                }
                .onFailure {
                    _actionState.value = SingleUiState.Error(it.toUserMessage())
                }
        }
    }

    fun validarClaveActual(cedula: String, clave: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            usuarioRepository.login(cedula, clave)
                .onSuccess { callback(true) }
                .onFailure { callback(false) }
        }
    }

    fun searchUsuarioByCedula(cedula: String) {
        viewModelScope.launch {
            _usuariosState.value = ListUiState.Loading
            usuarioRepository.buscarPorCedula(cedula)
                .onSuccess { usuario ->
                    _usuariosState.value = ListUiState.Success(listOf(usuario))
                }
                .onFailure {
                    _usuariosState.value = ListUiState.Error(it.toUserMessage())
                }
        }
    }
}
