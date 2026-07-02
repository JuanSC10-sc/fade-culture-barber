package com.fadeculture.barber.ui.screens.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadeculture.barber.domain.model.BloqueoAgenda
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Composable
fun AdminAgendaScreen(
    barberId: String,
    barberName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Colores premium restaurados
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Horario del Día", "Bloqueos Temporales")
    val calendar = Calendar.getInstance()

    // --- ESTADOS: HORARIO DIARIO ---
    var fechaSeleccionada by remember {
        mutableStateOf(String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
    }

    var turnoMananaActivo by remember { mutableStateOf(false) }
    var tMInicio by remember { mutableStateOf("08:00") }
    var tMFin by remember { mutableStateOf("12:00") }

    var turnoTardeActivo by remember { mutableStateOf(false) }
    var tTInicio by remember { mutableStateOf("14:00") }
    var tTFin by remember { mutableStateOf("19:00") }

    // --- ESTADOS: BLOQUEOS TEMPORALES ---
    var fechaBloqueo by remember { mutableStateOf("") }
    var horaBloqueoInicio by remember { mutableStateOf("") }
    var horaBloqueoFin by remember { mutableStateOf("") }
    var motivoBloqueo by remember { mutableStateOf("") }
    var listaBloqueos by remember { mutableStateOf<List<BloqueoAgenda>>(emptyList()) }

    // 1. Cargar el Horario del Día seleccionado
    LaunchedEffect(fechaSeleccionada, barberId) {
        db.collection("barberos").document(barberId)
            .collection("horarios_diarios").document(fechaSeleccionada).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    turnoMananaActivo = doc.getBoolean("turnoMananaActivo") ?: false
                    tMInicio = doc.getString("tMInicio") ?: "08:00"
                    tMFin = doc.getString("tMFin") ?: "12:00"

                    turnoTardeActivo = doc.getBoolean("turnoTardeActivo") ?: false
                    tTInicio = doc.getString("tTInicio") ?: "14:00"
                    tTFin = doc.getString("tTFin") ?: "19:00"
                } else {
                    turnoMananaActivo = false
                    turnoTardeActivo = false
                    tMInicio = "08:00"
                    tMFin = "12:00"
                    tTInicio = "14:00"
                    tTFin = "19:00"
                }
            }
    }

    // 2. Escuchar la subcolección de Bloqueos
    LaunchedEffect(barberId) {
        db.collection("barberos").document(barberId).collection("bloqueos")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    listaBloqueos = snapshot.documents.map { doc ->
                        BloqueoAgenda(
                            id = doc.id,
                            fecha = doc.getString("fecha") ?: "",
                            horaInicio = doc.getString("horaInicio") ?: "",
                            horaFin = doc.getString("horaFin") ?: "",
                            motivo = doc.getString("motivo") ?: ""
                        )
                    }.sortedBy { it.fecha }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Agenda de Barbero", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = barberName, color = goldAccent, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    unselectedContentColor = Color.Gray,
                    selectedContentColor = goldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("1. Seleccionar Fecha a programar:", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                fechaSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = goldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = fechaSeleccionada, color = goldAccent, fontSize = 16.sp)
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("2. Configurar Turnos:", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))

                            // TURNO MAÑANA
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = turnoMananaActivo,
                                    onCheckedChange = { turnoMananaActivo = it },
                                    colors = CheckboxDefaults.colors(checkedColor = goldAccent, checkmarkColor = Color.Black, uncheckedColor = Color.Gray)
                                )
                                Text("Habilitar Turno Mañana", color = Color.White)
                            }

                            if (turnoMananaActivo) {
                                Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    OutlinedButton(onClick = {
                                        TimePickerDialog(context, { _, h, m -> tMInicio = String.format("%02d:%02d", h, m) }, 8, 0, true).show()
                                    }, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text("De: $tMInicio", color = Color.LightGray) }

                                    OutlinedButton(onClick = {
                                        TimePickerDialog(context, { _, h, m -> tMFin = String.format("%02d:%02d", h, m) }, 12, 0, true).show()
                                    }, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text("Hasta: $tMFin", color = Color.LightGray) }
                                }
                            }

                            // TURNO TARDE
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = turnoTardeActivo,
                                    onCheckedChange = { turnoTardeActivo = it },
                                    colors = CheckboxDefaults.colors(checkedColor = goldAccent, checkmarkColor = Color.Black, uncheckedColor = Color.Gray)
                                )
                                Text("Habilitar Turno Tarde", color = Color.White)
                            }

                            if (turnoTardeActivo) {
                                Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    OutlinedButton(onClick = {
                                        TimePickerDialog(context, { _, h, m -> tTInicio = String.format("%02d:%02d", h, m) }, 14, 0, true).show()
                                    }, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text("De: $tTInicio", color = Color.LightGray) }

                                    OutlinedButton(onClick = {
                                        TimePickerDialog(context, { _, h, m -> tTFin = String.format("%02d:%02d", h, m) }, 19, 0, true).show()
                                    }, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text("Hasta: $tTFin", color = Color.LightGray) }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val horarioMap = mapOf<String, Any>(
                                        "turnoMananaActivo" to turnoMananaActivo,
                                        "tMInicio" to tMInicio,
                                        "tMFin" to tMFin,
                                        "turnoTardeActivo" to turnoTardeActivo,
                                        "tTInicio" to tTInicio,
                                        "tTFin" to tTFin
                                    )
                                    db.collection("barberos").document(barberId)
                                        .collection("horarios_diarios").document(fechaSeleccionada)
                                        .set(horarioMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Horario guardado para $fechaSeleccionada", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Guardar Horario del Día", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Agregar Bloqueo de Horario", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            fechaBloqueo = String.format("%04d-%02d-%02d", y, m + 1, d)
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }, modifier = Modifier.fillMaxWidth(), border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent)) {
                        Text(if (fechaBloqueo.isEmpty()) "Seleccionar Fecha" else "Fecha: $fechaBloqueo", color = goldAccent)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = {
                            TimePickerDialog(context, { _, h, m -> horaBloqueoInicio = String.format("%02d:%02d", h, m) }, 13, 0, true).show()
                        }, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text(if (horaBloqueoInicio.isEmpty()) "Hora Inicio" else horaBloqueoInicio, color = Color.LightGray) }

                        OutlinedButton(onClick = {
                            TimePickerDialog(context, { _, h, m -> horaBloqueoFin = String.format("%02d:%02d", h, m) }, 14, 0, true).show()
                        }, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) { Text(if (horaBloqueoFin.isEmpty()) "Hora Fin" else horaBloqueoFin, color = Color.LightGray) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = motivoBloqueo,
                        onValueChange = { motivoBloqueo = it },
                        label = { Text("Motivo (Ej: Almuerzo)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldAccent, unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedLabelColor = goldAccent, unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (fechaBloqueo.isBlank() || horaBloqueoInicio.isBlank() || horaBloqueoFin.isBlank() || motivoBloqueo.isBlank()) {
                                Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val bloqueoMap = mapOf<String, Any>(
                                "fecha" to fechaBloqueo,
                                "horaInicio" to horaBloqueoInicio,
                                "horaFin" to horaBloqueoFin,
                                "motivo" to motivoBloqueo
                            )

                            db.collection("barberos").document(barberId).collection("bloqueos")
                                .add(bloqueoMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Bloqueo registrado", Toast.LENGTH_SHORT).show()
                                    horaBloqueoInicio = ""
                                    horaBloqueoFin = ""
                                    motivoBloqueo = ""
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Registrar Bloqueo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listaBloqueos) { bloqueo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFEF5350))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = bloqueo.motivo, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "${bloqueo.fecha} • ${bloqueo.horaInicio} a ${bloqueo.horaFin}", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = {
                                db.collection("barberos").document(barberId)
                                    .collection("bloqueos").document(bloqueo.id).delete()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}