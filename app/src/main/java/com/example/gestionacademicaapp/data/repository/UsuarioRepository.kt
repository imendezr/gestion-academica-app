package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.response.ApiResponse

class UsuarioRepository {
    suspend fun listar(context: Context): ApiResponse<List<Usuario>> {
        return ApiClient.get(context, Endpoints.USUARIOS_ALL, Array<Usuario>::class.java).mapList()
    }

    suspend fun insertar(context: Context, usuario: Usuario): ApiResponse<Usuario> {
        return ApiClient.post(context, Endpoints.USUARIO_INSERT, usuario, Usuario::class.java)
    }

    suspend fun modificar(context: Context, usuario: Usuario): ApiResponse<Usuario> {
        return ApiClient.put(context, Endpoints.USUARIO_UPDATE, usuario, Usuario::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.usuarioDelete(id.toInt()))
    }

    suspend fun buscarPorCedula(context: Context, cedula: String): ApiResponse<Usuario> {
        return ApiClient.get(context, Endpoints.usuarioByCedula(cedula), Usuario::class.java)
    }

    suspend fun login(context: Context, cedula: String, clave: String): ApiResponse<Usuario> {
        return ApiClient.post(context, Endpoints.login(cedula, clave), Any(), Usuario::class.java)
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
