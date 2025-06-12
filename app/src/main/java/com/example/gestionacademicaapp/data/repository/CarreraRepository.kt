package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.dao.CarreraCursoDao
import com.example.gestionacademicaapp.data.dao.CarreraDao
import jakarta.inject.Inject
import retrofit2.HttpException

interface CarreraRepository {
    suspend fun listar(): Result<List<Carrera>>
    suspend fun insertar(carrera: Carrera): Result<Unit>
    suspend fun modificar(carrera: Carrera): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun buscarPorCodigo(codigo: String): Result<Carrera>
    suspend fun buscarPorNombre(nombre: String): Result<Carrera>
    suspend fun agregarCurso(idCarrera: Long, idCurso: Long, idCiclo: Long): Result<Unit>
    suspend fun eliminarCurso(idCarrera: Long, idCurso: Long): Result<Unit>
    suspend fun actualizarOrdenCurso(
        idCarrera: Long,
        idCurso: Long,
        nuevoIdCiclo: Long
    ): Result<Unit>
}

class CarreraRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : CarreraRepository {
    override suspend fun listar(): Result<List<Carrera>> = safeApiCall {
        apiService.getAllCarreras()
    }

    override suspend fun insertar(carrera: Carrera): Result<Unit> = safeApiCall {
        val response = apiService.insertCarrera(carrera)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(carrera: Carrera): Result<Unit> = safeApiCall {
        val response = apiService.updateCarrera(carrera)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteCarrera(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun buscarPorCodigo(codigo: String): Result<Carrera> = safeApiCall {
        apiService.getCarreraByCodigo(codigo)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Carrera> = safeApiCall {
        apiService.getCarreraByNombre(nombre)
    }

    override suspend fun agregarCurso(idCarrera: Long, idCurso: Long, idCiclo: Long): Result<Unit> =
        safeApiCall {
            val response = apiService.addCursoToCarrera(idCarrera, idCurso, idCiclo)
            if (response.isSuccessful) Unit else throw HttpException(response)
        }

    override suspend fun eliminarCurso(idCarrera: Long, idCurso: Long): Result<Unit> = safeApiCall {
        val response = apiService.removeCursoFromCarrera(idCarrera, idCurso)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun actualizarOrdenCurso(
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

class CarreraRepositoryLocal @Inject constructor(
    private val carreraDao: CarreraDao,
    private val carreraCursoDao: CarreraCursoDao
) : CarreraRepository {
    override suspend fun listar(): Result<List<Carrera>> = try {
        Result.success(carreraDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(carrera: Carrera): Result<Unit> = try {
        carreraDao.insert(carrera)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(carrera: Carrera): Result<Unit> = try {
        carreraDao.update(carrera)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        carreraDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCodigo(codigo: String): Result<Carrera> = try {
        val carrera = carreraDao.getByCodigo(codigo)
        if (carrera != null) Result.success(carrera) else Result.failure(NoSuchElementException("Carrera no encontrada"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Carrera> = try {
        val carrera = carreraDao.getByNombre(nombre)
        if (carrera != null) Result.success(carrera) else Result.failure(NoSuchElementException("Carrera no encontrada"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun agregarCurso(idCarrera: Long, idCurso: Long, idCiclo: Long): Result<Unit> =
        try {
            carreraCursoDao.insert(CarreraCurso(0, idCarrera, idCurso, idCiclo))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun eliminarCurso(idCarrera: Long, idCurso: Long): Result<Unit> = try {
        carreraCursoDao.delete(idCarrera, idCurso)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun actualizarOrdenCurso(
        idCarrera: Long,
        idCurso: Long,
        nuevoIdCiclo: Long
    ): Result<Unit> = try {
        val carreraCurso =
            carreraCursoDao.getAll().find { it.pkCarrera == idCarrera && it.pkCurso == idCurso }
        if (carreraCurso != null) {
            carreraCursoDao.update(carreraCurso.copy(pkCiclo = nuevoIdCiclo))
            Result.success(Unit)
        } else {
            Result.failure(NoSuchElementException("Relación carrera-curso no encontrada"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class CarreraRepositoryImpl @Inject constructor(
    private val remote: CarreraRepositoryRemote,
    private val local: CarreraRepositoryLocal,
    private val isLocalMode: Boolean
) : CarreraRepository {
    override suspend fun listar(): Result<List<Carrera>> =
        if (isLocalMode) local.listar() else remote.listar()

    override suspend fun insertar(carrera: Carrera): Result<Unit> =
        if (isLocalMode) local.insertar(carrera) else remote.insertar(carrera)

    override suspend fun modificar(carrera: Carrera): Result<Unit> =
        if (isLocalMode) local.modificar(carrera) else remote.modificar(carrera)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (isLocalMode) local.eliminar(id) else remote.eliminar(id)

    override suspend fun buscarPorCodigo(codigo: String): Result<Carrera> =
        if (isLocalMode) local.buscarPorCodigo(codigo) else remote.buscarPorCodigo(codigo)

    override suspend fun buscarPorNombre(nombre: String): Result<Carrera> =
        if (isLocalMode) local.buscarPorNombre(nombre) else remote.buscarPorNombre(nombre)

    override suspend fun agregarCurso(idCarrera: Long, idCurso: Long, idCiclo: Long): Result<Unit> =
        if (isLocalMode) local.agregarCurso(idCarrera, idCurso, idCiclo) else remote.agregarCurso(
            idCarrera,
            idCurso,
            idCiclo
        )

    override suspend fun eliminarCurso(idCarrera: Long, idCurso: Long): Result<Unit> =
        if (isLocalMode) local.eliminarCurso(
            idCarrera,
            idCurso
        ) else remote.eliminarCurso(idCarrera, idCurso)

    override suspend fun actualizarOrdenCurso(
        idCarrera: Long,
        idCurso: Long,
        nuevoIdCiclo: Long
    ): Result<Unit> = if (isLocalMode) local.actualizarOrdenCurso(
        idCarrera,
        idCurso,
        nuevoIdCiclo
    ) else remote.actualizarOrdenCurso(idCarrera, idCurso, nuevoIdCiclo)
}
