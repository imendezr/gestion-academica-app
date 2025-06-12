package com.example.gestionacademicaapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOCAL_MODE = "is_local_mode"
        private const val DEFAULT_LOCAL_MODE = true // Offline default
    }

    fun isLocalMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOCAL_MODE, DEFAULT_LOCAL_MODE)
    }

    fun setLocalMode(isLocal: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_LOCAL_MODE, isLocal) }
    }
}
