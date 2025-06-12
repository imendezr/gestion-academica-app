package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.dao.ProfesorDao
import com.example.gestionacademicaapp.utils.ConfigManager
import jakarta.inject.Inject
import retrofit2.HttpException

interface ProfesorRepository {
    suspend fun listar(): Result<List<Profesor>>
    suspend fun insertar(profesor: Profesor): Result<Unit>
    suspend fun modificar(profesor: Profesor): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun eliminarPorCedula(cedula: String): Result<Unit>
    suspend fun buscarPorCedula(cedula: String): Result<Profesor>
    suspend fun buscarPorNombre(nombre: String): Result<Profesor>
}

class ProfesorRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : ProfesorRepository {
    override suspend fun listar(): Result<List<Profesor>> = safeApiCall {
        apiService.getAllProfesores()
    }

    override suspend fun insertar(profesor: Profesor): Result<Unit> = safeApiCall {
        val response = apiService.insertProfesor(profesor)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(profesor: Profesor): Result<Unit> = safeApiCall {
        val response = apiService.updateProfesor(profesor)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteProfesor(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminarPorCedula(cedula: String): Result<Unit> = safeApiCall {
        val response = apiService.deleteProfesorByCedula(cedula)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun buscarPorCedula(cedula: String): Result<Profesor> = safeApiCall {
        apiService.getProfesorByCedula(cedula)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Profesor> = safeApiCall {
        apiService.getProfesorByNombre(nombre)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ProfesorRepositoryLocal @Inject constructor(
    private val profesorDao: ProfesorDao
) : ProfesorRepository {
    override suspend fun listar(): Result<List<Profesor>> = try {
        Result.success(profesorDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(profesor: Profesor): Result<Unit> = try {
        profesorDao.insert(profesor)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(profesor: Profesor): Result<Unit> = try {
        profesorDao.update(profesor)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        profesorDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminarPorCedula(cedula: String): Result<Unit> = try {
        profesorDao.deleteByCedula(cedula)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCedula(cedula: String): Result<Profesor> = try {
        val profesor = profesorDao.getByCedula(cedula)
        if (profesor != null) Result.success(profesor) else Result.failure(NoSuchElementException("Profesor no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Profesor> = try {
        val profesor = profesorDao.getByNombre(nombre)
        if (profesor != null) Result.success(profesor) else Result.failure(NoSuchElementException("Profesor no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class ProfesorRepositoryImpl @Inject constructor(
    private val remote: ProfesorRepositoryRemote,
    private val local: ProfesorRepositoryLocal,
    private val configManager: ConfigManager
) : ProfesorRepository {
    override suspend fun listar(): Result<List<Profesor>> =
        if (configManager.isLocalMode()) local.listar() else remote.listar()

    override suspend fun insertar(profesor: Profesor): Result<Unit> =
        if (configManager.isLocalMode()) local.insertar(profesor) else remote.insertar(profesor)

    override suspend fun modificar(profesor: Profesor): Result<Unit> =
        if (configManager.isLocalMode()) local.modificar(profesor) else remote.modificar(profesor)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminar(id) else remote.eliminar(id)

    override suspend fun eliminarPorCedula(cedula: String): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminarPorCedula(cedula) else remote.eliminarPorCedula(
            cedula
        )

    override suspend fun buscarPorCedula(cedula: String): Result<Profesor> =
        if (configManager.isLocalMode()) local.buscarPorCedula(cedula) else remote.buscarPorCedula(
            cedula
        )

    override suspend fun buscarPorNombre(nombre: String): Result<Profesor> =
        if (configManager.isLocalMode()) local.buscarPorNombre(nombre) else remote.buscarPorNombre(
            nombre
        )
}
