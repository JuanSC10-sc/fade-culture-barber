package com.fadeculture.barber.ui.screens.admin

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.fadeculture.barber.domain.model.Barbero
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminPersonalScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Colores premium
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    // Estados de navegación interna de la pantalla
    var isFormOpen by remember { mutableStateOf(false) }
    var barberoSeleccionadoEdicion by remember { mutableStateOf<Barbero?>(null) }
    var barberoAgendaSeleccionado by remember { mutableStateOf<Barbero?>(null) } // Controla la pantalla de agenda

    // Estados para los campos del formulario
    var nombres by remember { mutableStateOf("") }
    var especialidad by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Lista de barberos traída de Firestore
    var listaBarberos by remember { mutableStateOf<List<Barbero>>(emptyList()) }

    // Escuchar los barberos de Firestore en tiempo real
    LaunchedEffect(Unit) {
        db.collection("barberos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val barberos = snapshot.documents.map { doc ->
                        Barbero(
                            id = doc.id,
                            nombres = doc.getString("nombres") ?: "",
                            especialidad = doc.getString("especialidad") ?: "",
                            fotoUrl = doc.getString("fotoUrl") ?: "",
                            estadoActivo = doc.getBoolean("estadoActivo") ?: true
                        )
                    }
                    listaBarberos = barberos
                }
            }
    }

    // Efecto para rellenar campos si se va a editar
    LaunchedEffect(barberoSeleccionadoEdicion) {
        if (barberoSeleccionadoEdicion != null) {
            nombres = barberoSeleccionadoEdicion!!.nombres
            especialidad = barberoSeleccionadoEdicion!!.especialidad
            fotoUrl = barberoSeleccionadoEdicion!!.fotoUrl
        } else {
            nombres = ""
            especialidad = ""
            fotoUrl = ""
        }
    }

    Scaffold(
        floatingActionButton = {
            if (!isFormOpen && barberoAgendaSeleccionado == null) {
                FloatingActionButton(
                    onClick = {
                        barberoSeleccionadoEdicion = null
                        isFormOpen = true
                    },
                    containerColor = goldAccent,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Barbero")
                }
            }
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(darkBackground)
        ) {
            if (barberoAgendaSeleccionado != null) {
                // --- CAPA A: PANTALLA DE AGENDA DE BLOQUEOS ---
                AdminAgendaScreen(
                    barberId = barberoAgendaSeleccionado!!.id,
                    barberName = barberoAgendaSeleccionado!!.nombres,
                    onBack = { barberoAgendaSeleccionado = null }
                )
            } else if (isFormOpen) {
                // --- CAPA B: FORMULARIO DE AGREGAR / EDITAR BARBERO ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { isFormOpen = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (barberoSeleccionadoEdicion == null) "Nuevo Barbero" else "Editar Barbero",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AdminTextField(value = nombres, onValueChange = { nombres = it }, label = "Nombres y Apellidos", goldAccent = goldAccent)
                    Spacer(modifier = Modifier.height(16.dp))
                    AdminTextField(value = especialidad, onValueChange = { especialidad = it }, label = "Especialidad (Ej: Urban, Clásico, Barba)", goldAccent = goldAccent)
                    Spacer(modifier = Modifier.height(16.dp))
                    AdminTextField(value = email, onValueChange = { email = it }, label = "Correo Electrónico ", goldAccent = goldAccent)
                    Spacer(modifier = Modifier.height(16.dp))
                    AdminTextField(value = password, onValueChange = { password = it }, label = "Contraseña", goldAccent = goldAccent)
                    Spacer(modifier = Modifier.height(16.dp))
                    AdminTextField(value = fotoUrl, onValueChange = { fotoUrl = it }, label = "URL Enlace de la Foto", goldAccent = goldAccent)

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {
                            if (nombres.isBlank() || especialidad.isBlank()) {
                                Toast.makeText(context, "Por favor complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val barberoMap = hashMapOf(
                                "nombres" to nombres,
                                "especialidad" to especialidad,
                                "fotoUrl" to fotoUrl,
                                "estadoActivo" to if (barberoSeleccionadoEdicion != null) barberoSeleccionadoEdicion!!.estadoActivo else true
                            )

                            if (barberoSeleccionadoEdicion == null) {
                                db.collection("barberos").add(barberoMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Barbero registrado con éxito", Toast.LENGTH_SHORT).show()
                                        isFormOpen = false
                                    }
                            } else {
                                db.collection("barberos").document(barberoSeleccionadoEdicion!!.id).set(barberoMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                        isFormOpen = false
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Barbero", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                // --- CAPA C: LISTADO GENERAL EN TIEMPO REAL ---
                Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    Text(text = "Lista de Personal", color = goldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Monitorea el equipo de Fade Culture", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    if (listaBarberos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No hay barberos registrados.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(listaBarberos) { barbero ->
                                BarberoCard(
                                    barbero = barbero,
                                    cardBackground = cardBackground,
                                    goldAccent = goldAccent,
                                    onEdit = {
                                        barberoSeleccionadoEdicion = barbero
                                        isFormOpen = true
                                    },
                                    onToggleStatus = {
                                        db.collection("barberos").document(barbero.id)
                                            .update("estadoActivo", !barbero.estadoActivo)
                                    },
                                    // Conectamos el botón de agenda de la tarjeta
                                    onManageAgenda = {
                                        barberoAgendaSeleccionado = barbero
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Componente de tarjeta de cada barbero ---
@Composable
fun BarberoCard(
    barbero: Barbero,
    cardBackground: Color,
    goldAccent: Color,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onManageAgenda: () -> Unit
) {
    val opacidad = if (barbero.estadoActivo) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(opacidad),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del barbero mediante Coil
            if (barbero.fotoUrl.isNotBlank()) {
                AsyncImage(
                    model = barbero.fotoUrl,
                    contentDescription = barbero.nombres,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF2A2415), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = goldAccent, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos informativos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = barbero.nombres,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (barbero.estadoActivo) barbero.especialidad else "DE BAJA (Oculto)",
                    color = if (barbero.estadoActivo) Color.Gray else Color(0xFFEF5350),
                    fontSize = 14.sp,
                    fontWeight = if (barbero.estadoActivo) FontWeight.Normal else FontWeight.Bold
                )
            }

            // Acciones: Agenda, Editar y Cambiar Estado
            Row {
                // NUEVO BOTÓN: Calendario para gestionar bloqueos
                IconButton(onClick = onManageAgenda) {
                    Icon(Icons.Default.DateRange, contentDescription = "Gestionar Agenda", tint = goldAccent)
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.LightGray)
                }

                IconButton(onClick = onToggleStatus) {
                    Icon(
                        imageVector = if (barbero.estadoActivo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Cambiar estado",
                        tint = if (barbero.estadoActivo) goldAccent else Color.Gray
                    )
                }
            }
        }
    }
}

// --- Input personalizado estilizado ---
@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    goldAccent: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = goldAccent,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = goldAccent,
            unfocusedLabelColor = Color.LightGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = goldAccent
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}