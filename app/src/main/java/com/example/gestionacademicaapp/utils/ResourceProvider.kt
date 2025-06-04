package com.example.gestionacademicaapp.utils

import android.content.Context
import androidx.annotation.StringRes
import jakarta.inject.Inject

class ResourceProvider @Inject constructor(private val context: Context) {
    fun getString(@StringRes resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
}