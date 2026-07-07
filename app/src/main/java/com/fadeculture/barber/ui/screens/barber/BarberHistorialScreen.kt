package com.fadeculture.barber.ui.screens.barber

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fadeculture.barber.data.model.Cita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Composable
fun BarberHistorialScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Colores de la marca
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)
    val highlightBackground = Color(0xFF2A2415)

    // Estados de datos
    var todasLasCitas by remember { mutableStateOf<List<Cita>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Estados de Filtro de Fecha
    var fechaFiltroDB by remember { mutableStateOf("") } // YYYY-MM-DD
    var fechaFiltroVisual by remember { mutableStateOf("") } // DD/MM/YYYY
    val calendar = Calendar.getInstance()

    // 1. Escuchar TODAS las citas del barbero
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("citas")
                .whereEqualTo("barberoId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        todasLasCitas = snapshot.documents.map { doc ->
                            Cita(
                                id = doc.id,
                                clienteNombre = doc.getString("clienteNombre") ?: "Cliente",
                                servicioTitulo = doc.getString("servicioTitulo") ?: "Servicio",
                                fecha = doc.getString("fecha") ?: "",
                                hora = doc.getString("hora") ?: "",
                                estado = doc.getString("estado") ?: "pendiente",
                                precio = doc.getDouble("precio") ?: 0.0,
                                barberoNombre = doc.getString("barberoNombre") ?: ""
                            )
                        }
                        cargando = false
                    }
                }
        }
    }

    // 2. Lógica de Filtrado (Solo Históricas + Fecha Seleccionada)
    val citasHistorial = todasLasCitas.filter { cita ->
        // Filtramos por estado
        val esHistorica = cita.estado == "finalizada" || cita.estado == "cancelada"

        // Filtramos por fecha si hay una seleccionada
        val coincideFecha = if (fechaFiltroDB.isNotBlank()) {
            cita.fecha == fechaFiltroDB
        } else {
            true
        }

        esHistorica && coincideFecha
    }.sortedWith(compareByDescending<Cita> { it.fecha }.thenByDescending { it.hora })
    // Ordenamos de la más reciente a la más antigua

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(top = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        // Cabecera
        Text("Historial de Trabajos", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Audita tus servicios completados y cancelados", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        // Selector de Fecha
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    DatePickerDialog(context, { _, y, m, d ->
                        fechaFiltroDB = String.format("%04d-%02d-%02d", y, m + 1, d)
                        fechaFiltroVisual = String.format("%02d/%02d/%04d", d, m + 1, y)
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                border = BorderStroke(1.dp, if (fechaFiltroVisual.isNotBlank()) goldAccent else Color.Gray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = if (fechaFiltroVisual.isNotBlank()) goldAccent else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (fechaFiltroVisual.isBlank()) "Filtrar por fecha" else "Fecha: $fechaFiltroVisual",
                    color = if (fechaFiltroVisual.isNotBlank()) goldAccent else Color.White
                )
            }

            if (fechaFiltroVisual.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        fechaFiltroDB = ""
                        fechaFiltroVisual = ""
                    },
                    modifier = Modifier.background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar filtro", tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Citas
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = goldAccent)
            }
        } else if (citasHistorial.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (fechaFiltroDB.isNotBlank()) "No hay trabajos registrados en esta fecha." else "Tu historial está vacío.",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp), // Espacio para el Bottom Navigation
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(citasHistorial) { cita ->
                    HistorialCitaItem(
                        cita = cita,
                        cardBackground = cardBackground,
                        goldAccent = goldAccent,
                        highlightBackground = highlightBackground
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialCitaItem(
    cita: Cita,
    cardBackground: Color,
    goldAccent: Color,
    highlightBackground: Color
) {
    // Colores por estado
    val esCancelada = cita.estado == "cancelada"
    val estadoColor = if (esCancelada) Color(0xFFEF5350) else Color(0xFF4CAF50)
    val estadoFondo = if (esCancelada) Color(0xFF3B1E1E) else Color(0xFF1B3320)
    val estadoTexto = if (esCancelada) "CANCELADA" else "COMPLETADA"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fecha y Hora
            Column(modifier = Modifier.width(85.dp)) {
                // Formateamos YYYY-MM-DD a DD/MM
                val partesFecha = cita.fecha.split("-")
                val fechaCorta = if (partesFecha.size == 3) "${partesFecha[2]}/${partesFecha[1]}" else cita.fecha

                Text(text = fechaCorta, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = cita.hora, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Caja de Información
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(highlightBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cita.clienteNombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(text = "S/. ${cita.precio}", color = goldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = cita.servicioTitulo, color = Color.LightGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Etiqueta de Estado
                    Box(
                        modifier = Modifier
                            .background(estadoFondo, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = estadoTexto, color = estadoColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}