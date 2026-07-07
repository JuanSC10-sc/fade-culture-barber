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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.fadeculture.barber.data.model.Servicio
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServiciosScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState() // Para prevenir que el teclado tape los campos

    // Colores premium
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    // Estados de navegación interna
    var isFormOpen by remember { mutableStateOf(false) }
    var servicioSeleccionadoEdicion by remember { mutableStateOf<Servicio?>(null) }

    // Estados para los campos del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }

    // Estados para el Selector de Categoría (Dropdown)
    var categoria by remember { mutableStateOf("Corte") }
    var expandedCategoria by remember { mutableStateOf(false) }
    val opcionesCategoria = listOf("Corte", "Barba", "Tinte", "Ondulación", "Servicios Completos")

    // Lista de servicios de Firestore
    var listaServicios by remember { mutableStateOf<List<Servicio>>(emptyList()) }

    // Escuchar Firestore en tiempo real
    LaunchedEffect(Unit) {
        db.collection("servicios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar servicios", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val servicios = snapshot.documents.map { doc ->
                        Servicio(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            precioSoles = doc.getDouble("precioSoles") ?: 0.0,
                            duracionMinutos = doc.getLong("duracionMinutos")?.toInt() ?: 30,
                            imagenUrl = doc.getString("imagenUrl") ?: "",
                            categoria = doc.getString("categoria") ?: "Corte", // 👈 RECUPERAMOS LA CATEGORÍA
                            estadoActivo = doc.getBoolean("estadoActivo") ?: true
                        )
                    }
                    listaServicios = servicios
                }
            }
    }

    // Rellenar campos en caso de edición
    LaunchedEffect(servicioSeleccionadoEdicion) {
        if (servicioSeleccionadoEdicion != null) {
            titulo = servicioSeleccionadoEdicion!!.titulo
            descripcion = servicioSeleccionadoEdicion!!.descripcion
            precio = servicioSeleccionadoEdicion!!.precioSoles.toString()
            duracion = servicioSeleccionadoEdicion!!.duracionMinutos.toString()
            imagenUrl = servicioSeleccionadoEdicion!!.imagenUrl
            categoria = servicioSeleccionadoEdicion!!.categoria // 👈 SETEAMOS LA CATEGORÍA AL EDITAR
        } else {
            titulo = ""
            descripcion = ""
            precio = ""
            duracion = ""
            imagenUrl = ""
            categoria = "Corte"
        }
    }

    Scaffold(
        floatingActionButton = {
            if (!isFormOpen) {
                FloatingActionButton(
                    onClick = {
                        servicioSeleccionadoEdicion = null
                        isFormOpen = true
                    },
                    containerColor = goldAccent,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Servicio")
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
            if (isFormOpen) {
                // --- VISTA: FORMULARIO ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(scrollState)
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
                            text = if (servicioSeleccionadoEdicion == null) "Nuevo Servicio" else "Editar Servicio",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AdminServicioTextField(value = titulo, onValueChange = { titulo = it }, label = "Nombre del Servicio (Ej: Corte Degradado)", goldAccent = goldAccent)
                    Spacer(modifier = Modifier.height(14.dp))

                    // --- SELECTOR DE CATEGORÍA VISUAL ---
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = !expandedCategoria },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true, // El usuario no puede escribir, solo seleccionar
                            label = { Text("Categoría del Servicio") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = goldAccent, unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = goldAccent, unfocusedLabelColor = Color.LightGray,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                cursorColor = goldAccent
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false },
                            modifier = Modifier.background(cardBackground)
                        ) {
                            opcionesCategoria.forEach { seleccion ->
                                DropdownMenuItem(
                                    text = { Text(seleccion, color = Color.White) },
                                    onClick = {
                                        categoria = seleccion
                                        expandedCategoria = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    AdminServicioTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción o detalles del servicio", goldAccent = goldAccent)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdminServicioTextField(value = precio, onValueChange = { precio = it }, label = "Precio (S/.)", goldAccent = goldAccent, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AdminServicioTextField(value = duracion, onValueChange = { duracion = it }, label = "Minutos", goldAccent = goldAccent, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    AdminServicioTextField(value = imagenUrl, onValueChange = { imagenUrl = it }, label = "URL Enlace de la Foto Referencial", goldAccent = goldAccent)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val precioDouble = precio.toDoubleOrNull()
                            val duracionInt = duracion.toIntOrNull()

                            when {

                                titulo.isBlank() ||
                                        descripcion.isBlank() ||
                                        precio.isBlank() ||
                                        duracion.isBlank() -> {

                                    Toast.makeText(
                                        context,
                                        "Complete todos los campos obligatorios",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                !titulo.matches(Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) -> {

                                    Toast.makeText(
                                        context,
                                        "El título solo debe contener letras",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                !descripcion.matches(Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ,.]+$")) -> {

                                    Toast.makeText(
                                        context,
                                        "La descripción solo debe contener letras",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                !precio.matches(Regex("^\\d+(\\.\\d{1,2})?$")) -> {

                                    Toast.makeText(
                                        context,
                                        "Ingrese un precio válido (Ej: 25.00)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                !duracion.matches(Regex("^\\d+$")) -> {

                                    Toast.makeText(
                                        context,
                                        "La duración solo debe contener números",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                            }

                            val servicioMap = hashMapOf(
                                "titulo" to titulo,
                                "descripcion" to descripcion,
                                "precioSoles" to precioDouble,
                                "duracionMinutos" to duracionInt,
                                "imagenUrl" to imagenUrl,
                                "categoria" to categoria,
                                "estadoActivo" to if (servicioSeleccionadoEdicion != null) servicioSeleccionadoEdicion!!.estadoActivo else true
                            )

                            if (servicioSeleccionadoEdicion == null) {
                                db.collection("servicios").add(servicioMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Servicio creado exitosamente", Toast.LENGTH_SHORT).show()
                                        isFormOpen = false
                                    }
                            } else {
                                db.collection("servicios").document(servicioSeleccionadoEdicion!!.id).set(servicioMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Servicio actualizado", Toast.LENGTH_SHORT).show()
                                        isFormOpen = false
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Servicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // --- VISTA: LISTADO EN TIEMPO REAL ---
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp)
                ) {
                    Text(text = "Catálogo de Servicios", color = goldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Gestiona los precios y cortes disponibles", color = Color.LightGray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    if (listaServicios.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No hay servicios registrados en el catálogo.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(listaServicios) { servicio ->
                                ServicioCard(
                                    servicio = servicio,
                                    cardBackground = cardBackground,
                                    goldAccent = goldAccent,
                                    onEdit = {
                                        servicioSeleccionadoEdicion = servicio
                                        isFormOpen = true
                                    },
                                    onToggleStatus = {
                                        db.collection("servicios").document(servicio.id)
                                            .update("estadoActivo", !servicio.estadoActivo)
                                            .addOnSuccessListener {
                                                val msg = if (!servicio.estadoActivo) "Servicio habilitado" else "Servicio ocultado del catálogo"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
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

// --- Componente de tarjeta de cada servicio ---
@Composable
fun ServicioCard(
    servicio: Servicio,
    cardBackground: Color,
    goldAccent: Color,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val opacidad = if (servicio.estadoActivo) 1f else 0.5f

    Card(
        modifier = Modifier.fillMaxWidth().alpha(opacidad),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (servicio.imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = servicio.imagenUrl,
                    contentDescription = servicio.titulo,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).background(Color(0xFF2A2415), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = goldAccent, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = servicio.titulo, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                // Mostramos también la categoría debajo del título para control visual
                Text(text = "${servicio.categoria} • ${servicio.duracionMinutos} min • S/. ${servicio.precioSoles}", color = goldAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                if (!servicio.estadoActivo) {
                    Text(text = "OCULTO PARA CLIENTES", color = Color(0xFFEF5350), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.LightGray)
                }
                IconButton(onClick = onToggleStatus) {
                    Icon(
                        imageVector = if (servicio.estadoActivo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Cambiar estado",
                        tint = if (servicio.estadoActivo) goldAccent else Color.Gray
                    )
                }
            }
        }
    }
}

// --- Input estilizado reutilizado con KeyboardOptions configurable ---
@Composable
fun AdminServicioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    goldAccent: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
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