package com.example.gestionacademicaapp.utils

import android.content.Context
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.google.gson.Gson
import androidx.core.content.edit

object SessionManager {

    fun setUsuario(context: Context, usuario: Usuario) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            val json = Gson().toJson(usuario)
            putString(Constants.USER_KEY, json)
        }
    }

    fun getUsuario(context: Context): Usuario? {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(Constants.USER_KEY, null)
        return if (json != null) Gson().fromJson(json, Usuario::class.java) else null
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(Constants.USER_KEY)
        }
    }

    fun isLoggedIn(context: Context): Boolean = getUsuario(context) != null

    // Obtener el rol del usuario
    fun getUserRole(context: Context): String? {
        val usuario = getUsuario(context)
        return usuario?.tipo
    }

    // Verificar si el usuario tiene un rol específico
    fun hasRole(context: Context, role: String): Boolean {
        val usuario = getUsuario(context)
        return usuario?.tipo?.equals(role, ignoreCase = true) ?: false
    }

    // Verificar si el usuario tiene alguno de los roles requeridos
    fun hasAnyRole(context: Context, roles: List<String>): Boolean {
        val usuario = getUsuario(context)
        return usuario?.tipo?.let { userRole ->
            roles.any { role -> userRole.equals(role, ignoreCase = true) }
        } ?: false
    }
}
