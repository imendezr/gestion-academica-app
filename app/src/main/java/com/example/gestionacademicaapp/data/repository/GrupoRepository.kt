package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import jakarta.inject.Inject
import retrofit2.HttpException

class GrupoRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Grupo>> = safeApiCall {
        apiService.getAllGrupos()
    }

    suspend fun insertar(grupo: Grupo): Result<Unit> = safeApiCall {
        val response = apiService.insertGrupo(grupo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(grupo: Grupo): Result<Unit> = safeApiCall {
        val response = apiService.updateGrupo(grupo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteGrupo(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    /*suspend fun cursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        apiService.getCursosByCarreraAndCiclo(idCarrera, idCiclo)
    }*/

    suspend fun gruposPorCarreraCurso(idCarrera: Long, idCurso: Long): Result<List<GrupoDto>> =
        safeApiCall {
            apiService.getGruposByCarreraCurso(idCarrera, idCurso)
        }

    suspend fun gruposPorCursoCicloCarrera(
        idCurso: Long,
        idCiclo: Long,
        idCarrera: Long
    ): Result<List<GrupoDto>> = safeApiCall {
        apiService.getGruposByCursoCicloCarrera(idCurso, idCiclo, idCarrera)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
