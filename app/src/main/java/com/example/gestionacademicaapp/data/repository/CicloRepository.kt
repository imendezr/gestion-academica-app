package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.response.ApiResponse

class CicloRepository {

    suspend fun listar(context: Context): ApiResponse<List<Ciclo>> {
        return ApiClient.get(context, Endpoints.CICLOS_ALL, Array<Ciclo>::class.java).mapList()
    }

    suspend fun insertar(context: Context, ciclo: Ciclo): ApiResponse<Ciclo> {
        return ApiClient.post(context, Endpoints.CICLO_INSERT, ciclo, Ciclo::class.java)
    }

    suspend fun modificar(context: Context, ciclo: Ciclo): ApiResponse<Ciclo> {
        return ApiClient.put(context, Endpoints.CICLO_UPDATE, ciclo, Ciclo::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.cicloDelete(id.toInt()))
    }

    suspend fun buscarPorAnio(context: Context, anio: Int): ApiResponse<Ciclo> {
        return ApiClient.get(context, Endpoints.cicloByAnio(anio), Ciclo::class.java)
    }

    suspend fun activar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.post(context, Endpoints.cicloActivate(id.toInt()), Any(), Boolean::class.java)
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
