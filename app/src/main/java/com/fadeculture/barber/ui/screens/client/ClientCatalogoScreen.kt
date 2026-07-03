package com.fadeculture.barber.ui.screens.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fadeculture.barber.data.model.Barbero
import com.fadeculture.barber.data.model.Servicio
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CatalogoScreen() {
    val db = FirebaseFirestore.getInstance()

    // Paleta de colores Fade Culture
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    var listaServicios by remember { mutableStateOf<List<Servicio>>(emptyList()) }
    var listaBarberos by remember { mutableStateOf<List<Barbero>>(emptyList()) }

    // Chips sincronizados exactamente con las opciones del Admin
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Corte", "Barba", "Tinte", "Ondulación", "Servicios Completos")

    LaunchedEffect(Unit) {
        db.collection("servicios")
            .whereEqualTo("estadoActivo", true)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listaServicios = snapshot.documents.map { doc ->
                        Servicio(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            precioSoles = doc.getDouble("precioSoles") ?: 0.0,
                            duracionMinutos = doc.getLong("duracionMinutos")?.toInt() ?: 0,
                            imagenUrl = doc.getString("imagenUrl") ?: "",
                            categoria = doc.getString("categoria") ?: "Corte", // 👈 RECUPERAMOS LA CATEGORÍA
                            estadoActivo = true
                        )
                    }
                }
            }

        db.collection("barberos")
            .whereEqualTo("estadoActivo", true)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listaBarberos = snapshot.documents.map { doc ->
                        Barbero(
                            id = doc.id,
                            nombres = doc.getString("nombres") ?: "",
                            especialidad = doc.getString("especialidad") ?: "",
                            fotoUrl = doc.getString("fotoUrl") ?: "",
                            estadoActivo = true
                        )
                    }
                }
            }
    }

    // --- FILTRADO DIRECTO Y LIMPIO (Clean Architecture) ---
    val serviciosFiltrados = if (categoriaSeleccionada == "Todos") {
        listaServicios
    } else {
        listaServicios.filter { it.categoria == categoriaSeleccionada }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Catálogo de Servicios",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Selecciona una categoría para explorar",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- RENDERIZADO DE CHIPS ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categorias) { categoria ->
                val esSeleccionado = categoria == categoriaSeleccionada
                Box(
                    modifier = Modifier
                        .background(
                            color = if (esSeleccionado) goldAccent else cardBackground,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { categoriaSeleccionada = categoria }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = categoria,
                        color = if (esSeleccionado) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- RENDERIZADO EN CUADRÍCULA DE 2 COLUMNAS ---
        if (serviciosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron servicios en esta categoría.", color = Color.Gray)
            }
        } else {
            val filasDeServicios = serviciosFiltrados.chunked(2)

            filasDeServicios.forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServicioGridCard(
                        servicio = fila[0],
                        cardBackground = cardBackground,
                        goldAccent = goldAccent,
                        modifier = Modifier.weight(1f)
                    )

                    if (fila.size > 1) {
                        ServicioGridCard(
                            servicio = fila[1],
                            cardBackground = cardBackground,
                            goldAccent = goldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "El Equipo Fade Culture",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(listaBarberos) { barbero ->
                BarberoClienteCard(barbero, cardBackground, goldAccent)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ServicioGridCard(
    servicio: Servicio,
    cardBackground: Color,
    goldAccent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(210.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, Color(0xFF2E2E2E))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (servicio.imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = servicio.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFF2A2415)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = goldAccent, modifier = Modifier.size(32.dp))
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = servicio.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${servicio.duracionMinutos} min",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "S/. ${servicio.precioSoles}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = goldAccent
                )
            }
        }
    }
}

@Composable
fun BarberoClienteCard(
    barbero: Barbero,
    cardBackground: Color,
    goldAccent: Color
) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, Color(0xFF2E2E2E))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (barbero.fotoUrl.isNotBlank()) {
                AsyncImage(
                    model = barbero.fotoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(70.dp).background(Color(0xFF2A2415), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = goldAccent, modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = barbero.nombres,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = barbero.especialidad,
                color = goldAccent,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}