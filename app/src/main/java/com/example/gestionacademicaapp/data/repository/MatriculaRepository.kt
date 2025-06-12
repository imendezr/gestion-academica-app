package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import com.example.gestionacademicaapp.data.dao.MatriculaDao
import com.example.gestionacademicaapp.utils.ConfigManager
import jakarta.inject.Inject
import retrofit2.HttpException

interface MatriculaRepository {
    suspend fun insertar(matricula: Matricula): Result<Unit>
    suspend fun modificar(matricula: Matricula): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun listarPorCedula(cedula: String): Result<List<MatriculaAlumnoDto>>
    suspend fun listarPorAlumnoYCiclo(
        idAlumno: Long,
        idCiclo: Long
    ): Result<List<MatriculaAlumnoDto>>

    suspend fun listarPorGrupo(idGrupo: Long): Result<List<MatriculaAlumnoDto>>
    suspend fun buscarPorId(idMatricula: Long): Result<Matricula>
    suspend fun existeMatriculaPorAlumnoYGrupo(idAlumno: Long, idGrupo: Long): Result<Boolean>
    suspend fun modificarGrupoMatricula(idMatricula: Long, idGrupo: Long): Result<Unit>
    suspend fun buscarMatriculaPorGrupo(grupoId: Long): Result<Matricula>
}

class MatriculaRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : MatriculaRepository {
    override suspend fun insertar(matricula: Matricula): Result<Unit> = safeApiCall {
        println("Insertando matrícula: $matricula")
        val response = apiService.insertMatricula(matricula)
        println("Respuesta de insertar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(matricula: Matricula): Result<Unit> = safeApiCall {
        println("Modificando matrícula: $matricula")
        val response = apiService.updateMatricula(matricula)
        println("Respuesta de modificar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        println("Eliminando matrícula: id=$id")
        val response = apiService.deleteMatricula(id)
        println("Respuesta de eliminar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun listarPorCedula(cedula: String): Result<List<MatriculaAlumnoDto>> =
        safeApiCall {
            println("Listando matrículas por cédula: $cedula")
            val response = apiService.getMatriculasPorCedula(cedula)
            println("Respuesta de listarPorCedula: $response")
            response
        }

    override suspend fun listarPorAlumnoYCiclo(
        idAlumno: Long,
        idCiclo: Long
    ): Result<List<MatriculaAlumnoDto>> = safeApiCall {
        println("Listando matrículas para idAlumno=$idAlumno, idCiclo=$idCiclo")
        val response = apiService.getMatriculasPorAlumnoYCiclo(idAlumno, idCiclo)
        println("Respuesta de listarPorAlumnoYCiclo: size=${response.size}, data=$response")
        response
    }

    override suspend fun listarPorGrupo(idGrupo: Long): Result<List<MatriculaAlumnoDto>> =
        safeApiCall {
            println("Listando matrículas por grupo: $idGrupo")
            val response = apiService.getMatriculasPorGrupo(idGrupo)
            println("Respuesta de listarPorGrupo: $response")
            response
        }

    override suspend fun buscarPorId(idMatricula: Long): Result<Matricula> = safeApiCall {
        println("Buscando matrícula por id: $idMatricula")
        val response = apiService.getMatriculaById(idMatricula)
        println("Respuesta de buscarPorId: $response")
        response
    }

    override suspend fun existeMatriculaPorAlumnoYGrupo(
        idAlumno: Long,
        idGrupo: Long
    ): Result<Boolean> = safeApiCall {
        apiService.checkMatriculaExists(idAlumno, idGrupo)
    }

    override suspend fun modificarGrupoMatricula(idMatricula: Long, idGrupo: Long): Result<Unit> =
        safeApiCall {
            val response = apiService.updateMatriculaGrupo(idMatricula, idGrupo)
            if (response.isSuccessful) Unit else throw HttpException(response)
        }

    override suspend fun buscarMatriculaPorGrupo(grupoId: Long): Result<Matricula> = safeApiCall {
        apiService.getMatriculaByGrupoId(grupoId)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            println("Error en safeApiCall: ${e.message}")
            Result.failure(e)
        }
    }
}

class MatriculaRepositoryLocal @Inject constructor(
    private val matriculaDao: MatriculaDao
) : MatriculaRepository {
    override suspend fun insertar(matricula: Matricula): Result<Unit> = try {
        matriculaDao.insert(matricula)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(matricula: Matricula): Result<Unit> = try {
        matriculaDao.update(matricula)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        matriculaDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun listarPorCedula(cedula: String): Result<List<MatriculaAlumnoDto>> = try {
        Result.success(matriculaDao.getMatriculasPorCedula(cedula))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun listarPorAlumnoYCiclo(
        idAlumno: Long,
        idCiclo: Long
    ): Result<List<MatriculaAlumnoDto>> = try {
        Result.success(matriculaDao.getMatriculasPorAlumnoYCiclo(idAlumno, idCiclo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun listarPorGrupo(idGrupo: Long): Result<List<MatriculaAlumnoDto>> = try {
        Result.success(matriculaDao.getMatriculasPorGrupo(idGrupo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorId(idMatricula: Long): Result<Matricula> = try {
        val matricula = matriculaDao.getById(idMatricula)
        if (matricula != null) Result.success(matricula) else Result.failure(
            NoSuchElementException(
                "Matrícula no encontrada"
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun existeMatriculaPorAlumnoYGrupo(
        idAlumno: Long,
        idGrupo: Long
    ): Result<Boolean> = try {
        Result.success(matriculaDao.checkMatriculaExists(idAlumno, idGrupo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificarGrupoMatricula(idMatricula: Long, idGrupo: Long): Result<Unit> =
        try {
            matriculaDao.updateMatriculaGrupo(idMatricula, idGrupo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun buscarMatriculaPorGrupo(grupoId: Long): Result<Matricula> = try {
        val matricula = matriculaDao.getByGrupoId(grupoId)
        if (matricula != null) Result.success(matricula) else Result.failure(
            NoSuchElementException(
                "Matrícula no encontrada"
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class MatriculaRepositoryImpl @Inject constructor(
    private val remote: MatriculaRepositoryRemote,
    private val local: MatriculaRepositoryLocal,
    private val configManager: ConfigManager
) : MatriculaRepository {
    override suspend fun insertar(matricula: Matricula): Result<Unit> =
        if (configManager.isLocalMode()) local.insertar(matricula) else remote.insertar(matricula)

    override suspend fun modificar(matricula: Matricula): Result<Unit> =
        if (configManager.isLocalMode()) local.modificar(matricula) else remote.modificar(matricula)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminar(id) else remote.eliminar(id)

    override suspend fun listarPorCedula(cedula: String): Result<List<MatriculaAlumnoDto>> =
        if (configManager.isLocalMode()) local.listarPorCedula(cedula) else remote.listarPorCedula(
            cedula
        )

    override suspend fun listarPorAlumnoYCiclo(
        idAlumno: Long,
        idCiclo: Long
    ): Result<List<MatriculaAlumnoDto>> =
        if (configManager.isLocalMode()) local.listarPorAlumnoYCiclo(
            idAlumno,
            idCiclo
        ) else remote.listarPorAlumnoYCiclo(idAlumno, idCiclo)

    override suspend fun listarPorGrupo(idGrupo: Long): Result<List<MatriculaAlumnoDto>> =
        if (configManager.isLocalMode()) local.listarPorGrupo(idGrupo) else remote.listarPorGrupo(
            idGrupo
        )

    override suspend fun buscarPorId(idMatricula: Long): Result<Matricula> =
        if (configManager.isLocalMode()) local.buscarPorId(idMatricula) else remote.buscarPorId(
            idMatricula
        )

    override suspend fun existeMatriculaPorAlumnoYGrupo(
        idAlumno: Long,
        idGrupo: Long
    ): Result<Boolean> = if (configManager.isLocalMode()) local.existeMatriculaPorAlumnoYGrupo(
        idAlumno,
        idGrupo
    ) else remote.existeMatriculaPorAlumnoYGrupo(idAlumno, idGrupo)

    override suspend fun modificarGrupoMatricula(idMatricula: Long, idGrupo: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.modificarGrupoMatricula(
            idMatricula,
            idGrupo
        ) else remote.modificarGrupoMatricula(idMatricula, idGrupo)

    override suspend fun buscarMatriculaPorGrupo(grupoId: Long): Result<Matricula> =
        if (configManager.isLocalMode()) local.buscarMatriculaPorGrupo(grupoId) else remote.buscarMatriculaPorGrupo(
            grupoId
        )
}
