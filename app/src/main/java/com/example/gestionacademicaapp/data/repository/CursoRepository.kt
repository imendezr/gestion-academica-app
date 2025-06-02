package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import jakarta.inject.Inject
import retrofit2.HttpException

class CursoRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Curso>> = safeApiCall {
        apiService.getAllCursos()
    }

    suspend fun insertar(curso: Curso): Result<Unit> = safeApiCall {
        val response = apiService.insertCurso(curso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(curso: Curso): Result<Unit> = safeApiCall {
        val response = apiService.updateCurso(curso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteCurso(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorCodigo(codigo: String): Result<Curso> = safeApiCall {
        apiService.getCursoByCodigo(codigo)
    }

    suspend fun buscarPorNombre(nombre: String): Result<Curso> = safeApiCall {
        apiService.getCursoByNombre(nombre)
    }

    suspend fun buscarPorCarrera(idCarrera: Long): Result<List<CursoDto>> = safeApiCall {
        apiService.getCursosByCarrera(idCarrera)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        apiService.getCursosByCarreraYCiclo(idCarrera, idCiclo)
    }

    suspend fun buscarPorCiclo(
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        apiService.getCursosByCiclo(idCiclo)
    }
}
