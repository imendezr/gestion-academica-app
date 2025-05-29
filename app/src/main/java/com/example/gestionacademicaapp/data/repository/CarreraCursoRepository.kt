package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import jakarta.inject.Inject
import retrofit2.HttpException

class CarreraCursoRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun insertar(idCarrera: Long, idCurso: Long, idCiclo: Long): Result<Unit> =
        safeApiCall {
            val response = apiService.addCursoToCarrera(idCarrera, idCurso, idCiclo)
            if (response.isSuccessful) Unit else throw HttpException(response)
        }

    suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit> =
        safeApiCall {
            val response = apiService.removeCursoFromCarrera(idCarrera, idCurso)
            if (response.isSuccessful) Unit else throw HttpException(response)
        }

    suspend fun modificarOrden(idCarrera: Long, idCurso: Long, nuevoIdCiclo: Long): Result<Unit> =
        safeApiCall {
            val response = apiService.updateCursoOrden(idCarrera, idCurso, nuevoIdCiclo)
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
