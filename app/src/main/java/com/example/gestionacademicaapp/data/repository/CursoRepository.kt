package com.example.gestionacademicaapp.data.repository

import android.util.Log
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import jakarta.inject.Inject
import retrofit2.HttpException

class CursoRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun listar(): Result<List<Curso>> = safeApiCall {
        Log.d("CursoRepository", "Listando cursos")
        val response = apiService.getAllCursos()
        Log.d("CursoRepository", "Respuesta de listar: $response")
        response
    }

    suspend fun insertar(curso: Curso): Result<Unit> = safeApiCall {
        Log.d("CursoRepository", "Insertando curso: $curso")
        val response = apiService.insertCurso(curso)
        Log.d("CursoRepository", "Respuesta de insertar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(curso: Curso): Result<Unit> = safeApiCall {
        Log.d("CursoRepository", "Modificando curso: $curso")
        val response = apiService.updateCurso(curso)
        Log.d("CursoRepository", "Respuesta de modificar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        Log.d("CursoRepository", "Eliminando curso: id=$id")
        val response = apiService.deleteCurso(id)
        Log.d("CursoRepository", "Respuesta de eliminar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorCodigo(codigo: String): Result<Curso> = safeApiCall {
        Log.d("CursoRepository", "Buscando curso por codigo: $codigo")
        val response = apiService.getCursoByCodigo(codigo)
        Log.d("CursoRepository", "Respuesta de buscarPorCodigo: $response")
        response
    }

    suspend fun buscarPorNombre(nombre: String): Result<Curso> = safeApiCall {
        Log.d("CursoRepository", "Buscando curso por nombre: $nombre")
        val response = apiService.getCursoByNombre(nombre)
        Log.d("CursoRepository", "Respuesta de buscarPorNombre: $response")
        response
    }

    suspend fun buscarPorCarrera(idCarrera: Long): Result<List<CursoDto>> = safeApiCall {
        Log.d("CursoRepository", "Buscando cursos por carrera: idCarrera=$idCarrera")
        val response = apiService.getCursosByCarrera(idCarrera)
        Log.d("CursoRepository", "Respuesta de buscarPorCarrera: $response")
        response
    }

    suspend fun buscarPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        Log.d("CursoRepository", "Buscando cursos por carrera=$idCarrera, ciclo=$idCiclo")
        val response = apiService.getCursosByCarreraYCiclo(idCarrera, idCiclo)
        Log.d("CursoRepository", "Respuesta de buscarPorCarreraYCiclo: $response")
        response
    }

    suspend fun buscarPorCiclo(
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        Log.d("CursoRepository", "Buscando cursos por ciclo: idCiclo=$idCiclo")
        val response = apiService.getCursosByCiclo(idCiclo)
        Log.d("CursoRepository", "Respuesta de buscarPorCiclo: $response")
        response
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Log.e("CursoRepository", "Error en safeApiCall: ${e.message}", e)
            Result.failure(e)
        }
    }
}
