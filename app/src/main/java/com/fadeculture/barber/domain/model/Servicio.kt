package com.fadeculture.barber.domain.model

data class Servicio(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precioSoles: Double = 0.0,
    val duracionMinutos: Int = 30,
    val imagenUrl: String = "",
    val estadoActivo: Boolean = true // 👈 Asegúrate de tener esta línea agregada
)