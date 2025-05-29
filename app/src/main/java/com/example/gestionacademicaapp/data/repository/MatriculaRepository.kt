package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Matricula
import jakarta.inject.Inject
import retrofit2.HttpException

class MatriculaRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Matricula>> = safeApiCall {
        apiService.getAllMatriculas()
    }

    suspend fun insertar(matricula: Matricula): Result<Unit> = safeApiCall {
        val response = apiService.insertMatricula(matricula)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(matricula: Matricula): Result<Unit> = safeApiCall {
        val response = apiService.updateMatricula(matricula)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteMatricula(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
