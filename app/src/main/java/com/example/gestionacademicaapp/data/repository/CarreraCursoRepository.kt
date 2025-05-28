package com.example.gestionacademicaapp.data.repository

import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.api.Endpoints
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.response.ApiResponse

class CarreraCursoRepository {

    suspend fun insertar(context: Context, idCarrera: Long, idCurso: Long, idCiclo: Long): ApiResponse<CarreraCurso> {
        return ApiClient.post(context, Endpoints.addCursoToCarrera(idCarrera.toInt(), idCurso.toInt(), idCiclo.toInt()), Any(), CarreraCurso::class.java)
    }

    suspend fun eliminar(context: Context, idCarrera: Long, idCurso: Long): ApiResponse<Boolean> {
        return ApiClient.delete(context, Endpoints.removeCursoFromCarrera(idCarrera.toInt(), idCurso.toInt()))
    }

    suspend fun modificarOrden(context: Context, idCarrera: Long, idCurso: Long, nuevoIdCiclo: Long): ApiResponse<CarreraCurso> {
        return ApiClient.put(context, Endpoints.updateCursoOrden(idCarrera.toInt(), idCurso.toInt(), nuevoIdCiclo.toInt()), Any(), CarreraCurso::class.java)
    }
}
