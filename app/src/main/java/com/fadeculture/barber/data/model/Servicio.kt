package com.fadeculture.barber.data.model

data class Servicio(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precioSoles: Double = 0.0,
    val duracionMinutos: Int = 30,
    val imagenUrl: String = "",
    val categoria: String = "Corte",
    val estadoActivo: Boolean = true //
)