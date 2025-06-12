package com.example.gestionacademicaapp.data.repository

import android.util.Log
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.dao.CursoDao
import jakarta.inject.Inject
import retrofit2.HttpException

interface CursoRepository {
    suspend fun listar(): Result<List<Curso>>
    suspend fun insertar(curso: Curso): Result<Unit>
    suspend fun modificar(curso: Curso): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun buscarPorCodigo(codigo: String): Result<Curso>
    suspend fun buscarPorNombre(nombre: String): Result<Curso>
    suspend fun buscarPorCarrera(idCarrera: Long): Result<List<CursoDto>>
    suspend fun buscarPorCarreraYCiclo(idCarrera: Long, idCiclo: Long): Result<List<CursoDto>>
    suspend fun buscarPorCiclo(idCiclo: Long): Result<List<CursoDto>>
}

class CursoRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : CursoRepository {
    override suspend fun listar(): Result<List<Curso>> = safeApiCall {
        Log.d("CursoRepository", "Listando cursos")
        val response = apiService.getAllCursos()
        Log.d("CursoRepository", "Respuesta de listar: $response")
        response
    }

    override suspend fun insertar(curso: Curso): Result<Unit> = safeApiCall {
        Log.d("CursoRepository", "Insertando curso: $curso")
        val response = apiService.insertCurso(curso)
        Log.d("CursoRepository", "Respuesta de insertar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(curso: Curso): Result<Unit> = safeApiCall {
        Log.d("CursoRepository", "Modificando curso: $curso")
        val response = apiService.updateCurso(curso)
        Log.d("CursoRepository", "Respuesta de modificar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        Log.d("CursoRepository", "Eliminando curso: id=$id")
        val response = apiService.deleteCurso(id)
        Log.d("CursoRepository", "Respuesta de eliminar: $response")
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun buscarPorCodigo(codigo: String): Result<Curso> = safeApiCall {
        Log.d("CursoRepository", "Buscando curso por codigo: $codigo")
        val response = apiService.getCursoByCodigo(codigo)
        Log.d("CursoRepository", "Respuesta de buscarPorCodigo: $response")
        response
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Curso> = safeApiCall {
        Log.d("CursoRepository", "Buscando curso por nombre: $nombre")
        val response = apiService.getCursoByNombre(nombre)
        Log.d("CursoRepository", "Respuesta de buscarPorNombre: $response")
        response
    }

    override suspend fun buscarPorCarrera(idCarrera: Long): Result<List<CursoDto>> = safeApiCall {
        Log.d("CursoRepository", "Buscando cursos por carrera: idCarrera=$idCarrera")
        val response = apiService.getCursosByCarrera(idCarrera)
        Log.d("CursoRepository", "Respuesta de buscarPorCarrera: $response")
        response
    }

    override suspend fun buscarPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        Log.d("CursoRepository", "Buscando cursos por carrera=$idCarrera, ciclo=$idCiclo")
        val response = apiService.getCursosByCarreraYCiclo(idCarrera, idCiclo)
        Log.d("CursoRepository", "Respuesta de buscarPorCarreraYCiclo: $response")
        response
    }

    override suspend fun buscarPorCiclo(idCiclo: Long): Result<List<CursoDto>> = safeApiCall {
        Log.d("CursoRepository", "Buscando cursos por ciclo: idCiclo=$idCiclo")
        val response = apiService.getCursosByCiclo(idCiclo)
        Log.d("CursoRepository", "Respuesta de buscarPorCiclo: $response")
        response
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Log.e("CursoRepository", "Error en safeApiCall: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class CursoRepositoryLocal @Inject constructor(
    private val cursoDao: CursoDao
) : CursoRepository {
    override suspend fun listar(): Result<List<Curso>> = try {
        Result.success(cursoDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(curso: Curso): Result<Unit> = try {
        cursoDao.insert(curso)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(curso: Curso): Result<Unit> = try {
        cursoDao.update(curso)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        cursoDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCodigo(codigo: String): Result<Curso> = try {
        val curso = cursoDao.getByCodigo(codigo)
        if (curso != null) Result.success(curso) else Result.failure(NoSuchElementException("Curso no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorNombre(nombre: String): Result<Curso> = try {
        val curso = cursoDao.getByNombre(nombre)
        if (curso != null) Result.success(curso) else Result.failure(NoSuchElementException("Curso no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCarrera(idCarrera: Long): Result<List<CursoDto>> = try {
        Result.success(cursoDao.getByCarrera(idCarrera))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = try {
        Result.success(cursoDao.getByCarreraYCiclo(idCarrera, idCiclo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCiclo(idCiclo: Long): Result<List<CursoDto>> = try {
        Result.success(cursoDao.getByCiclo(idCiclo))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class CursoRepositoryImpl @Inject constructor(
    private val remote: CursoRepositoryRemote,
    private val local: CursoRepositoryLocal,
    private val isLocalMode: Boolean
) : CursoRepository {
    override suspend fun listar(): Result<List<Curso>> =
        if (isLocalMode) local.listar() else remote.listar()

    override suspend fun insertar(curso: Curso): Result<Unit> =
        if (isLocalMode) local.insertar(curso) else remote.insertar(curso)

    override suspend fun modificar(curso: Curso): Result<Unit> =
        if (isLocalMode) local.modificar(curso) else remote.modificar(curso)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (isLocalMode) local.eliminar(id) else remote.eliminar(id)

    override suspend fun buscarPorCodigo(codigo: String): Result<Curso> =
        if (isLocalMode) local.buscarPorCodigo(codigo) else remote.buscarPorCodigo(codigo)

    override suspend fun buscarPorNombre(nombre: String): Result<Curso> =
        if (isLocalMode) local.buscarPorNombre(nombre) else remote.buscarPorNombre(nombre)

    override suspend fun buscarPorCarrera(idCarrera: Long): Result<List<CursoDto>> =
        if (isLocalMode) local.buscarPorCarrera(idCarrera) else remote.buscarPorCarrera(idCarrera)

    override suspend fun buscarPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = if (isLocalMode) local.buscarPorCarreraYCiclo(
        idCarrera,
        idCiclo
    ) else remote.buscarPorCarreraYCiclo(idCarrera, idCiclo)

    override suspend fun buscarPorCiclo(idCiclo: Long): Result<List<CursoDto>> =
        if (isLocalMode) local.buscarPorCiclo(idCiclo) else remote.buscarPorCiclo(idCiclo)
}
