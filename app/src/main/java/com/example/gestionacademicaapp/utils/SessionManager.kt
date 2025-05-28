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
}
