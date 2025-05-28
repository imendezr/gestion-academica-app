package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.response.ApiResponse

class ProfesorRepository {

    suspend fun listar(context: Context): ApiResponse<List<Profesor>> {
        return ApiClient.get(context, Endpoints.PROFESORES_ALL, Array<Profesor>::class.java).mapList()
    }

    suspend fun insertar(context: Context, profesor: Profesor): ApiResponse<Profesor> {
        return ApiClient.post(context, Endpoints.PROFESOR_INSERT, profesor, Profesor::class.java)
    }

    suspend fun modificar(context: Context, profesor: Profesor): ApiResponse<Profesor> {
        return ApiClient.put(context, Endpoints.PROFESOR_UPDATE, profesor, Profesor::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.profesorDelete(id.toInt()))
    }

    suspend fun buscarPorCedula(context: Context, cedula: String): ApiResponse<Profesor> {
        return ApiClient.get(context, Endpoints.profesorByCedula(cedula), Profesor::class.java)
    }

    suspend fun buscarPorNombre(context: Context, nombre: String): ApiResponse<Profesor> {
        return ApiClient.get(context, Endpoints.profesorByNombre(nombre), Profesor::class.java)
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
