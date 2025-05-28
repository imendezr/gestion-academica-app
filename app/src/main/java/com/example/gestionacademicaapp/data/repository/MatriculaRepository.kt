package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.response.ApiResponse

class MatriculaRepository {

    suspend fun listar(context: Context): ApiResponse<List<Matricula>> {
        return ApiClient.get(context, Endpoints.MATRICULAS_ALL, Array<Matricula>::class.java).mapList()
    }

    suspend fun insertar(context: Context, matricula: Matricula): ApiResponse<Matricula> {
        return ApiClient.post(context, Endpoints.MATRICULA_INSERT, matricula, Matricula::class.java)
    }

    suspend fun modificar(context: Context, matricula: Matricula): ApiResponse<Matricula> {
        return ApiClient.put(context, Endpoints.MATRICULA_UPDATE, matricula, Matricula::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.matriculaDelete(id.toInt()))
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
