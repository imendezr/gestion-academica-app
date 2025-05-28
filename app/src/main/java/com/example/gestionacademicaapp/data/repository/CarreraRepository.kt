package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.response.ApiResponse

class CarreraRepository {

    suspend fun listar(context: Context): ApiResponse<List<Carrera>> {
        return ApiClient.get(context, Endpoints.CARRERAS_ALL, Array<Carrera>::class.java).mapList()
    }

    suspend fun insertar(context: Context, carrera: Carrera): ApiResponse<Carrera> {
        return ApiClient.post(context, Endpoints.CARRERA_INSERT, carrera, Carrera::class.java)
    }

    suspend fun modificar(context: Context, carrera: Carrera): ApiResponse<Carrera> {
        return ApiClient.put(context, Endpoints.CARRERA_UPDATE, carrera, Carrera::class.java)
    }

    suspend fun eliminar(context: Context, id: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.carreraDelete(id.toInt()))
    }

    suspend fun buscarPorCodigo(context: Context, codigo: String): ApiResponse<Carrera> {
        return ApiClient.get(context, Endpoints.carreraByCodigo(codigo), Carrera::class.java)
    }

    suspend fun buscarPorNombre(context: Context, nombre: String): ApiResponse<Carrera> {
        return ApiClient.get(context, Endpoints.carreraByNombre(nombre), Carrera::class.java)
    }

    suspend fun agregarCurso(context: Context, idCarrera: Int, idCurso: Int, idCiclo: Int): ApiResponse<CarreraCurso> {
        return ApiClient.post(context, Endpoints.addCursoToCarrera(idCarrera, idCurso, idCiclo), Any(), CarreraCurso::class.java)
    }

    suspend fun eliminarCurso(context: Context, idCarrera: Int, idCurso: Int): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.removeCursoFromCarrera(idCarrera, idCurso))
    }

    suspend fun actualizarOrdenCurso(context: Context, idCarrera: Int, idCurso: Int, nuevoIdCiclo: Int): ApiResponse<CarreraCurso> {
        return ApiClient.put(context, Endpoints.updateCursoOrden(idCarrera, idCurso, nuevoIdCiclo), Any(), CarreraCurso::class.java)
    }

    private inline fun <reified T> ApiResponse<Array<T>>.mapList(): ApiResponse<List<T>> {
        return when (this) {
            is ApiResponse.Success -> ApiResponse.success(this.data.toList())
            is ApiResponse.Error -> this
        }
    }
}
