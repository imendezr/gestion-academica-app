package com.example.gestionacademicaapp.utils

import android.view.View
import androidx.annotation.ColorRes
import com.google.android.material.snackbar.Snackbar

object Notificador {

    fun show(
        view: View,
        mensaje: String,
        @ColorRes colorResId: Int? = null,
        duracion: Int = Snackbar.LENGTH_SHORT,
        accionTexto: String = "Cerrar",
        accion: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, mensaje, duracion)

        // Color de fondo
        colorResId?.let {
            val color = view.context.getColor(it)
            snackbar.setBackgroundTint(color)
        }

        // Color del texto
        snackbar.setTextColor(view.context.getColor(android.R.color.white))

        // Acción opcional
        snackbar.setAction(accionTexto) {
            accion?.invoke() ?: snackbar.dismiss()
        }

        snackbar.show()
    }
}
