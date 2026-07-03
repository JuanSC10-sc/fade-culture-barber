package com.fadeculture.barber.ui.screens.client

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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LibraryBooks
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
import com.fadeculture.barber.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ClientHomeScreen(navController: NavHostController,
                     onNavigateTab: (String) -> Unit) {
    // Paleta de colores
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)
    val highlightBackground = Color(0xFF2A2415)

    val scrollState = rememberScrollState()

    // VARIABLE DE ESTADO PARA EL NOMBRE
    var nombreCliente by remember { mutableStateOf("...") }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Buscamos en la colección
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Extraemos el campo
                        val nombreCompleto = document.getString("nombres") ?: "Cliente"

                        nombreCliente = nombreCompleto
                    } else {
                        nombreCliente = "Cliente"
                    }
                }
                .addOnFailureListener {
                    // Si falla la conexión
                    nombreCliente = "Cliente"
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
                    text = "Hola, $nombreCliente 👋",
                    color = goldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Qué deseas hacer hoy?",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LISTA DE ACCIONES
        ActionRowCard(
            title = "Generar Cita",
            subtitle = "Reserva un turno disponible\ncon tu barbero.",
            icon = Icons.Default.EditCalendar,
            cardBackground = cardBackground,
            goldAccent = goldAccent,
            onClick = { /* TODO: Navegar a reservas */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionRowCard(
            title = "Mis Citas",
            subtitle = "Ver y gestionar tus reservas\nactuales y pasadas.",
            icon = Icons.Default.LibraryBooks,
            cardBackground = cardBackground,
            goldAccent = goldAccent,
            onClick = { /* TODO: Navegar al historial */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionRowCard(
            title = "Catálogo",
            subtitle = "Descubre nuestros servicios\ny estilos de corte.",
            icon = Icons.Default.ContentCut,
            cardBackground = cardBackground,
            goldAccent = goldAccent,
            onClick = { onNavigateTab("client_catalogo") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // TARJETA DE PRÓXIMA CITA DESTACADA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = highlightBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Tu próxima cita",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lun 02 Jun - 11:00 AM", // Próximamente
                    color = goldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Barbero: Juan Pérez - Corte + barba",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

//Componente Reutilizable
@Composable
fun ActionRowCard(
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