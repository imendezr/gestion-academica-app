package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.response.ApiResponse

class GrupoRepository {

    suspend fun listar(context: Context): ApiResponse<List<Grupo>> {
        return ApiClient.get(context, Endpoints.GRUPOS_ALL, Array<Grupo>::class.java).mapList()
    }

    suspend fun insertar(context: Context, grupo: Grupo): ApiResponse<Grupo> {
        return ApiClient.post(context, Endpoints.GRUPO_INSERT, grupo, Grupo::class.java)
    }

    suspend fun modificar(context: Context, grupo: Grupo): ApiResponse<Grupo> {
        return ApiClient.put(context, Endpoints.GRUPO_UPDATE, grupo, Grupo::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.grupoDelete(id.toInt()))
    }

    suspend fun cursosPorCarreraYCiclo(context: Context, idCarrera: Long, idCiclo: Long): ApiResponse<List<CursoDto>> {
        return ApiClient.get(context, Endpoints.cursosByCarreraAndCiclo(idCarrera, idCiclo), Array<CursoDto>::class.java).mapList()
    }

    suspend fun gruposPorCarreraCurso(context: Context, idCarreraCurso: Long): ApiResponse<List<Grupo>> {
        return ApiClient.get(context, Endpoints.gruposByCarreraCurso(idCarreraCurso), Array<Grupo>::class.java).mapList()
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
