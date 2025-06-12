package com.example.gestionacademicaapp.data.repository

import android.util.Log
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.dao.CarreraCursoDao
import jakarta.inject.Inject
import retrofit2.HttpException

interface CarreraCursoRepository {
    suspend fun insertar(carreraCurso: CarreraCurso): Result<Unit>
    suspend fun modificar(carreraCurso: CarreraCurso): Result<Unit>
    suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit>
    suspend fun listar(): Result<List<CarreraCurso>>
    suspend fun buscarCursosPorCarreraYCiclo(idCarrera: Long, idCiclo: Long): Result<List<CursoDto>>
    suspend fun tieneGruposAsociados(idCarrera: Long, idCurso: Long): Result<Boolean>
}

class CarreraCursoRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : CarreraCursoRepository {
    override suspend fun insertar(carreraCurso: CarreraCurso): Result<Unit> = safeApiCall {
        Log.d("CarreraCursoRepository", "Insertando carreraCurso: $carreraCurso")
        val response = apiService.insertCarreraCurso(carreraCurso)
        Log.d("CarreraCursoRepository", "Respuesta de insertar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(carreraCurso: CarreraCurso): Result<Unit> = safeApiCall {
        Log.d("CarreraCursoRepository", "Modificando carreraCurso: $carreraCurso")
        val response = apiService.updateCarreraCurso(carreraCurso)
        Log.d("CarreraCursoRepository", "Respuesta de modificar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit> = safeApiCall {
        Log.d(
            "CarreraCursoRepository",
            "Eliminando carreraCurso: idCarrera=$idCarrera, idCurso=$idCurso"
        )
        val response = apiService.deleteCarreraCurso(idCarrera, idCurso)
        Log.d("CarreraCursoRepository", "Respuesta de eliminar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun listar(): Result<List<CarreraCurso>> = safeApiCall {
        Log.d("CarreraCursoRepository", "Listando relaciones carrera-curso")
        val response = apiService.getAllCarreraCurso()
        Log.d("CarreraCursoRepository", "Respuesta de listar: $response")
        response
    }

    override suspend fun buscarCursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        Log.d("CarreraCursoRepository", "Buscando cursos por carrera=$idCarrera, ciclo=$idCiclo")
        val response = apiService.getCursosByCarreraYCiclo(idCarrera, idCiclo)
        Log.d("CarreraCursoRepository", "Respuesta de buscarCursosPorCarreraYCiclo: $response")
        response
    }

    override suspend fun tieneGruposAsociados(idCarrera: Long, idCurso: Long): Result<Boolean> =
        safeApiCall {
            Log.d(
                "CarreraCursoRepository",
                "Verificando grupos asociados: idCarrera=$idCarrera, idCurso=$idCurso"
            )
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

class CarreraCursoRepositoryLocal @Inject constructor(
    private val carreraCursoDao: CarreraCursoDao
) : CarreraCursoRepository {
    override suspend fun insertar(carreraCurso: CarreraCurso): Result<Unit> = try {
        carreraCursoDao.insert(carreraCurso)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(carreraCurso: CarreraCurso): Result<Unit> = try {
        carreraCursoDao.update(carreraCurso)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit> = try {
        carreraCursoDao.delete(idCarrera, idCurso)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun listar(): Result<List<CarreraCurso>> = try {
        Result.success(carreraCursoDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarCursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = try {
        Result.success(carreraCursoDao.getCursosByCarreraYCiclo(idCarrera, idCiclo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun tieneGruposAsociados(idCarrera: Long, idCurso: Long): Result<Boolean> =
        try {
            Result.success(carreraCursoDao.tieneGruposAsociados(idCarrera, idCurso))
        } catch (e: Exception) {
            Result.failure(e)
        }
}

class CarreraCursoRepositoryImpl @Inject constructor(
    private val remote: CarreraCursoRepositoryRemote,
    private val local: CarreraCursoRepositoryLocal,
    private val isLocalMode: Boolean
) : CarreraCursoRepository {
    override suspend fun insertar(carreraCurso: CarreraCurso): Result<Unit> =
        if (isLocalMode) local.insertar(carreraCurso) else remote.insertar(carreraCurso)

    override suspend fun modificar(carreraCurso: CarreraCurso): Result<Unit> =
        if (isLocalMode) local.modificar(carreraCurso) else remote.modificar(carreraCurso)

    override suspend fun eliminar(idCarrera: Long, idCurso: Long): Result<Unit> =
        if (isLocalMode) local.eliminar(idCarrera, idCurso) else remote.eliminar(idCarrera, idCurso)

    override suspend fun listar(): Result<List<CarreraCurso>> =
        if (isLocalMode) local.listar() else remote.listar()

    override suspend fun buscarCursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = if (isLocalMode) local.buscarCursosPorCarreraYCiclo(
        idCarrera,
        idCiclo
    ) else remote.buscarCursosPorCarreraYCiclo(idCarrera, idCiclo)

    override suspend fun tieneGruposAsociados(idCarrera: Long, idCurso: Long): Result<Boolean> =
        if (isLocalMode) local.tieneGruposAsociados(
            idCarrera,
            idCurso
        ) else remote.tieneGruposAsociados(idCarrera, idCurso)
}
