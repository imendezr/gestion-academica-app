package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import jakarta.inject.Inject
import retrofit2.HttpException

class MatriculaRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun insertar(matricula: Matricula): Result<Unit> = safeApiCall {
        println("Insertando matrícula: $matricula")
        val response = apiService.insertMatricula(matricula)
        println("Respuesta de insertar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun modificar(matricula: Matricula): Result<Unit> = safeApiCall {
        println("Modificando matrícula: $matricula")
        val response = apiService.updateMatricula(matricula)
        println("Respuesta de modificar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        println("Eliminando matrícula: id=$id")
        val response = apiService.deleteMatricula(id)
        println("Respuesta de eliminar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    suspend fun listarPorCedula(cedula: String): Result<List<MatriculaAlumnoDto>> = safeApiCall {
        println("Listando matrículas por cédula: $cedula")
        val response = apiService.getMatriculasPorCedula(cedula)
        println("Respuesta de listarPorCedula: $response")
        response
    }

    suspend fun listarPorAlumnoYCiclo(
        idAlumno: Long,
        idCiclo: Long
    ): Result<List<MatriculaAlumnoDto>> = safeApiCall {
        println("Listando matrículas para idAlumno=$idAlumno, idCiclo=$idCiclo")
        val response = apiService.getMatriculasPorAlumnoYCiclo(idAlumno, idCiclo)
        println("Respuesta de listarPorAlumnoYCiclo: size=${response.size}, data=$response")
        response
    }

    suspend fun listarPorGrupo(idGrupo: Long): Result<List<MatriculaAlumnoDto>> = safeApiCall {
        println("Listando matrículas por grupo: $idGrupo")
        val response = apiService.getMatriculasPorGrupo(idGrupo)
        println("Respuesta de listarPorGrupo: $response")
        response
    }

    suspend fun buscarPorId(idMatricula: Long): Result<Matricula> = safeApiCall {
        println("Buscando matrícula por id: $idMatricula")
        val response = apiService.getMatriculaById(idMatricula)
        println("Respuesta de buscarPorId: $response")
        response
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            println("Error en safeApiCall: ${e.message}")
            Result.failure(e)
        }
    }
}