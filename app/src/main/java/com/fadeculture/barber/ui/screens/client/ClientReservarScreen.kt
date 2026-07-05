package com.fadeculture.barber.ui.screens.client

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fadeculture.barber.data.model.Barbero
import com.fadeculture.barber.data.model.Servicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientReservarScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scrollState = rememberScrollState()

    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    var barberos by remember { mutableStateOf<List<Barbero>>(emptyList()) }
    var servicios by remember { mutableStateOf<List<Servicio>>(emptyList()) }

    var categoriaSeleccionada by remember { mutableStateOf("Corte") }
    val categorias = listOf("Corte", "Barba", "Tinte", "Ondulación", "Servicios Completos")

    var servicioSeleccionado by remember { mutableStateOf<Servicio?>(null) }
    var barberoSeleccionado by remember { mutableStateOf<Barbero?>(null) }
    var fechaDB by remember { mutableStateOf("") }
    var fechaVisual by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf("") }

    // --- NUEVOS ESTADOS DE CONTROL DE FLUJO ---
    var showConfirmDialog by remember { mutableStateOf(false) } // Pregunta de Si/No
    var showReceiptScreen by remember { mutableStateOf(false) } // Pantalla de Comprobante final
    // Guardamos los datos finales del cliente para mostrarlos en el comprobante
    var nombreClienteTicket by remember { mutableStateOf("") }
    var apellidoClienteTicket by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var horasDisponiblesVisuales by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("barberos").whereEqualTo("estadoActivo", true).get()
            .addOnSuccessListener { snapshot ->
                barberos = snapshot.documents.map { doc ->
                    Barbero(id = doc.id, nombres = doc.getString("nombres") ?: "", especialidad = doc.getString("especialidad") ?: "", fotoUrl = doc.getString("fotoUrl") ?: "")
                }
            }
        db.collection("servicios").whereEqualTo("estadoActivo", true).get()
            .addOnSuccessListener { snapshot ->
                servicios = snapshot.documents.map { doc ->
                    Servicio(id = doc.id, titulo = doc.getString("titulo") ?: "", precioSoles = doc.getDouble("precioSoles") ?: 0.0, duracionMinutos = doc.getLong("duracionMinutos")?.toInt() ?: 30, imagenUrl = doc.getString("imagenUrl") ?: "", categoria = doc.getString("categoria") ?: "Corte")
                }
            }
    }

    LaunchedEffect(fechaDB, barberoSeleccionado, servicioSeleccionado) {
        if (fechaDB.isNotBlank() && barberoSeleccionado != null && servicioSeleccionado != null) {
            val duracionServicio = servicioSeleccionado!!.duracionMinutos
            val barberoId = barberoSeleccionado!!.id

            db.collection("citas").whereEqualTo("barberoId", barberoId).whereEqualTo("fecha", fechaDB).whereEqualTo("estado", "pendiente").get()
                .addOnSuccessListener { snapshotCitas ->
                    val ocupadosPorCitas = snapshotCitas.documents.mapNotNull { doc ->
                        val horaString = doc.getString("hora") ?: return@mapNotNull null
                        val duracion = doc.getLong("duracionMinutos")?.toInt() ?: 30
                        val inicioMinutos = timeToMinutes(horaString)
                        Pair(inicioMinutos, inicioMinutos + duracion)
                    }

                    db.collection("barberos").document(barberoId).collection("bloqueos").whereEqualTo("fecha", fechaDB).get()
                        .addOnSuccessListener { snapshotBloqueos ->
                            val ocupadosPorBloqueos = snapshotBloqueos.documents.mapNotNull { doc ->
                                val inicioStr = doc.getString("horaInicio") ?: return@mapNotNull null
                                val finStr = doc.getString("horaFin") ?: return@mapNotNull null
                                Pair(timeToMinutes(inicioStr), timeToMinutes(finStr))
                            }

                            val todosLosTiemposOcupados = ocupadosPorCitas + ocupadosPorBloqueos

                            db.collection("barberos").document(barberoId).collection("horarios_diarios").document(fechaDB).get()
                                .addOnSuccessListener { doc ->
                                    val horasValidas = mutableListOf<String>()
                                    if (doc.exists()) {
                                        val tMActivo = doc.getBoolean("turnoMananaActivo") ?: false
                                        val tTActivo = doc.getBoolean("turnoTardeActivo") ?: false

                                        if (tMActivo) horasValidas.addAll(generarBloquesLibres(doc.getString("tMInicio") ?: "08:00", doc.getString("tMFin") ?: "12:00", duracionServicio, todosLosTiemposOcupados))
                                        if (tTActivo) horasValidas.addAll(generarBloquesLibres(doc.getString("tTInicio") ?: "14:00", doc.getString("tTFin") ?: "19:00", duracionServicio, todosLosTiemposOcupados))
                                    }
                                    horasDisponiblesVisuales = horasValidas
                                    horaSeleccionada = ""
                                }
                        }
                }
        }
    }

    val serviciosFiltrados = servicios.filter { it.categoria == categoriaSeleccionada }

    // --- CAPA 1: FORMULARIO PRINCIPAL DE RESERVA ---
    if (!showReceiptScreen) {
        Column(
            modifier = Modifier.fillMaxSize().background(darkBackground).padding(24.dp).verticalScroll(scrollState)
        ) {
            Text("Agendar Cita", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Selecciona tus preferencias", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Servicio
            Text("1. Selecciona el Servicio:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias) { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    Box(
                        modifier = Modifier.background(color = if (isSelected) goldAccent else cardBackground, shape = RoundedCornerShape(20.dp)).clickable { categoriaSeleccionada = categoria }.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text(text = categoria, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (serviciosFiltrados.isEmpty()) {
                Text("No hay servicios en esta categoría.", color = Color.Gray, fontSize = 13.sp)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(serviciosFiltrados) { servicio ->
                        val isSelected = servicioSeleccionado?.id == servicio.id
                        Card(
                            modifier = Modifier.width(160.dp).height(180.dp).clickable { servicioSeleccionado = servicio },
                            colors = CardDefaults.cardColors(containerColor = cardBackground),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) goldAccent else Color(0xFF2E2E2E)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (servicio.imagenUrl.isNotBlank()) AsyncImage(model = servicio.imagenUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(90.dp), contentScale = ContentScale.Crop)
                                else Box(modifier = Modifier.fillMaxWidth().height(90.dp).background(Color(0xFF2A2415)), contentAlignment = Alignment.Center) { Icon(Icons.Default.ContentCut, contentDescription = null, tint = goldAccent, modifier = Modifier.size(32.dp)) }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = servicio.titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${servicio.duracionMinutos} min • S/. ${servicio.precioSoles}", color = goldAccent, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Barbero
            Text("2. Selecciona tu Barbero:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(barberos) { barbero ->
                    val isSelected = barberoSeleccionado?.id == barbero.id
                    Card(
                        modifier = Modifier.width(130.dp).clickable { barberoSeleccionado = barbero },
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) goldAccent else Color(0xFF2E2E2E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (barbero.fotoUrl.isNotBlank()) AsyncImage(model = barbero.fotoUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            else Box(modifier = Modifier.size(60.dp).background(Color(0xFF2A2415), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = goldAccent) }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = barbero.nombres, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = barbero.especialidad, color = goldAccent, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Fecha
            Text("3. Elige la Fecha:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    DatePickerDialog(context, { _, y, m, d ->
                        fechaDB = String.format("%04d-%02d-%02d", y, m + 1, d)
                        fechaVisual = String.format("%02d/%02d/%04d", d, m + 1, y)
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(1.dp, if (fechaVisual.isNotBlank()) goldAccent else Color.Gray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = goldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (fechaVisual.isBlank()) "Seleccionar del Calendario" else fechaVisual, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Hora
            if (fechaDB.isNotBlank() && barberoSeleccionado != null && servicioSeleccionado != null) {
                Text("4. Horarios Disponibles:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Cálculo exacto basado en la duración del servicio", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (horasDisponiblesVisuales.isEmpty()) {
                    Text("No hay turnos disponibles que calcen con la duración de este servicio.", color = Color(0xFFEF5350), fontSize = 13.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(horasDisponiblesVisuales) { horaItem ->
                            val esSeleccionado = horaItem == horaSeleccionada
                            Box(
                                modifier = Modifier.background(color = if (esSeleccionado) goldAccent else cardBackground, shape = RoundedCornerShape(12.dp)).clickable { horaSeleccionada = horaItem }.padding(horizontal = 18.dp, vertical = 12.dp)
                            ) { Text(text = horaItem, color = if (esSeleccionado) Color.Black else Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            // Botón Generar Cita (Solo abre el diálogo de confirmación)
            Button(
                onClick = {
                    if (barberoSeleccionado == null || servicioSeleccionado == null || fechaDB.isBlank() || horaSeleccionada.isBlank()) {
                        Toast.makeText(context, "Por favor complete todos los pasos", Toast.LENGTH_SHORT).show()
                    } else {
                        showConfirmDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Generar Cita", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // --- CAPA 2: DIÁLOGO DE CONFIRMACIÓN DE CITA (SÍ / NO) ---
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false }, // Si presiona fuera, solo se cierra
            containerColor = cardBackground,
            title = { Text("Confirmar Reserva", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas agendar tu cita para el ${fechaVisual} a las ${horaSeleccionada}hrs?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        val currentUid = auth.currentUser?.uid ?: ""

                        // 1. Ocultamos el diálogo inmediatamente para dar sensación de rapidez
                        showConfirmDialog = false

                        // 2. Traemos Nombre Y Apellido del cliente antes de guardar
                        db.collection("usuarios").document(currentUid).get()
                            .addOnSuccessListener { userDoc ->
                                val nombreReal = userDoc.getString("nombres") ?: "Cliente"
                                val apellidoReal = userDoc.getString("apellidos") ?: ""

                                // Guardamos temporalmente para pasarlos al ticket visual
                                nombreClienteTicket = nombreReal
                                apellidoClienteTicket = apellidoReal

                                val nuevaCita = hashMapOf(
                                    "clienteId" to currentUid,
                                    "clienteNombre" to nombreReal,
                                    "clienteApellidos" to apellidoReal, // Guardamos el apellido en BD
                                    "barberoId" to barberoSeleccionado!!.id,
                                    "barberoNombre" to barberoSeleccionado!!.nombres,
                                    "servicioId" to servicioSeleccionado!!.id,
                                    "servicioTitulo" to servicioSeleccionado!!.titulo,
                                    "duracionMinutos" to servicioSeleccionado!!.duracionMinutos,
                                    "precio" to servicioSeleccionado!!.precioSoles,
                                    "fecha" to fechaDB,
                                    "hora" to horaSeleccionada,
                                    "estado" to "pendiente"
                                )

                                db.collection("citas").add(nuevaCita)
                                    .addOnSuccessListener {
                                        // 3. ¡Éxito! Mostramos la pantalla final de comprobante
                                        showReceiptScreen = true
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error de conexión al procesar reserva", Toast.LENGTH_SHORT).show()
                                    }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black)
                ) { Text("Sí, confirmar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false }, // No hace nada, solo cierra el aviso
                    border = BorderStroke(1.dp, Color.Gray)
                ) { Text("No, cancelar", color = Color.White) }
            }
        )
    }

    // --- CAPA 3: PANTALLA DE COMPROBANTE FINAL (DIGITAL TICKET) ---
    if (showReceiptScreen) {
        Column(
            modifier = Modifier.fillMaxSize().background(darkBackground).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = goldAccent, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("¡Reserva Exitosa!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Hemos preparado tu espacio.", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de Ticket
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, goldAccent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = goldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPROBANTE DE CITA", color = goldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Divider(color = Color(0xFF2E2E2E), modifier = Modifier.padding(vertical = 12.dp))

                    Text("Cliente: $nombreClienteTicket $apellidoClienteTicket", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Servicio: ${servicioSeleccionado?.titulo}", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Especialista: ${barberoSeleccionado?.nombres}", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fecha: $fechaVisual", color = Color.LightGray, fontSize = 14.sp)
                        Text("Hora: $horaSeleccionada", color = Color.LightGray, fontSize = 14.sp)
                    }

                    Divider(color = Color(0xFF2E2E2E), modifier = Modifier.padding(vertical = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("S/. ${servicioSeleccionado?.precioSoles}", color = goldAccent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    // Reiniciamos todas las variables para que quede listo para una nueva reserva
                    showReceiptScreen = false
                    fechaDB = ""
                    fechaVisual = ""
                    horaSeleccionada = ""
                    servicioSeleccionado = null
                    barberoSeleccionado = null
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Hacer otra reserva", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}

// ... [Aquí van las funciones timeToMinutes, minutesToTime y generarBloquesLibres idénticas a las del paso anterior] ...
fun timeToMinutes(time: String): Int {
    val partes = time.split(":")
    if (partes.size != 2) return 0
    return (partes[0].toIntOrNull() ?: 0) * 60 + (partes[1].toIntOrNull() ?: 0)
}
fun minutesToTime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return String.format("%02d:%02d", h, m)
}
fun generarBloquesLibres(inicioTurno: String, finTurno: String, duracionNuevoServicio: Int, tiemposOcupados: List<Pair<Int, Int>>): List<String> {
    val bloquesValidos = mutableListOf<String>()
    val inicioShift = timeToMinutes(inicioTurno)
    val finShift = timeToMinutes(finTurno)
    val intervaloGeneracion = 30
    var tiempoActual = inicioShift
    while (tiempoActual + duracionNuevoServicio <= finShift) {
        val inicioPropuesto = tiempoActual
        val finPropuesto = tiempoActual + duracionNuevoServicio
        var hayColision = false
        for (ocupado in tiemposOcupados) {
            if (inicioPropuesto < ocupado.second && finPropuesto > ocupado.first) {
                hayColision = true; break
            }
        }
        if (!hayColision) bloquesValidos.add(minutesToTime(inicioPropuesto))
        tiempoActual += intervaloGeneracion
    }
    return bloquesValidos
}