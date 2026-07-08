package com.fadeculture.barber.ui.screens.barber

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarberPerfilScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scrollState = rememberScrollState()

    // Colores
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)
    val errorColor = Color(0xFFEF5350)

    // Estados de Datos
    var nombres by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf(currentUser?.email ?: "") }
    var cargando by remember { mutableStateOf(true) }

    // Estados para Diálogo de Contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var contrasenaActual by remember { mutableStateOf("") }
    var contrasenaNueva by remember { mutableStateOf("") }
    var procesandoContrasena by remember { mutableStateOf(false) }

    // Estados para visibilidad
    var actualVisible by remember { mutableStateOf(false) }
    var nuevaVisible by remember { mutableStateOf(false) }

    // Cargar Datos del Especialista desde Firestore
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            db.collection("usuarios").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        nombres = doc.getString("nombres") ?: ""
                        telefono = doc.getString("telefono") ?: ""
                    }
                    cargando = false
                }
                .addOnFailureListener {
                    cargando = false
                    Toast.makeText(context, "Error de red al cargar perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = goldAccent
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                // Cabecera
                Text("Perfil Profesional", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Actualiza tus datos de contacto y acceso", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                // Campo Correo (Deshabilitado por seguridad)
                OutlinedTextField(
                    value = correo,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Correo Electrónico (Solo Lectura)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.DarkGray,
                        disabledTextColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledLeadingIconColor = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Nombres
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = { Text("Nombres y Apellidos") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = goldAccent, focusedLabelColor = goldAccent,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedLeadingIconColor = goldAccent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Número de Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = goldAccent, focusedLabelColor = goldAccent,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedLeadingIconColor = goldAccent
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Botón: Guardar Cambios
                Button(
                    onClick = {
                        when {
                            nombres.isBlank() || telefono.isBlank() -> {
                                Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            !nombres.matches(Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) -> {
                                Toast.makeText(context, "Los nombres y apellidos solo deben contener letras", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            !telefono.matches(Regex("^\\d{9}$")) -> {
                                Toast.makeText(context, "El teléfono debe contener exactamente 9 dígitos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            !telefono.all { it.isDigit() } -> {

                                Toast.makeText(
                                    context,
                                    "El teléfono solo debe contener números",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                        }

                        if (currentUser != null) {
                            val datosActualizados = mapOf(
                                "nombres" to nombres,
                                "telefono" to telefono
                            )
                            // Actualizar colección "usuarios"
                            db.collection("usuarios")
                                .document(currentUser.uid)
                                .update(datosActualizados)
                                .addOnSuccessListener {

                                    // Sincronizar actualización con colección "barberos"
                                    db.collection("barberos")
                                        .document(currentUser.uid)
                                        .update(datosActualizados)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Información actualizada correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Se actualizó 'usuarios', pero falló 'barberos'", Toast.LENGTH_SHORT).show()
                                        }

                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Fallo al guardar cambios", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón: Cambiar Contraseña
                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar Contraseña", color = Color.White)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón: Cerrar Sesión
                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        // 100% Seguro: Redirige y limpia la pila para no poder volver
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, errorColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = errorColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = errorColor, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- DIÁLOGO DE SEGURIDAD PARA CONTRASEÑA ---
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!procesandoContrasena) {
                    contrasenaActual = ""
                    contrasenaNueva = ""
                    actualVisible = false
                    nuevaVisible = false
                    showPasswordDialog = false
                }
            },
            containerColor = cardBackground,
            title = { Text("Seguridad de Cuenta", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa tus credenciales actuales para verificar tu identidad.", color = Color.Gray, fontSize = 14.sp)

                    // Input Contraseña Actual
                    OutlinedTextField(
                        value = contrasenaActual,
                        onValueChange = { contrasenaActual = it },
                        label = { Text("Contraseña Actual") },
                        visualTransformation = if (actualVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (actualVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { actualVisible = !actualVisible }) {
                                Icon(image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = goldAccent, focusedLabelColor = goldAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    // Input Nueva Contraseña
                    OutlinedTextField(
                        value = contrasenaNueva,
                        onValueChange = { contrasenaNueva = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = if (nuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (nuevaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { nuevaVisible = !nuevaVisible }) {
                                Icon(image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = goldAccent, focusedLabelColor = goldAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !procesandoContrasena,
                    onClick = {
                        // Validaciones Lógicas
                        if (contrasenaActual.isBlank() || contrasenaNueva.isBlank()) {
                            Toast.makeText(context, "Llene todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (contrasenaNueva.length < 6) {
                            Toast.makeText(context, "Mínimo 6 caracteres requeridos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (contrasenaActual == contrasenaNueva) {
                            Toast.makeText(context, "La nueva clave debe ser distinta a la actual", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        procesandoContrasena = true
                        val user = auth.currentUser

                        if (user != null && user.email != null) {
                            val credential = EmailAuthProvider.getCredential(user.email!!, contrasenaActual)

                            // Reautenticación en Firebase
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(contrasenaNueva)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Contraseña modificada con éxito", Toast.LENGTH_SHORT).show()
                                            contrasenaActual = ""
                                            contrasenaNueva = ""
                                            actualVisible = false
                                            nuevaVisible = false
                                            procesandoContrasena = false
                                            showPasswordDialog = false
                                        }
                                        .addOnFailureListener { e ->
                                            procesandoContrasena = false
                                            Toast.makeText(context, "Error en Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    procesandoContrasena = false
                                    Toast.makeText(context, "La contraseña actual no es válida", Toast.LENGTH_LONG).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black)
                ) {
                    if (procesandoContrasena) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    } else {
                        Text("Actualizar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!procesandoContrasena) {
                    OutlinedButton(
                        onClick = {
                            contrasenaActual = ""
                            contrasenaNueva = ""
                            actualVisible = false
                            nuevaVisible = false
                            showPasswordDialog = false
                        },
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Cancelar", color = Color.White)
                    }
                }
            }
        )
    }
}