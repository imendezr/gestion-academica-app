package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoProfesorDto
import com.example.gestionacademicaapp.data.dao.GrupoDao
import jakarta.inject.Inject
import retrofit2.HttpException

interface GrupoRepository {
    suspend fun listar(): Result<List<Grupo>>
    suspend fun insertar(grupo: Grupo): Result<Unit>
    suspend fun modificar(grupo: Grupo): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun cursosPorCarreraYCiclo(idCarrera: Long, idCiclo: Long): Result<List<CursoDto>>
    suspend fun gruposPorCarreraCurso(idCarrera: Long, idCurso: Long): Result<List<GrupoDto>>
    suspend fun gruposPorCursoCicloCarrera(
        idCurso: Long,
        idCiclo: Long,
        idCarrera: Long
    ): Result<List<GrupoDto>>

    suspend fun gruposPorProfesor(cedula: String): Result<List<GrupoDto>>
    suspend fun gruposPorProfesorCicloActivo(cedula: String): Result<List<GrupoProfesorDto>>
    suspend fun buscarGrupoPorMatricula(idMatricula: Long): Result<GrupoDto>
    suspend fun buscarCursoPorGrupo(idGrupo: Long): Result<CursoDto>
}

class GrupoRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : GrupoRepository {
    override suspend fun listar(): Result<List<Grupo>> = safeApiCall {
        apiService.getAllGrupos()
    }

    override suspend fun insertar(grupo: Grupo): Result<Unit> = safeApiCall {
        val response = apiService.insertGrupo(grupo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(grupo: Grupo): Result<Unit> = safeApiCall {
        val response = apiService.updateGrupo(grupo)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteGrupo(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun cursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = safeApiCall {
        apiService.getCursosByCarreraAndCiclo(idCarrera, idCiclo)
    }

    override suspend fun gruposPorCarreraCurso(
        idCarrera: Long,
        idCurso: Long
    ): Result<List<GrupoDto>> = safeApiCall {
        apiService.getGruposByCarreraCurso(idCarrera, idCurso)
    }

    override suspend fun gruposPorCursoCicloCarrera(
        idCurso: Long,
        idCiclo: Long,
        idCarrera: Long
    ): Result<List<GrupoDto>> = safeApiCall {
        apiService.getGruposByCursoCicloCarrera(idCurso, idCiclo, idCarrera)
    }

    override suspend fun gruposPorProfesor(cedula: String): Result<List<GrupoDto>> = safeApiCall {
        apiService.getGruposByProfesor(cedula)
    }

    override suspend fun gruposPorProfesorCicloActivo(cedula: String): Result<List<GrupoProfesorDto>> =
        safeApiCall {
            apiService.getGruposByProfesorCicloActivo(cedula)
        }

    override suspend fun buscarGrupoPorMatricula(idMatricula: Long): Result<GrupoDto> =
        safeApiCall {
            apiService.getGrupoByMatriculaId(idMatricula)
        }

    override suspend fun buscarCursoPorGrupo(idGrupo: Long): Result<CursoDto> = safeApiCall {
        apiService.getCursoByGrupoId(idGrupo)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GrupoRepositoryLocal @Inject constructor(
    private val grupoDao: GrupoDao
) : GrupoRepository {
    override suspend fun listar(): Result<List<Grupo>> = try {
        Result.success(grupoDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(grupo: Grupo): Result<Unit> = try {
        grupoDao.insert(grupo)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(grupo: Grupo): Result<Unit> = try {
        grupoDao.update(grupo)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        grupoDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun cursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = try {
        Result.success(grupoDao.getCursosByCarreraAndCiclo(idCarrera, idCiclo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun gruposPorCarreraCurso(
        idCarrera: Long,
        idCurso: Long
    ): Result<List<GrupoDto>> = try {
        Result.success(grupoDao.getGruposByCarreraCurso(idCarrera, idCurso))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun gruposPorCursoCicloCarrera(
        idCurso: Long,
        idCiclo: Long,
        idCarrera: Long
    ): Result<List<GrupoDto>> = try {
        Result.success(grupoDao.getGruposByCursoCicloCarrera(idCurso, idCiclo, idCarrera))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun gruposPorProfesor(cedula: String): Result<List<GrupoDto>> = try {
        Result.success(grupoDao.getGruposByProfesor(cedula))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun gruposPorProfesorCicloActivo(cedula: String): Result<List<GrupoProfesorDto>> =
        try {
            Result.success(grupoDao.getGruposByProfesorCicloActivo(cedula))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun buscarGrupoPorMatricula(idMatricula: Long): Result<GrupoDto> = try {
        val grupo = grupoDao.getGrupoByMatriculaId(idMatricula)
        if (grupo != null) Result.success(grupo) else Result.failure(NoSuchElementException("Grupo no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarCursoPorGrupo(idGrupo: Long): Result<CursoDto> = try {
        val curso = grupoDao.getCursoByGrupoId(idGrupo)
        if (curso != null) Result.success(curso) else Result.failure(NoSuchElementException("Curso no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class GrupoRepositoryImpl @Inject constructor(
    private val remote: GrupoRepositoryRemote,
    private val local: GrupoRepositoryLocal,
    private val isLocalMode: Boolean
) : GrupoRepository {
    override suspend fun listar(): Result<List<Grupo>> =
        if (isLocalMode) local.listar() else remote.listar()

    override suspend fun insertar(grupo: Grupo): Result<Unit> =
        if (isLocalMode) local.insertar(grupo) else remote.insertar(grupo)

    override suspend fun modificar(grupo: Grupo): Result<Unit> =
        if (isLocalMode) local.modificar(grupo) else remote.modificar(grupo)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (isLocalMode) local.eliminar(id) else remote.eliminar(id)

    override suspend fun cursosPorCarreraYCiclo(
        idCarrera: Long,
        idCiclo: Long
    ): Result<List<CursoDto>> = if (isLocalMode) local.cursosPorCarreraYCiclo(
        idCarrera,
        idCiclo
    ) else remote.cursosPorCarreraYCiclo(idCarrera, idCiclo)

    override suspend fun gruposPorCarreraCurso(
        idCarrera: Long,
        idCurso: Long
    ): Result<List<GrupoDto>> = if (isLocalMode) local.gruposPorCarreraCurso(
        idCarrera,
        idCurso
    ) else remote.gruposPorCarreraCurso(idCarrera, idCurso)

    override suspend fun gruposPorCursoCicloCarrera(
        idCurso: Long,
        idCiclo: Long,
        idCarrera: Long
    ): Result<List<GrupoDto>> = if (isLocalMode) local.gruposPorCursoCicloCarrera(
        idCurso,
        idCiclo,
        idCarrera
    ) else remote.gruposPorCursoCicloCarrera(idCurso, idCiclo, idCarrera)

    override suspend fun gruposPorProfesor(cedula: String): Result<List<GrupoDto>> =
        if (isLocalMode) local.gruposPorProfesor(cedula) else remote.gruposPorProfesor(cedula)

    override suspend fun gruposPorProfesorCicloActivo(cedula: String): Result<List<GrupoProfesorDto>> =
        if (isLocalMode) local.gruposPorProfesorCicloActivo(cedula) else remote.gruposPorProfesorCicloActivo(
            cedula
        )

    override suspend fun buscarGrupoPorMatricula(idMatricula: Long): Result<GrupoDto> =
        if (isLocalMode) local.buscarGrupoPorMatricula(idMatricula) else remote.buscarGrupoPorMatricula(
            idMatricula
        )

    override suspend fun buscarCursoPorGrupo(idGrupo: Long): Result<CursoDto> =
        if (isLocalMode) local.buscarCursoPorGrupo(idGrupo) else remote.buscarCursoPorGrupo(idGrupo)
}
