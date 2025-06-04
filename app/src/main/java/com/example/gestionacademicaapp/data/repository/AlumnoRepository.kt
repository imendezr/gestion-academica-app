package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Alumno
import jakarta.inject.Inject
import retrofit2.HttpException


class AlumnoRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun listar(): Result<List<Alumno>> = safeApiCall {
        apiService.getAllAlumnos()
    }

    suspend fun insertar(alumno: Alumno): Result<Unit> = safeApiCall {
        val response = apiService.insertAlumno(alumno)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(alumno: Alumno): Result<Unit> = safeApiCall {
        val response = apiService.updateAlumno(alumno)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteAlumno(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminarPorCedula(cedula: String): Result<Unit> = safeApiCall {
        val response = apiService.deleteAlumnoByCedula(cedula)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun buscarPorCedula(cedula: String): Result<Alumno> = safeApiCall {
        apiService.getAlumnoByCedula(cedula)
    }

    suspend fun buscarPorNombre(nombre: String): Result<Alumno> = safeApiCall {
        apiService.getAlumnoByNombre(nombre)
    }

    suspend fun buscarPorCarrera(idCarrera: Int): Result<List<Alumno>> = safeApiCall {
        apiService.getAlumnosByCarrera(idCarrera.toLong())
    }

    suspend fun alumnosConOfertaEnCiclo(idCiclo: Long): Result<List<Alumno>> = safeApiCall {
        apiService.getAlumnosConOfertaEnCiclo(idCiclo)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
