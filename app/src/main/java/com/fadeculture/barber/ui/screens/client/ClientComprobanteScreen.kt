package com.fadeculture.barber.ui.screens.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fadeculture.barber.data.model.Cita
import com.fadeculture.barber.ui.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ClientComprobanteScreen(navController: NavHostController, citaId: String) {
    val db = FirebaseFirestore.getInstance()
    var citaConfirmada by remember { mutableStateOf<Cita?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    // Consultamos la cita exacta recién creada
    LaunchedEffect(citaId) {
        db.collection("citas").document(citaId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    citaConfirmada = Cita(
                        id = doc.id,
                        clienteNombre = doc.getString("clienteNombre") ?: "",
                        clienteApellidos = doc.getString("clienteApellidos") ?: "",
                        barberoNombre = doc.getString("barberoNombre") ?: "",
                        servicioTitulo = doc.getString("servicioTitulo") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        fecha = doc.getString("fecha") ?: "",
                        hora = doc.getString("hora") ?: ""
                    )
                }
                cargando = false
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(darkBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(color = goldAccent)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Generando comprobante...", color = Color.White)
        } else if (citaConfirmada != null) {
            val cita = citaConfirmada!!

            Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = goldAccent, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("¡Reserva Exitosa!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Tu espacio ha sido separado.", fontSize = 14.sp, color = Color.Gray)

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

                    Text("Cliente: ${cita.clienteNombre} ${cita.clienteApellidos}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Servicio: ${cita.servicioTitulo}", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Especialista: ${cita.barberoNombre}", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fecha: ${cita.fecha}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Hora: ${cita.hora} hrs", color = Color.LightGray, fontSize = 14.sp)
                    }

                    Divider(color = Color(0xFF2E2E2E), modifier = Modifier.padding(vertical = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("S/. ${cita.precio}", color = goldAccent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 👇 Mensaje solicitado sobre el correo
            Text(
                text = "Se ha enviado una copia de este comprobante al correo electrónico registrado.",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    // Volvemos al inicio limpiando el historial para no retroceder al comprobante
                    navController.navigate(Screen.ClientMain.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Volver al Inicio", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        } else {
            Text("Error al cargar el comprobante.", color = Color.Red)
        }
    }
}