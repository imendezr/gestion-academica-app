package com.example.gestionacademicaapp.data.repository

import android.util.Log
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.dao.CicloDao
import com.example.gestionacademicaapp.utils.ConfigManager
import jakarta.inject.Inject
import retrofit2.HttpException

interface CicloRepository {
    suspend fun listar(): Result<List<Ciclo>>
    suspend fun insertar(ciclo: Ciclo): Result<Unit>
    suspend fun modificar(ciclo: Ciclo): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun buscarPorAnio(anio: Long): Result<Ciclo>
    suspend fun activar(id: Long): Result<Unit>
    suspend fun obtenerPorId(id: Long): Result<Ciclo>
}

class CicloRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : CicloRepository {
    override suspend fun listar(): Result<List<Ciclo>> = safeApiCall {
        Log.d("CicloRepository", "Listando ciclos")
        val response = apiService.getAllCiclos()
        Log.d("CicloRepository", "Respuesta de listar: $response")
        response
    }

    override suspend fun insertar(ciclo: Ciclo): Result<Unit> = safeApiCall {
        val response = apiService.insertCiclo(ciclo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(ciclo: Ciclo): Result<Unit> = safeApiCall {
        val response = apiService.updateCiclo(ciclo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteCiclo(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun buscarPorAnio(anio: Long): Result<Ciclo> = safeApiCall {
        apiService.getCicloByAnio(anio)
    }

    override suspend fun activar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.activateCiclo(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun obtenerPorId(id: Long): Result<Ciclo> = safeApiCall {
        apiService.getCicloById(id)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Log.e("CicloRepository", "Error en safeApiCall: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class CicloRepositoryLocal @Inject constructor(
    private val cicloDao: CicloDao
) : CicloRepository {
    override suspend fun listar(): Result<List<Ciclo>> = try {
        Result.success(cicloDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(ciclo: Ciclo): Result<Unit> = try {
        cicloDao.insert(ciclo)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(ciclo: Ciclo): Result<Unit> = try {
        cicloDao.update(ciclo)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        cicloDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorAnio(anio: Long): Result<Ciclo> = try {
        val ciclo = cicloDao.getByAnio(anio)
        if (ciclo != null) Result.success(ciclo) else Result.failure(NoSuchElementException("Ciclo no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun activar(id: Long): Result<Unit> = try {
        cicloDao.activate(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun obtenerPorId(id: Long): Result<Ciclo> = try {
        val ciclo = cicloDao.getById(id)
        if (ciclo != null) Result.success(ciclo) else Result.failure(NoSuchElementException("Ciclo no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class CicloRepositoryImpl @Inject constructor(
    private val remote: CicloRepositoryRemote,
    private val local: CicloRepositoryLocal,
    private val configManager: ConfigManager
) : CicloRepository {
    override suspend fun listar(): Result<List<Ciclo>> =
        if (configManager.isLocalMode()) local.listar() else remote.listar()

    override suspend fun insertar(ciclo: Ciclo): Result<Unit> =
        if (configManager.isLocalMode()) local.insertar(ciclo) else remote.insertar(ciclo)

    override suspend fun modificar(ciclo: Ciclo): Result<Unit> =
        if (configManager.isLocalMode()) local.modificar(ciclo) else remote.modificar(ciclo)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminar(id) else remote.eliminar(id)

    override suspend fun buscarPorAnio(anio: Long): Result<Ciclo> =
        if (configManager.isLocalMode()) local.buscarPorAnio(anio) else remote.buscarPorAnio(anio)

    override suspend fun activar(id: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.activar(id) else remote.activar(id)

    override suspend fun obtenerPorId(id: Long): Result<Ciclo> =
        if (configManager.isLocalMode()) local.obtenerPorId(id) else remote.obtenerPorId(id)
}
