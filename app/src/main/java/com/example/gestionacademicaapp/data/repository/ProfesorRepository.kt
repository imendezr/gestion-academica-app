package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Profesor
import jakarta.inject.Inject
import retrofit2.HttpException

class ProfesorRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Profesor>> = safeApiCall {
        apiService.getAllProfesores()
    }

    suspend fun insertar(profesor: Profesor): Result<Unit> = safeApiCall {
        val response = apiService.insertProfesor(profesor)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(profesor: Profesor): Result<Unit> = safeApiCall {
        val response = apiService.updateProfesor(profesor)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteProfesor(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminarPorCedula(cedula: String): Result<Unit> = safeApiCall {
        val response = apiService.deleteProfesorByCedula(cedula)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorCedula(cedula: String): Result<Profesor> = safeApiCall {
        apiService.getProfesorByCedula(cedula)
    }

    suspend fun buscarPorNombre(nombre: String): Result<Profesor> = safeApiCall {
        apiService.getProfesorByNombre(nombre)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
