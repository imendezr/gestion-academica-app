package com.example.gestionacademicaapp.utils

import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.annotation.ColorRes
import com.google.android.material.snackbar.Snackbar

object Notificador {

    fun show(
        view: View,
        mensaje: String,
        @ColorRes colorResId: Int? = null,
        duracion: Int = Snackbar.LENGTH_LONG, // Cambiado a LONG para mayor visibilidad
        accionTexto: String? = null, // Acción opcional
        accion: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, mensaje, duracion)

        // Color de fondo
        colorResId?.let {
            val color = view.context.getColor(it)
            snackbar.setBackgroundTint(color)
        }

        // Color del texto adaptable al tema
        val textColor = if (isSystemInDarkTheme(view.context)) {
            android.R.color.white
        } else {
            android.R.color.black
        }
        snackbar.setTextColor(view.context.getColor(textColor))

        // Acción solo si se proporciona accionTexto
        if (accionTexto != null) {
            snackbar.setAction(accionTexto) {
                accion?.invoke() ?: snackbar.dismiss()
            }
        }

        snackbar.show()
    }

    // Función auxiliar para detectar el tema oscuro
    private fun isSystemInDarkTheme(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }
}
