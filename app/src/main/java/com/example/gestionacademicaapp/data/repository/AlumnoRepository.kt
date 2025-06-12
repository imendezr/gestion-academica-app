package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.dao.AlumnoDao
import com.example.gestionacademicaapp.utils.ConfigManager
import jakarta.inject.Inject
import retrofit2.HttpException


interface AlumnoRepository {
    suspend fun listar(): Result<List<Alumno>>
    suspend fun insertar(alumno: Alumno): Result<Unit>
    suspend fun modificar(alumno: Alumno): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun eliminarPorCedula(cedula: String): Result<Unit>
    suspend fun buscarPorId(idAlumno: Long): Result<Alumno>
    suspend fun buscarPorCedula(cedula: String): Result<Alumno>
    suspend fun buscarPorNombre(nombre: String): Result<Alumno>
    suspend fun buscarPorCarrera(idCarrera: Long): Result<List<Alumno>>
    suspend fun alumnosConOfertaEnCiclo(idCiclo: Long): Result<List<Alumno>>
}

class AlumnoRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : AlumnoRepository {
    override suspend fun listar(): Result<List<Alumno>> = safeApiCall {
        apiService.getAllAlumnos()
    }

    override suspend fun insertar(alumno: Alumno): Result<Unit> = safeApiCall {
        val response = apiService.insertAlumno(alumno)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(alumno: Alumno): Result<Unit> = safeApiCall {
        val response = apiService.updateAlumno(alumno)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteAlumno(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminarPorCedula(cedula: String): Result<Unit> = safeApiCall {
        val response = apiService.deleteAlumnoByCedula(cedula)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun buscarPorId(idAlumno: Long): Result<Alumno> = safeApiCall {
        apiService.getAlumnoById(idAlumno)
    }

    override suspend fun buscarPorCedula(cedula: String): Result<Alumno> = safeApiCall {
        apiService.getAlumnoByCedula(cedula)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Alumno> = safeApiCall {
        apiService.getAlumnoByNombre(nombre)
    }

    override suspend fun buscarPorCarrera(idCarrera: Long): Result<List<Alumno>> = safeApiCall {
        apiService.getAlumnosByCarrera(idCarrera)
    }

    override suspend fun alumnosConOfertaEnCiclo(idCiclo: Long): Result<List<Alumno>> =
        safeApiCall {
            apiService.getAlumnosConOfertaEnCiclo(idCiclo)
        }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AlumnoRepositoryLocal @Inject constructor(
    private val alumnoDao: AlumnoDao
) : AlumnoRepository {
    override suspend fun listar(): Result<List<Alumno>> = try {
        Result.success(alumnoDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(alumno: Alumno): Result<Unit> = try {
        alumnoDao.insert(alumno)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(alumno: Alumno): Result<Unit> = try {
        alumnoDao.update(alumno)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        alumnoDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminarPorCedula(cedula: String): Result<Unit> = try {
        alumnoDao.deleteByCedula(cedula)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorId(idAlumno: Long): Result<Alumno> = try {
        val alumno = alumnoDao.getById(idAlumno)
        if (alumno != null) Result.success(alumno) else Result.failure(NoSuchElementException("Alumno no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCedula(cedula: String): Result<Alumno> = try {
        val alumno = alumnoDao.getByCedula(cedula)
        if (alumno != null) Result.success(alumno) else Result.failure(NoSuchElementException("Alumno no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Alumno> = try {
        val alumno = alumnoDao.getByNombre(nombre)
        if (alumno != null) Result.success(alumno) else Result.failure(NoSuchElementException("Alumno no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCarrera(idCarrera: Long): Result<List<Alumno>> = try {
        Result.success(alumnoDao.getByCarrera(idCarrera))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun alumnosConOfertaEnCiclo(idCiclo: Long): Result<List<Alumno>> = try {
        Result.success(alumnoDao.getAlumnosConOfertaEnCiclo(idCiclo))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class AlumnoRepositoryImpl @Inject constructor(
    private val remote: AlumnoRepositoryRemote,
    private val local: AlumnoRepositoryLocal,
    private val configManager: ConfigManager
) : AlumnoRepository {
    override suspend fun listar(): Result<List<Alumno>> =
        if (configManager.isLocalMode()) local.listar() else remote.listar()

    override suspend fun insertar(alumno: Alumno): Result<Unit> =
        if (configManager.isLocalMode()) local.insertar(alumno) else remote.insertar(alumno)

    override suspend fun modificar(alumno: Alumno): Result<Unit> =
        if (configManager.isLocalMode()) local.modificar(alumno) else remote.modificar(alumno)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminar(id) else remote.eliminar(id)

    override suspend fun eliminarPorCedula(cedula: String): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminarPorCedula(cedula) else remote.eliminarPorCedula(
            cedula
        )

    override suspend fun buscarPorId(idAlumno: Long): Result<Alumno> =
        if (configManager.isLocalMode()) local.buscarPorId(idAlumno) else remote.buscarPorId(
            idAlumno
        )

    override suspend fun buscarPorCedula(cedula: String): Result<Alumno> =
        if (configManager.isLocalMode()) local.buscarPorCedula(cedula) else remote.buscarPorCedula(
            cedula
        )

    override suspend fun buscarPorNombre(nombre: String): Result<Alumno> =
        if (configManager.isLocalMode()) local.buscarPorNombre(nombre) else remote.buscarPorNombre(
            nombre
        )

    override suspend fun buscarPorCarrera(idCarrera: Long): Result<List<Alumno>> =
        if (configManager.isLocalMode()) local.buscarPorCarrera(idCarrera) else remote.buscarPorCarrera(
            idCarrera
        )

    override suspend fun alumnosConOfertaEnCiclo(idCiclo: Long): Result<List<Alumno>> =
        if (configManager.isLocalMode()) local.alumnosConOfertaEnCiclo(idCiclo) else remote.alumnosConOfertaEnCiclo(
            idCiclo
        )
}
