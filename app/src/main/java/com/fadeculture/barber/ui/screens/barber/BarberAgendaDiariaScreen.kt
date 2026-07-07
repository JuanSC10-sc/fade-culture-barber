package com.fadeculture.barber.ui.screens.barber

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BarberAgendaScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Paleta de colores original de la plantilla
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)
    val highlightBackground = Color(0xFF2A2415)

    // Variables de estado
    var nombreBarbero by remember { mutableStateOf("...") }
    var citasDeHoy by remember { mutableStateOf<List<Cita>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Calcular la fecha de hoy dinámicamente
    val calendar = Calendar.getInstance()
    val hoyDB = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))

    // Formato visual amigable (Ej: Mié 06 Jul)
    val formatoVisual = SimpleDateFormat("EEE dd MMM", Locale("es", "ES"))
    val hoyVisual = formatoVisual.format(calendar.time).replaceFirstChar { it.uppercase() }

    LaunchedEffect(userId) {
        if (userId != null) {
            // 1. Obtener nombre del barbero
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nombreBarbero = document.getString("nombres") ?: "Barbero"
                    }
                }

            // 2. Escuchar en tiempo real las citas de HOY para este barbero
            db.collection("citas")
                .whereEqualTo("barberoId", userId)
                .whereEqualTo("fecha", hoyDB)
                // Evitamos canceladas para limpiar la agenda
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val listaMapeada = snapshot.documents.mapNotNull { doc ->
                            val estado = doc.getString("estado") ?: "pendiente"
                            if (estado == "cancelada") return@mapNotNull null // Ignoramos las canceladas

                            Cita(
                                id = doc.id,
                                clienteNombre = doc.getString("clienteNombre") ?: "Cliente",
                                servicioTitulo = doc.getString("servicioTitulo") ?: "Servicio",
                                fecha = doc.getString("fecha") ?: "",
                                hora = doc.getString("hora") ?: "",
                                estado = estado,
                                precio = doc.getDouble("precio") ?: 0.0,
                                barberoNombre = doc.getString("barberoNombre") ?: ""
                            )
                        }

                        // Ordenamos cronológicamente usando Kotlin para evitar problemas de índices en Firebase
                        citasDeHoy = listaMapeada.sortedBy { it.hora }
                        cargando = false
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // CABECERA SUPERIOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FADE CULTURE BARBER ✂️",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hola, $nombreBarbero 👋",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mi Agenda Diaria - $hoyVisual",
                    color = goldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LISTA DE HORARIOS REALES
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = goldAccent)
            }
        } else if (citasDeHoy.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes citas programadas para hoy.", color = Color.Gray, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(citasDeHoy) { cita ->
                    AgendaCitaItem(
                        cita = cita,
                        cardBackground = cardBackground,
                        goldAccent = goldAccent,
                        highlightBackground = highlightBackground,
                        onEstadoChange = { idCita, nuevoEstado ->
                            // Lógica de actualización a Firestore
                            db.collection("citas").document(idCita)
                                .update("estado", nuevoEstado)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                                }
                        }
                    )
                }
            }
        }
    }
}

// Componente adaptado a datos reales y máquina de estados
@Composable
fun AgendaCitaItem(
    cita: Cita,
    cardBackground: Color,
    goldAccent: Color,
    highlightBackground: Color,
    onEstadoChange: (String, String) -> Unit
) {
    // Definimos bordes y fondos dependiendo del estado de la cita
    val borderColor = when (cita.estado) {
        "pendiente" -> goldAccent.copy(alpha = 0.5f)
        "en_proceso" -> Color(0xFF4CAF50).copy(alpha = 0.5f) // Verde brillante
        "finalizada" -> Color.DarkGray
        else -> Color.DarkGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Usamos defaultMinSize en lugar de height fijo para que el botón pueda expandir la tarjeta
            .defaultMinSize(minHeight = 76.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HORA DEL TURNO
            Text(
                text = cita.hora,
                color = if (cita.estado == "finalizada") Color.Gray else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(70.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // CAJA DE INFORMACIÓN Y BOTONES
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (cita.estado == "finalizada") Color(0xFF181818) else highlightBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    // Datos del Cliente y Servicio
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Cliente: ", color = Color.Gray, fontSize = 13.sp)
                        Text(
                            text = cita.clienteNombre,
                            color = if (cita.estado == "finalizada") Color.Gray else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Servicio: ", color = Color.Gray, fontSize = 13.sp)
                        Text(
                            text = cita.servicioTitulo,
                            color = if (cita.estado == "finalizada") Color.Gray else goldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // MÁQUINA DE ESTADOS (Botones dinámicos)
                    if (cita.estado != "finalizada") {
                        Spacer(modifier = Modifier.height(10.dp))

                        if (cita.estado == "pendiente") {
                            Button(
                                onClick = { onEstadoChange(cita.id, "en_proceso") },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Iniciar Atención", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (cita.estado == "en_proceso") {
                            Button(
                                onClick = { onEstadoChange(cita.id, "finalizada") },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Finalizar Atención", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Estado Finalizado
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Completado ✅", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}