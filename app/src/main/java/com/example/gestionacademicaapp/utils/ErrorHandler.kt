package com.example.gestionacademicaapp.utils

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

data class ErrorResponse(val error: String?, val message: String?, val status: Int)

fun Throwable.toUserMessage(): String {
    return when (this) {
        is IOException -> "Sin conexión a internet. Verifica tu red."
        is HttpException -> {
            try {
                val errorBody = response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                when (code()) {
                    400 -> errorResponse.message ?: "Solicitud inválida"
                    401 -> errorResponse.message ?: "No autorizado"
                    403 -> errorResponse.message ?: "Acceso denegado"
                    404 -> errorResponse.message ?: "Recurso no encontrado"
                    500 -> errorResponse.message ?: "Error interno del servidor"
                    else -> errorResponse.message ?: "Error del servidor: ${code()}"
                }
            } catch (e: Exception) {
                when (code()) {
                    400 -> "Solicitud inválida"
                    401 -> "No autorizado"
                    403 -> "Acceso denegado"
                    404 -> "Recurso no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error del servidor: ${code()}"
                }
            }
        }
        else -> message ?: "Ocurrió un error inesperado"
    }
}
