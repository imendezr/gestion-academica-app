package com.example.gestionacademicaapp.data.response

import com.google.gson.annotations.SerializedName

sealed class ApiResponse<out T> {
    data class Success<T>(
        @SerializedName("data")
        val data: T
    ) : ApiResponse<T>()

    data class Error(
        @SerializedName("message")
        val message: String,
        @SerializedName("code")
        val code: Int? = null
    ) : ApiResponse<Nothing>()

    // Compañero para crear instancias fácilmente
    companion object {
        fun <T> success(data: T): ApiResponse<T> = Success(data)
        fun error(message: String, code: Int? = null): ApiResponse<Nothing> = Error(message, code)
    }
}
