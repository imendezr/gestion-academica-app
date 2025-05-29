package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Carrera
import jakarta.inject.Inject
import retrofit2.HttpException

class CarreraRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Carrera>> = safeApiCall {
        apiService.getAllCarreras()
    }

    suspend fun insertar(carrera: Carrera): Result<Unit> = safeApiCall {
        val response = apiService.insertCarrera(carrera)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(carrera: Carrera): Result<Unit> = safeApiCall {
        val response = apiService.updateCarrera(carrera)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteCarrera(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorCodigo(codigo: String): Result<Carrera> = safeApiCall {
        apiService.getCarreraByCodigo(codigo)
    }

    suspend fun buscarPorNombre(nombre: String): Result<Carrera> = safeApiCall {
        apiService.getCarreraByNombre(nombre)
    }

    suspend fun agregarCurso(
        idCarrera: Long,
        idCurso: Long,
        idCiclo: Long
    ): Result<Unit> = safeApiCall {
        val response = apiService.addCursoToCarrera(idCarrera, idCurso, idCiclo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminarCurso(idCarrera: Long, idCurso: Long): Result<Unit> = safeApiCall {
        val response = apiService.removeCursoFromCarrera(idCarrera, idCurso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun actualizarOrdenCurso(
        idCarrera: Long,
        idCurso: Long,
        nuevoIdCiclo: Long
    ): Result<Unit> = safeApiCall {
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
