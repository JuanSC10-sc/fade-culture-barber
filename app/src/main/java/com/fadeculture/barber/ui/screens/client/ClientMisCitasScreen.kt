package com.fadeculture.barber.ui.screens.client

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadeculture.barber.data.model.Cita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

@Composable
fun ClientMisCitasScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Colores
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    // Estados de datos
    var todasLasCitas by remember { mutableStateOf<List<Cita>>(emptyList()) }

    // Estado para controlar el Diálogo de Cancelación
    var citaACancelar by remember { mutableStateOf<Cita?>(null) }

    // Estados de UI
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pendientes", "Historial")

    // Estados de Filtro de Fecha
    var fechaFiltroDB by remember { mutableStateOf("") } // YYYY-MM-DD
    var fechaFiltroVisual by remember { mutableStateOf("") } // DD/MM/YYYY
    val calendar = Calendar.getInstance()

    // Escuchar citas del cliente en TIEMPO REAL
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            db.collection("citas")
                .whereEqualTo("clienteId", currentUser.uid)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .orderBy("hora", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        todasLasCitas = snapshot.documents.map { doc ->
                            Cita(
                                id = doc.id,
                                servicioTitulo = doc.getString("servicioTitulo") ?: "",
                                barberoNombre = doc.getString("barberoNombre") ?: "",
                                fecha = doc.getString("fecha") ?: "",
                                hora = doc.getString("hora") ?: "",
                                precio = doc.getDouble("precio") ?: 0.0,
                                estado = doc.getString("estado") ?: "pendiente"
                            )
                        }
                    }
                }
        }
    }

    // Lógica de Filtrado
    val citasFiltradas = todasLasCitas.filter { cita ->
        val coincideTab = if (selectedTab == 0) {
            cita.estado == "pendiente"
        } else {
            cita.estado == "finalizada" || cita.estado == "cancelada"
        }

        val coincideFecha = if (fechaFiltroDB.isNotBlank()) {
            cita.fecha == fechaFiltroDB
        } else {
            true
        }

        coincideTab && coincideFecha
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        // Cabecera
        Text("Mis Citas", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Revisa tus próximas visitas y tu historial", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        // Pestañas (Tabs)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = darkBackground,
            contentColor = goldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = goldAccent
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    unselectedContentColor = Color.Gray,
                    selectedContentColor = goldAccent
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filtro de Fecha Simplificado
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
                modifier = Modifier.weight(1f).height(45.dp),
                border = BorderStroke(1.dp, if (fechaFiltroVisual.isNotBlank()) goldAccent else Color.Gray),
                shape = RoundedCornerShape(8.dp)
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
                IconButton(onClick = {
                    fechaFiltroDB = ""
                    fechaFiltroVisual = ""
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar filtro", tint = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Citas
        if (citasFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (fechaFiltroDB.isNotBlank()) "No tienes citas para esta fecha." else "Aún no tienes citas en esta sección.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(citasFiltradas) { cita ->
                    ItemCitaCard(
                        cita = cita,
                        onCancelarClick = { citaSeleccionada ->
                            citaACancelar = citaSeleccionada
                        }
                    )
                }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE CANCELACIÓN ---
    if (citaACancelar != null) {
        AlertDialog(
            onDismissRequest = { citaACancelar = null },
            containerColor = cardBackground,
            title = { Text("Cancelar Cita", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas cancelar tu cita de ${citaACancelar!!.servicioTitulo}? El horario quedará libre para otros clientes.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        val idCita = citaACancelar!!.id
                        citaACancelar = null // Ocultamos el diálogo rápido

                        // Actualizamos Firestore: Solo cambiamos el estado
                        db.collection("citas").document(idCita).update("estado", "cancelada")
                            .addOnSuccessListener {
                                Toast.makeText(context, "Cita cancelada correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al cancelar la cita", Toast.LENGTH_SHORT).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350), contentColor = Color.White)
                ) {
                    Text("Sí, cancelar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { citaACancelar = null },
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Volver", color = Color.White)
                }
            }
        )
    }
}

// Componente Visual Simplificado con Botón de Cancelar
@Composable
fun ItemCitaCard(cita: Cita, onCancelarClick: (Cita) -> Unit) {
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    val colorEstado = when(cita.estado) {
        "pendiente" -> goldAccent
        "finalizada" -> Color(0xFF4CAF50)
        "cancelada" -> Color(0xFFEF5350)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila Superior: Servicio y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = cita.servicioTitulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = cita.estado.uppercase(), color = colorEstado, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Datos de Barbero y Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Barbero: ${cita.barberoNombre}", color = Color.LightGray, fontSize = 14.sp)
                Text(text = "S/. ${cita.precio}", color = goldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fecha y Hora
            val partesFecha = cita.fecha.split("-")
            val fechaFormateada = if (partesFecha.size == 3) "${partesFecha[2]}/${partesFecha[1]}/${partesFecha[0]}" else cita.fecha

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$fechaFormateada - ${cita.hora} hrs", color = Color.Gray, fontSize = 14.sp)
            }

            // Solo aparece si el estado es pendiente
            if (cita.estado == "pendiente") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onCancelarClick(cita) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF5350)), // Borde rojo
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar Cita", color = Color(0xFFEF5350), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}