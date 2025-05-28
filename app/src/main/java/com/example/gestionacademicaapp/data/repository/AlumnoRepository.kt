package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.data.response.ApiResponse


class AlumnoRepository {

    suspend fun listar(context: Context): ApiResponse<List<Alumno>> {
        return ApiClient.get(context, Endpoints.ALUMNOS_ALL, Array<Alumno>::class.java).mapList()
    }

    suspend fun insertar(context: Context, alumno: Alumno): ApiResponse<Alumno> {
        return ApiClient.post(context, Endpoints.ALUMNO_INSERT, alumno, Alumno::class.java)
    }

    suspend fun modificar(context: Context, alumno: Alumno): ApiResponse<Alumno> {
        return ApiClient.put(context, Endpoints.ALUMNO_UPDATE, alumno, Alumno::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.alumnoDelete(id.toInt()))
    }

    suspend fun buscarPorCedula(context: Context, cedula: String): ApiResponse<Alumno> {
        return ApiClient.get(context, Endpoints.alumnoByCedula(cedula), Alumno::class.java)
    }

    suspend fun buscarPorNombre(context: Context, nombre: String): ApiResponse<Alumno> {
        return ApiClient.get(context, Endpoints.alumnoByNombre(nombre), Alumno::class.java)
    }

    suspend fun buscarPorCarrera(context: Context, idCarrera: Int): ApiResponse<List<Alumno>> {
        return ApiClient.get(context, Endpoints.alumnosByCarrera(idCarrera), Array<Alumno>::class.java).mapList()
    }

    suspend fun historialAlumno(context: Context, idAlumno: Int): ApiResponse<List<MatriculaAlumnoDto>> {
        return ApiClient.get(context, Endpoints.alumnoHistorial(idAlumno), Array<MatriculaAlumnoDto>::class.java).mapList()
    }

    // Utilidad para convertir Array<T> en List<T> cuando se usa ApiClient con Array.class
    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
