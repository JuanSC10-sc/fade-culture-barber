package com.fadeculture.barber.data.source

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object EmailService {

    private const val SERVICE_ID = "service_lm0afjy"
    private const val TEMPLATE_ID = "template_tqfjswa"
    private const val PUBLIC_KEY = "ZymITF7Ou7cHTvybf"

    private const val URL_EMAIL_JS = "https://api.emailjs.com/api/v1.0/email/send"
    private val client = OkHttpClient()

    //corrutinas
    suspend fun enviarComprobante(
        correoDestino: String,
        nombreCliente: String,
        servicio: String,
        barbero: String,
        fecha: String,
        hora: String,
        precio: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // parámetros dinámicos
            val templateParams = JSONObject().apply {
                put("correo_destino", correoDestino)
                put("nombre_cliente", nombreCliente)
                put("servicio", servicio)
                put("barbero", barbero)
                put("fecha", fecha)
                put("hora", hora)
                put("precio", precio)
            }

            // estructura principal API de EmailJS
            val requestPayload = JSONObject().apply {
                put("service_id", SERVICE_ID)
                put("template_id", TEMPLATE_ID)
                put("user_id", PUBLIC_KEY)
                put("template_params", templateParams)
            }

            // petición HTTP POST
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestPayload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(URL_EMAIL_JS)
                .post(requestBody)
                .build()

            // Ejecutar la petición
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful

            if (!isSuccess) {
                Log.e("EmailJS", "Error: ${response.body?.string()}")
            }

            return@withContext isSuccess

        } catch (e: Exception) {
            Log.e("EmailJS", "Excepción al enviar correo: ${e.message}")
            return@withContext false
        }
    }
}