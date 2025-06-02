package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Ciclo
import jakarta.inject.Inject
import retrofit2.HttpException

class CicloRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Ciclo>> = safeApiCall {
        apiService.getAllCiclos()
    }

    suspend fun insertar(ciclo: Ciclo): Result<Unit> = safeApiCall {
        val response = apiService.insertCiclo(ciclo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(ciclo: Ciclo): Result<Unit> = safeApiCall {
        val response = apiService.updateCiclo(ciclo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteCiclo(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorAnio(anio: Long): Result<Ciclo> = safeApiCall {
        apiService.getCicloByAnio(anio)
    }

    suspend fun activar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.activateCiclo(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun obtenerPorId(id: Long): Result<Ciclo> = safeApiCall {
        apiService.getCicloById(id)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
