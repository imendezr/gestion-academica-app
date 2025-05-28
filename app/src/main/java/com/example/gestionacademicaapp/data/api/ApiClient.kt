package com.example.gestionacademicaapp.data.api

import android.content.Context
import com.example.gestionacademicaapp.data.response.ApiResponse
import com.example.gestionacademicaapp.utils.Constants
import com.example.gestionacademicaapp.utils.isNetworkAvailable
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val CONNECT_TIMEOUT = 10000 // 10 segundos
    private const val READ_TIMEOUT = 15000    // 15 segundos

    // Método auxiliar para determinar si el error es reintentable
    private fun isRetryableError(responseCode: Int?, exception: Exception?): Boolean {
        // Reintentable: problemas de conexión o errores del servidor (5xx)
        return exception != null || (responseCode != null && responseCode in 500..599)
    }

    private fun extractMessageFromJson(json: String): String {
        return try {
            val jsonObject = Gson().fromJson(json, com.google.gson.JsonObject::class.java)
            jsonObject["message"]?.asString ?: json
        } catch (e: Exception) {
            json // fallback: retornar tal cual si no es JSON
        }
    }

    // Método genérico para solicitudes GET
    suspend fun <T> get(
        context: Context,
        endpoint: String,
        responseClass: Class<T>
    ): ApiResponse<T> =
        withContext(Dispatchers.IO) {
            if (!context.isNetworkAvailable()) {
                return@withContext ApiResponse.error("No hay conexión a Internet")
            }

            var attempt = 0
            var lastErrorMessage: String? = null

            while (attempt < Constants.MAX_RETRIES) {
                val url = URL("${Constants.BASE_URL}$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = CONNECT_TIMEOUT
                    connection.readTimeout = READ_TIMEOUT
                    connection.setRequestProperty("Content-Type", "application/json")

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()
                        val data = Gson().fromJson(response, responseClass)
                        return@withContext ApiResponse.success(data)
                    } else {
                        val reader = BufferedReader(InputStreamReader(connection.errorStream))
                        val errorMessage = reader.readText()
                        reader.close()
                        lastErrorMessage = extractMessageFromJson(errorMessage)

                        if (responseCode in 400..499) {
                            return@withContext ApiResponse.error(errorMessage, responseCode)
                        }
                        if (!isRetryableError(responseCode, null)) {
                            return@withContext ApiResponse.error(
                                "Error en la solicitud: $responseCode - $errorMessage",
                                responseCode
                            )
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.message
                    if (!isRetryableError(null, e) || attempt == Constants.MAX_RETRIES - 1) {
                        return@withContext ApiResponse.error("Error: ${e.message}")
                    }
                } finally {
                    connection.disconnect()
                }

                attempt++
                if (attempt < Constants.MAX_RETRIES) {
                    delay(Constants.RETRY_DELAY_MS)
                }
            }

            return@withContext ApiResponse.error(
                lastErrorMessage ?: "Error: Se agotaron los intentos (${Constants.MAX_RETRIES})"
            )
        }

    // Método genérico para solicitudes POST
    suspend fun <T> post(
        context: Context,
        endpoint: String,
        data: Any,
        responseClass: Class<T>
    ): ApiResponse<T> =
        withContext(Dispatchers.IO) {
            if (!context.isNetworkAvailable()) {
                return@withContext ApiResponse.error("No hay conexión a Internet")
            }

            var attempt = 0
            var lastErrorMessage: String? = null

            while (attempt < Constants.MAX_RETRIES) {
                val url = URL("${Constants.BASE_URL}$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = CONNECT_TIMEOUT
                    connection.readTimeout = READ_TIMEOUT
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    val jsonInput = Gson().toJson(data)
                    val outputStream: OutputStream = connection.outputStream
                    outputStream.write(jsonInput.toByteArray())
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()
                        val data = Gson().fromJson(response, responseClass)
                        return@withContext ApiResponse.success(data)
                    } else {
                        val reader = BufferedReader(InputStreamReader(connection.errorStream))
                        val errorMessage = reader.readText()
                        reader.close()
                        lastErrorMessage = extractMessageFromJson(errorMessage)

                        if (responseCode in 400..499) {
                            return@withContext ApiResponse.error(errorMessage, responseCode)
                        }
                        if (!isRetryableError(responseCode, null)) {
                            return@withContext ApiResponse.error(
                                "Error en la solicitud: $responseCode - $errorMessage",
                                responseCode
                            )
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.message
                    if (!isRetryableError(null, e) || attempt == Constants.MAX_RETRIES - 1) {
                        return@withContext ApiResponse.error("Error: ${e.message}")
                    }
                } finally {
                    connection.disconnect()
                }

                attempt++
                if (attempt < Constants.MAX_RETRIES) {
                    delay(Constants.RETRY_DELAY_MS)
                }
            }

            return@withContext ApiResponse.error(
                lastErrorMessage ?: "Error: Se agotaron los intentos (${Constants.MAX_RETRIES})"
            )
        }

    // Método genérico para solicitudes PUT
    suspend fun <T> put(
        context: Context,
        endpoint: String,
        data: Any,
        responseClass: Class<T>
    ): ApiResponse<T> =
        withContext(Dispatchers.IO) {
            if (!context.isNetworkAvailable()) {
                return@withContext ApiResponse.error("No hay conexión a Internet")
            }

            var attempt = 0
            var lastErrorMessage: String? = null

            while (attempt < Constants.MAX_RETRIES) {
                val url = URL("${Constants.BASE_URL}$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "PUT"
                    connection.connectTimeout = CONNECT_TIMEOUT
                    connection.readTimeout = READ_TIMEOUT
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    val jsonInput = Gson().toJson(data)
                    val outputStream: OutputStream = connection.outputStream
                    outputStream.write(jsonInput.toByteArray())
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()
                        val result = Gson().fromJson(response, responseClass)
                        return@withContext ApiResponse.success(result)
                    } else {
                        val reader = BufferedReader(InputStreamReader(connection.errorStream))
                        val errorMessage = reader.readText()
                        reader.close()
                        lastErrorMessage = extractMessageFromJson(errorMessage)

                        if (responseCode in 400..499) {
                            return@withContext ApiResponse.error(errorMessage, responseCode)
                        }
                        if (!isRetryableError(responseCode, null)) {
                            return@withContext ApiResponse.error(
                                "Error en la solicitud: $responseCode - $errorMessage",
                                responseCode
                            )
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.message
                    if (!isRetryableError(null, e) || attempt == Constants.MAX_RETRIES - 1) {
                        return@withContext ApiResponse.error("Error: ${e.message}")
                    }
                } finally {
                    connection.disconnect()
                }

                attempt++
                if (attempt < Constants.MAX_RETRIES) {
                    delay(Constants.RETRY_DELAY_MS)
                }
            }

            return@withContext ApiResponse.error(
                lastErrorMessage ?: "Error: Se agotaron los intentos (${Constants.MAX_RETRIES})"
            )
        }

    // Método genérico para solicitudes DELETE
    suspend fun delete(context: Context, endpoint: String): ApiResponse<Boolean> =
        withContext(Dispatchers.IO) {
            if (!context.isNetworkAvailable()) {
                return@withContext ApiResponse.error("No hay conexión a Internet")
            }

            var attempt = 0
            var lastErrorMessage: String? = null

            while (attempt < Constants.MAX_RETRIES) {
                val url = URL("${Constants.BASE_URL}$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "DELETE"
                    connection.connectTimeout = CONNECT_TIMEOUT
                    connection.readTimeout = READ_TIMEOUT

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        return@withContext ApiResponse.success(true)
                    } else {
                        val reader = BufferedReader(InputStreamReader(connection.errorStream))
                        val errorMessage = reader.readText()
                        reader.close()
                        lastErrorMessage = extractMessageFromJson(errorMessage)

                        if (responseCode in 400..499) {
                            return@withContext ApiResponse.error(errorMessage, responseCode)
                        }
                        if (!isRetryableError(responseCode, null)) {
                            return@withContext ApiResponse.error(
                                "Error en la solicitud: $responseCode - $errorMessage",
                                responseCode
                            )
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.message
                    if (!isRetryableError(null, e) || attempt == Constants.MAX_RETRIES - 1) {
                        return@withContext ApiResponse.error("Error: ${e.message}")
                    }
                } finally {
                    connection.disconnect()
                }

                attempt++
                if (attempt < Constants.MAX_RETRIES) {
                    delay(Constants.RETRY_DELAY_MS)
                }
            }

            return@withContext ApiResponse.error(
                lastErrorMessage ?: "Error: Se agotaron los intentos (${Constants.MAX_RETRIES})"
            )
        }
}
