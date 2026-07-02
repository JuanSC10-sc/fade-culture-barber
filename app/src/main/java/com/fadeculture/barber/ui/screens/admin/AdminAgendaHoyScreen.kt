package com.fadeculture.barber.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Modelo temporal solo para mostrar el resumen
data class ResumenTurno(
    val nombreBarbero: String,
    val tieneManana: Boolean,
    val mInicio: String,
    val mFin: String,
    val tieneTarde: Boolean,
    val tInicio: String,
    val tFin: String
)

@Composable
fun AdminAgendaHoyScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    // Obtenemos la fecha exacta de HOY en formato YYYY-MM-DD
    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var listaResumenes by remember { mutableStateOf<List<ResumenTurno>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("barberos").whereEqualTo("estadoActivo", true).get()
            .addOnSuccessListener { barberosSnapshot ->
                val resumenesTemp = mutableListOf<ResumenTurno>()
                var consultasPendientes = barberosSnapshot.size()

                if (consultasPendientes == 0) isLoading = false

                for (doc in barberosSnapshot) {
                    val nombre = doc.getString("nombres") ?: "Desconocido"
                    db.collection("barberos").document(doc.id).collection("horarios_diarios").document(fechaHoy).get()
                        .addOnSuccessListener { horarioDoc ->
                            if (horarioDoc.exists()) {
                                val tieneM = horarioDoc.getBoolean("turnoMananaActivo") ?: false
                                val tieneT = horarioDoc.getBoolean("turnoTardeActivo") ?: false

                                // Si tiene al menos un turno habilitado, lo agregamos a la lista
                                if (tieneM || tieneT) {
                                    resumenesTemp.add(
                                        ResumenTurno(
                                            nombreBarbero = nombre,
                                            tieneManana = tieneM,
                                            mInicio = horarioDoc.getString("tMInicio") ?: "",
                                            mFin = horarioDoc.getString("tMFin") ?: "",
                                            tieneTarde = tieneT,
                                            tInicio = horarioDoc.getString("tTInicio") ?: "",
                                            tFin = horarioDoc.getString("tTFin") ?: ""
                                        )
                                    )
                                }
                            }
                            consultasPendientes--
                            if (consultasPendientes == 0) {
                                listaResumenes = resumenesTemp.sortedBy { it.nombreBarbero }
                                isLoading = false
                            }
                        }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Today, contentDescription = null, tint = goldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Agenda Global de Hoy", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(text = "Fecha: $fechaHoy", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Cargando horarios...", color = Color.Gray)
            }
        } else if (listaResumenes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Ningún barbero tiene turno programado para hoy.", color = Color.Gray)
            }
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
                            Spacer(modifier = Modifier.height(12.dp))

                            if (resumen.tieneManana) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFB300))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Mañana: ${resumen.mInicio} - ${resumen.mFin}", color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            if (resumen.tieneTarde) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFF5C6BC0))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Tarde: ${resumen.tInicio} - ${resumen.tFin}", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}