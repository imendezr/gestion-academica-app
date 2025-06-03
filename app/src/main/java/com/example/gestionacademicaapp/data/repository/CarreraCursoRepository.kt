package com.example.gestionacademicaapp.data.repository

import android.util.Log
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import jakarta.inject.Inject
import retrofit2.HttpException

class CarreraCursoRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun insertar(carreraCurso: CarreraCurso): Result<Unit> = safeApiCall {
        Log.d("CarreraCursoRepository", "Insertando carreraCurso: $carreraCurso")
        val response = apiService.insertCarreraCurso(carreraCurso)
        Log.d("CarreraCursoRepository", "Respuesta de insertar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(carreraCurso: CarreraCurso): Result<Unit> = safeApiCall {
        Log.d("CarreraCursoRepository", "Modificando carreraCurso: $carreraCurso")
        val response = apiService.updateCarreraCurso(carreraCurso)
        Log.d("CarreraCursoRepository", "Respuesta de modificar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit> = safeApiCall {
        Log.d("CarreraCursoRepository", "Eliminando carreraCurso: idCarrera=$idCarrera, idCurso=$idCurso")
        val response = apiService.deleteCarreraCurso(idCarrera, idCurso)
        Log.d("CarreraCursoRepository", "Respuesta de eliminar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun listar(): Result<List<CarreraCurso>> = safeApiCall {
        Log.d("CarreraCursoRepository", "Listando relaciones carrera-curso")
        val response = apiService.getAllCarreraCurso()
        Log.d("CarreraCursoRepository", "Respuesta de listar: $response")
        response
    }

    suspend fun buscarCursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        Log.d("CarreraCursoRepository", "Buscando cursos por carrera=$idCarrera, ciclo=$idCiclo")
        val response = apiService.getCursosByCarreraYCiclo(idCarrera, idCiclo)
        Log.d("CarreraCursoRepository", "Respuesta de buscarCursosPorCarreraYCiclo: $response")
        response
    }

    suspend fun tieneGruposAsociados(idCarrera: Long, idCurso: Long): Result<Boolean> = safeApiCall {
        Log.d("CarreraCursoRepository", "Verificando grupos asociados: idCarrera=$idCarrera, idCurso=$idCurso")
        val response = apiService.tieneGruposAsociados(idCarrera, idCurso)
        Log.d("CarreraCursoRepository", "Respuesta de tieneGruposAsociados: $response")
        response
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Log.e("CarreraCursoRepository", "Error en safeApiCall: ${e.message}", e)
            Result.failure(e)
        }
    }
}
