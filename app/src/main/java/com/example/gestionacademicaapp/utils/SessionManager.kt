package com.example.gestionacademicaapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.dao.UsuarioDao
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: ConfigManager,
    internal val usuarioDao: UsuarioDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun setUsuario(usuario: Usuario) {
        prefs.edit {
            val json = Gson().toJson(usuario)
            putString(Constants.USER_KEY, json)
        }
    }

    fun getUsuario(): Usuario? {
        val json = prefs.getString(Constants.USER_KEY, null)
        val usuario = if (json != null) Gson().fromJson(json, Usuario::class.java) else null
        return if (configManager.isLocalMode() && usuario != null) {
            // Validate session against local DB in local mode
            runBlocking {
                usuarioDao.getByCedula(usuario.cedula)?.takeIf { it.idUsuario == usuario.idUsuario }
            }
        } else {
            usuario
        }
    }

    fun getUserId(): Long {
        return getUsuario()?.idUsuario ?: -1L
    }

    fun clear() {
        prefs.edit {
            remove(Constants.USER_KEY)
        }
    }

    fun isLoggedIn(): Boolean = getUsuario() != null

    fun getUserRole(): String? {
        return getUsuario()?.tipo
    }

    fun hasRole(role: String): Boolean {
        return getUsuario()?.tipo?.equals(role, ignoreCase = true) == true
    }

    fun hasAnyRole(roles: List<String>): Boolean {
        return getUsuario()?.tipo?.let { userRole ->
            roles.any { role -> userRole.equals(role, ignoreCase = true) }
        } == true
    }
}
