package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import jakarta.inject.Inject
import retrofit2.HttpException

class CarreraCursoRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun insertar(carreraCurso: CarreraCurso): Result<Unit> = safeApiCall {
        val response = apiService.insertCarreraCurso(carreraCurso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(carreraCurso: CarreraCurso): Result<Unit> = safeApiCall {
        val response = apiService.updateCarreraCurso(carreraCurso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteCarreraCurso(idCarrera, idCurso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun listar(): Result<List<CarreraCurso>> = safeApiCall {
        apiService.getAllCarreraCurso()
    }

    suspend fun buscarCursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        apiService.getCursosByCarreraYCiclo(idCarrera, idCiclo)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
