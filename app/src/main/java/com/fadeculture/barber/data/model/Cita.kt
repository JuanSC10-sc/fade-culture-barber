package com.fadeculture.barber.data.model

data class Cita(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val clienteApellidos: String = "",
    val barberoId: String = "",
    val barberoNombre: String = "",
    val servicioId: String = "",
    val servicioTitulo: String = "",
    val duracionMinutos: Int = 0,
    val precio: Double = 0.0,
    val fecha: String = "", // Formato: YYYY-MM-DD
    val hora: String = "",  // Formato: HH:mm
    val estado: String = "pendiente" // pendiente, finalizada, cancelada
)