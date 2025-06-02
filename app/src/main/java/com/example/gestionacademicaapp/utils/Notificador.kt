package com.example.gestionacademicaapp.utils

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import com.google.android.material.snackbar.Snackbar

/**
 * Utilidad para mostrar Snackbars personalizados con colores, acciones y anclaje.
 * Usa `show` para mensajes simples y `SnackbarBuilder` para configuraciones complejas con acciones.
 */
object Notificador {

    /**
     * Muestra un Snackbar con configuración personalizada.
     *
     * @param view Vista donde se mostrará el Snackbar.
     * @param mensaje Texto a mostrar.
     * @param colorResId Color de fondo (opcional).
     * @param duracion Duración del Snackbar (por defecto LENGTH_LONG).
     * @param accionTexto Texto de la acción (opcional).
     * @param accion Callback de la acción (opcional).
     * @param anchorView Vista donde anclar el Snackbar (opcional).
     */
    fun show(
        view: View,
        mensaje: String,
        @ColorRes colorResId: Int? = null,
        duracion: Int = Snackbar.LENGTH_LONG,
        accionTexto: String? = null,
        accion: (() -> Unit)? = null,
        anchorView: View? = null,
        isDarkTheme: Boolean = isSystemInDarkTheme(view.context)
    ) {
        if (mensaje.isBlank()) return // Evitar mostrar Snackbars vacíos

        val snackbar = Snackbar.make(view, mensaje, duracion)

        // Color de fondo
        colorResId?.let { snackbar.setBackgroundTint(view.context.getColor(it)) }

        // Color del texto según el tema
        val textColorAttr = if (isDarkTheme) {
            com.google.android.material.R.attr.colorOnSurface
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        val typedValue = TypedValue()
        view.context.theme.resolveAttribute(textColorAttr, typedValue, true)
        snackbar.setTextColor(typedValue.data)

        // Acción opcional
        accionTexto?.let { text ->
            snackbar.setAction(text) { accion?.invoke() ?: snackbar.dismiss() }
        }

        // Anclar si se proporciona una vista
        anchorView?.let { snackbar.anchorView = it }

        snackbar.show()
    }

    /**
     * Builder para configurar un Snackbar de manera fluida.
     */
    class SnackbarBuilder(private val view: View, private val mensaje: String) {
        private var colorResId: Int? = null
        private var duracion: Int = Snackbar.LENGTH_LONG
        private var accionTexto: String? = null
        private var accion: (() -> Unit)? = null
        private var anchorView: View? = null
        private var isDarkTheme: Boolean = isSystemInDarkTheme(view.context)

        fun withBackground(@ColorRes color: Int) = apply { colorResId = color }
        fun withDuration(duration: Int) = apply { duracion = duration }
        fun withAction(text: String, action: () -> Unit) = apply {
            accionTexto = text
            accion = action
        }
        fun withAnchor(anchor: View) = apply { anchorView = anchor }
        fun withDarkTheme(dark: Boolean) = apply { isDarkTheme = dark }

        fun show() = show(
            view = view,
            mensaje = mensaje,
            colorResId = colorResId,
            duracion = duracion,
            accionTexto = accionTexto,
            accion = accion,
            anchorView = anchorView,
            isDarkTheme = isDarkTheme
        )
    }

    private fun isSystemInDarkTheme(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }
}
