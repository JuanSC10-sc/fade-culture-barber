package com.fadeculture.barber.data.model

data class Barbero(
    val id: String = "",
    val nombres: String = "",
    val especialidad: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val estadoActivo: Boolean = true
)