package com.fadeculture.barber.domain.model

data class BloqueoAgenda(
    val id: String = "",
    val fecha: String = "",       // Ejemplo: "2026-07-02"
    val horaInicio: String = "",  // Ejemplo: "13:00"
    val horaFin: String = "",     // Ejemplo: "14:00"
    val motivo: String = ""       // Ejemplo: "Almuerzo"
)