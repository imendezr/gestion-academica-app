package com.example.gestionacademicaapp.utils

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(): String {
    return when (this) {
        is IOException -> "Sin conexión a internet. Verifica tu red."
        is HttpException -> when (code()) {
            400 -> "Solicitud inválida"
            401 -> "No autorizado"
            403 -> "Acceso denegado"
            404 -> "Recurso no encontrado"
            500 -> "Error interno del servidor"
            else -> "Error del servidor: ${code()}"
        }

        else -> message ?: "Ocurrió un error inesperado"
    }
}
