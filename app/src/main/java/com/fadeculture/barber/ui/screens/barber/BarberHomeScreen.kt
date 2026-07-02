package com.fadeculture.barber.ui.screens.barber

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Modelo de datos para representar cada turno de la agenda
data class AgendaSlot(
    val id: String,
    val hora: String,
    val disponible: Boolean,
    val nombreCliente: String = "",
    val corteServicio: String = ""
)

@Composable
fun BarberAgendaScreen(navController: NavHostController) {
    // Paleta de colores (Conserva tu tema original)
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)
    val highlightBackground = Color(0xFF2A2415)
    val availableBackground = Color(0xFF181818)

    // Variables de estado
    var nombreBarbero by remember { mutableStateOf("...") }

    // Lista simulada (En producción, esto vendría de un ViewModel observando Firestore)
    val turnosLista = remember {
        listOf(
            AgendaSlot("1", "10:00 AM", false, "Carlos Sánchez", "Classic T"),
            AgendaSlot("2", "11:30 AM", false, "Juan Pérez", "Fade Completo"),
            AgendaSlot("3", "13:00 PM", true),
            AgendaSlot("4", "14:00 PM", false, "Pedro Gómez", "Skin Fade"),
            AgendaSlot("5", "16:00 PM", false, "David C.", "Classic Cut"),
            AgendaSlot("6", "17:30 PM", true),
            AgendaSlot("7", "19:00 PM", true),
            AgendaSlot("8", "20:30 PM", true),
            AgendaSlot("9", "22:00 PM", true)
        )
    }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nombreBarbero = document.getString("nombres") ?: "Alex"
                    } else {
                        nombreBarbero = "Alex"
                    }
                }
                .addOnFailureListener {
                    nombreBarbero = "Alex"
                }
        } else {
            nombreBarbero = "Alex"
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
                    text = "Hola, $nombreBarbero!! 👋",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mi Agenda Diaria - Miér 22 May",
                    color = goldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LISTA DE HORARIOS (Reemplaza verticalScroll por LazyColumn para optimizar rendimiento)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(turnosLista) { turno ->
                AgendaSlotItem(
                    slot = turno,
                    cardBackground = cardBackground,
                    availableBackground = availableBackground,
                    goldAccent = goldAccent,
                    highlightBackground = highlightBackground,
                    onClick = {
                        /* TODO: Abrir detalle del turno o agendar cliente manual */
                    }
                )
            }
        }
    }
}

// Componente Reutilizable para cada fila de horario
@Composable
fun AgendaSlotItem(
    slot: AgendaSlot,
    cardBackground: Color,
    availableBackground: Color,
    goldAccent: Color,
    highlightBackground: Color,
    onClick: () -> Unit
) {
    val containerColor = if (slot.disponible) availableBackground else cardBackground
    val borderColor = if (slot.disponible) Color.DarkGray.copy(alpha = 0.4f) else goldAccent.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HORA DEL TURNO
            Text(
                text = slot.hora,
                color = if (slot.disponible) Color.Gray else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(90.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // INFORMACIÓN DEL CLIENTE O DISPONIBILIDAD
            if (slot.disponible) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Disponible",
                        color = Color.DarkGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Caja destacada para turno ocupado (Estilo similar a la tarjeta derecha en la imagen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(highlightBackground)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cliente: ",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = slot.nombreCliente,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Corte: ",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = slot.corteServicio,
                                color = goldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}