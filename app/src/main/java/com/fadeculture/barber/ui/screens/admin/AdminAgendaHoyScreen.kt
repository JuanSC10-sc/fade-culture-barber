package com.fadeculture.barber.ui.screens.admin

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

// Modelo de datos para la vista
data class ResumenBarberoCompleto(
    val nombreBarbero: String,
    val tieneTurno: Boolean,
    val mInicio: String?,
    val mFin: String?,
    val tInicio: String?,
    val tFin: String?,
    val listaBloqueos: List<BloqueoInfo>
)

data class BloqueoInfo(val motivo: String, val horaInicio: String, val horaFin: String)

@Composable
fun AdminAgendaHoyScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    var fechaSeleccionada by remember {
        mutableStateOf(String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
    }
    var listaResumenes by remember { mutableStateOf<List<ResumenBarberoCompleto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun cargarDatos() {
        isLoading = true
        db.collection("barberos").whereEqualTo("estadoActivo", true).get()
            .addOnSuccessListener { barberosSnapshot ->
                val listaTemp = mutableListOf<ResumenBarberoCompleto>()
                var pendientes = barberosSnapshot.size()

                if (pendientes == 0) isLoading = false

                for (doc in barberosSnapshot) {
                    val barberId = doc.id
                    val nombre = doc.getString("nombres") ?: "Desconocido"

                    // Obtenemos Horario y Bloqueos simultáneamente
                    db.collection("barberos").document(barberId).collection("horarios_diarios").document(fechaSeleccionada).get()
                        .addOnSuccessListener { horarioDoc ->

                            db.collection("barberos").document(barberId).collection("bloqueos")
                                .whereEqualTo("fecha", fechaSeleccionada).get()
                                .addOnSuccessListener { bloqueosSnapshot ->

                                    val bloqueos = bloqueosSnapshot.documents.map { bDoc ->
                                        BloqueoInfo(
                                            bDoc.getString("motivo") ?: "Bloqueo",
                                            bDoc.getString("horaInicio") ?: "",
                                            bDoc.getString("horaFin") ?: ""
                                        )
                                    }

                                    val tieneM = horarioDoc.getBoolean("turnoMananaActivo") ?: false
                                    val tieneT = horarioDoc.getBoolean("turnoTardeActivo") ?: false

                                    listaTemp.add(
                                        ResumenBarberoCompleto(
                                            nombreBarbero = nombre,
                                            tieneTurno = tieneM || tieneT,
                                            mInicio = if(tieneM) horarioDoc.getString("tMInicio") else null,
                                            mFin = if(tieneM) horarioDoc.getString("tMFin") else null,
                                            tInicio = if(tieneT) horarioDoc.getString("tTInicio") else null,
                                            tFin = if(tieneT) horarioDoc.getString("tTFin") else null,
                                            listaBloqueos = bloqueos
                                        )
                                    )

                                    pendientes--
                                    if (pendientes == 0) {
                                        listaResumenes = listaTemp.sortedBy { it.nombreBarbero }
                                        isLoading = false
                                    }
                                }
                        }
                }
            }
    }

    LaunchedEffect(fechaSeleccionada) { cargarDatos() }

    Column(modifier = Modifier.fillMaxSize().background(darkBackground).padding(20.dp)) {
        Text("Agenda Global", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Vista detallada por barbero y fecha", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = {
                DatePickerDialog(context, { _, y, m, d ->
                    fechaSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, goldAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = goldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Fecha: $fechaSeleccionada", color = goldAccent, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = goldAccent) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(listaResumenes) { resumen ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = resumen.nombreBarbero, color = goldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            if (!resumen.tieneTurno) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "● Sin turno programado", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                if (resumen.mInicio != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Mañana: ${resumen.mInicio} - ${resumen.mFin}", color = Color.White, fontSize = 14.sp)
                                    }
                                }
                                if (resumen.tInicio != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Tarde: ${resumen.tInicio} - ${resumen.tFin}", color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }

                            // Sección Bloqueos
                            if (resumen.listaBloqueos.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Bloqueos/Ausencias:", color = Color(0xFFEF5350), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                resumen.listaBloqueos.forEach { bloqueo ->
                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${bloqueo.motivo} (${bloqueo.horaInicio}-${bloqueo.horaFin})", color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}