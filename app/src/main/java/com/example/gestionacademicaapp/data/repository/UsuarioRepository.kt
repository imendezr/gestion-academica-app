package com.example.gestionacademicaapp.data.repository

import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.dao.UsuarioDao
import com.example.gestionacademicaapp.utils.ConfigManager
import jakarta.inject.Inject
import retrofit2.HttpException

interface UsuarioRepository {
    suspend fun listar(): Result<List<Usuario>>
    suspend fun insertar(usuario: Usuario): Result<Unit>
    suspend fun modificar(usuario: Usuario): Result<Unit>
    suspend fun eliminar(id: Long): Result<Unit>
    suspend fun buscarPorCedula(cedula: String): Result<Usuario>
    suspend fun login(cedula: String, clave: String): Result<Usuario>
}

class UsuarioRepositoryRemote @Inject constructor(
    private val apiService: ApiService
) : UsuarioRepository {
    override suspend fun listar(): Result<List<Usuario>> = safeApiCall {
        apiService.getAllUsuarios()
    }

    override suspend fun insertar(usuario: Usuario): Result<Unit> = safeApiCall {
        val response = apiService.insertUsuario(usuario)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun modificar(usuario: Usuario): Result<Unit> = safeApiCall {
        val response = apiService.updateUsuario(usuario)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = safeApiCall {
        val response = apiService.deleteUsuario(id)
        if (response.isSuccessful) Unit else throw HttpException(response)
    }

    override suspend fun buscarPorCedula(cedula: String): Result<Usuario> = safeApiCall {
        apiService.getUsuarioByCedula(cedula)
    }

    override suspend fun login(cedula: String, clave: String): Result<Usuario> = safeApiCall {
        apiService.login(cedula, clave)
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UsuarioRepositoryLocal @Inject constructor(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {
    override suspend fun listar(): Result<List<Usuario>> = try {
        Result.success(usuarioDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun insertar(usuario: Usuario): Result<Unit> = try {
        usuarioDao.insert(usuario)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun modificar(usuario: Usuario): Result<Unit> = try {
        usuarioDao.update(usuario)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminar(id: Long): Result<Unit> = try {
        usuarioDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun buscarPorCedula(cedula: String): Result<Usuario> = try {
        val usuario = usuarioDao.getByCedula(cedula)
        if (usuario != null) Result.success(usuario) else Result.failure(NoSuchElementException("Usuario no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun login(cedula: String, clave: String): Result<Usuario> = try {
        val usuario = usuarioDao.login(cedula, clave)
        if (usuario != null) Result.success(usuario) else Result.failure(NoSuchElementException("Credenciales inválidas"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class UsuarioRepositoryImpl @Inject constructor(
    private val remote: UsuarioRepositoryRemote,
    private val local: UsuarioRepositoryLocal,
    private val configManager: ConfigManager
) : UsuarioRepository {
    override suspend fun listar(): Result<List<Usuario>> =
        if (configManager.isLocalMode()) local.listar() else remote.listar()

    override suspend fun insertar(usuario: Usuario): Result<Unit> =
        if (configManager.isLocalMode()) local.insertar(usuario) else remote.insertar(usuario)

    override suspend fun modificar(usuario: Usuario): Result<Unit> =
        if (configManager.isLocalMode()) local.modificar(usuario) else remote.modificar(usuario)

    override suspend fun eliminar(id: Long): Result<Unit> =
        if (configManager.isLocalMode()) local.eliminar(id) else remote.eliminar(id)

    override suspend fun buscarPorCedula(cedula: String): Result<Usuario> =
        if (configManager.isLocalMode()) local.buscarPorCedula(cedula) else remote.buscarPorCedula(
            cedula
        )

    override suspend fun login(cedula: String, clave: String): Result<Usuario> =
        if (configManager.isLocalMode()) local.login(cedula, clave) else remote.login(cedula, clave)
}
