package com.fadeculture.barber.domain.model

data class Barbero(
    val id: String = "",
    val nombres: String = "",
    val especialidad: String = "",
    val correo: String = "",
    val fotoUrl: String = "",
    val estadoActivo: Boolean = true
)