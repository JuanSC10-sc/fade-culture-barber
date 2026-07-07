package com.fadeculture.barber.ui.screens.barber

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fadeculture.barber.data.model.Cita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BarberHomeScreen(
    navController: NavHostController,
    onNavigateTab: (String) -> Unit
) {
    // Paleta de colores
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)
    val highlightBackground = Color(0xFF2A2415)

    val scrollState = rememberScrollState()

    // VARIABLES DE ESTADO
    var nombreBarbero by remember { mutableStateOf("...") }
    var proximaCita by remember { mutableStateOf<Cita?>(null) }
    var cargandoCita by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // 1. Buscamos el nombre del barbero
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nombreBarbero = document.getString("nombres") ?: "Barbero"
                    } else {
                        nombreBarbero = "Barbero"
                    }
                }
                .addOnFailureListener {
                    nombreBarbero = "Barbero"
                }

            // 2. Buscamos el próximo cliente pendiente para este barbero
            db.collection("citas")
                .whereEqualTo("barberoId", userId)
                .whereEqualTo("estado", "pendiente")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val listaCitas = snapshot.documents.map { doc ->
                            Cita(
                                id = doc.id,
                                clienteNombre = doc.getString("clienteNombre") ?: "Cliente",
                                servicioTitulo = doc.getString("servicioTitulo") ?: "",
                                fecha = doc.getString("fecha") ?: "",
                                hora = doc.getString("hora") ?: "",
                                precio = doc.getDouble("precio") ?: 0.0,
                                estado = "pendiente",
                                barberoNombre = nombreBarbero
                            )
                        }

                        // Ordenamos cronológicamente para obtener la cita más inmediata
                        proximaCita = listaCitas.minByOrNull { "${it.fecha}T${it.hora}" }
                    } else {
                        proximaCita = null
                    }
                    cargandoCita = false
                }
                .addOnFailureListener {
                    proximaCita = null
                    cargandoCita = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hola, $nombreBarbero 👋",
                    color = goldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Panel de Control - Especialista",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LISTA DE ACCIONES DEL BARBERO
        BarberActionRowCard(
            title = "Agenda Diaria",
            subtitle = "Revisa y atiende tus\ncitas de hoy.",
            icon = Icons.Default.EventNote,
            cardBackground = cardBackground,
            goldAccent = goldAccent,
            onClick = { onNavigateTab("barber_agenda") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BarberActionRowCard(
            title = "Historial de Trabajos",
            subtitle = "Audita los servicios que\nhas completado.",
            icon = Icons.Default.Assignment,
            cardBackground = cardBackground,
            goldAccent = goldAccent,
            onClick = { onNavigateTab("barber_historial") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BarberActionRowCard(
            title = "Mi Perfil",
            subtitle = "Gestiona tus datos\ny tu cuenta de acceso.",
            icon = Icons.Default.Person,
            cardBackground = cardBackground,
            goldAccent = goldAccent,
            onClick = { onNavigateTab("barber_perfil") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // TARJETA DE PRÓXIMO CLIENTE DESTACADA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTab("barber_agenda") },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = highlightBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Tu próximo cliente",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (cargandoCita) {
                    Text(
                        text = "Sincronizando agenda...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                } else if (proximaCita != null) {
                    val cita = proximaCita!!

                    val partesFecha = cita.fecha.split("-")
                    val fechaFormateada = if (partesFecha.size == 3) "${partesFecha[2]}/${partesFecha[1]}/${partesFecha[0]}" else cita.fecha

                    Text(
                        text = "$fechaFormateada - ${cita.hora} hrs",
                        color = goldAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cliente: ${cita.clienteNombre}\nServicio: ${cita.servicioTitulo}",
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "Turnos libres por ahora",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No tienes citas pendientes asignadas.",
                        color = goldAccent,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun BarberActionRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    cardBackground: Color,
    goldAccent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color = Color(0xFF2A2415), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = goldAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}