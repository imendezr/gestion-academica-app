package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.response.ApiResponse

class CursoRepository {

    suspend fun listar(context: Context): ApiResponse<List<Curso>> {
        return ApiClient.get(context, Endpoints.CURSOS_ALL, Array<Curso>::class.java).mapList()
    }

    suspend fun insertar(context: Context, curso: Curso): ApiResponse<Curso> {
        return ApiClient.post(context, Endpoints.CURSO_INSERT, curso, Curso::class.java)
    }

    suspend fun modificar(context: Context, curso: Curso): ApiResponse<Curso> {
        return ApiClient.put(context, Endpoints.CURSO_UPDATE, curso, Curso::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.cursoDelete(id.toInt()))
    }

    suspend fun buscarPorCodigo(context: Context, codigo: String): ApiResponse<Curso> {
        return ApiClient.get(context, Endpoints.cursoByCodigo(codigo), Curso::class.java)
    }

    suspend fun buscarPorNombre(context: Context, nombre: String): ApiResponse<Curso> {
        return ApiClient.get(context, Endpoints.cursoByNombre(nombre), Curso::class.java)
    }

    suspend fun buscarPorCarrera(context: Context, idCarrera: Long): ApiResponse<List<CursoDto>> {
        return ApiClient.get(
            context,
            Endpoints.cursosByCarrera(idCarrera),
            Array<CursoDto>::class.java
        ).mapList()
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
