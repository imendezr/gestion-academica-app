package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Usuario
import jakarta.inject.Inject
import retrofit2.HttpException

class UsuarioRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Usuario>> = safeApiCall {
        apiService.getAllUsuarios()
    }

    suspend fun insertar(usuario: Usuario): Result<Unit> = safeApiCall {
        val response = apiService.insertUsuario(usuario)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(usuario: Usuario): Result<Unit> = safeApiCall {
        val response = apiService.updateUsuario(usuario)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteUsuario(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorCedula(cedula: String): Result<Usuario> = safeApiCall {
        apiService.getUsuarioByCedula(cedula)
    }

    suspend fun login(cedula: String, clave: String): Result<Usuario> = safeApiCall {
        apiService.login(cedula, clave)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
