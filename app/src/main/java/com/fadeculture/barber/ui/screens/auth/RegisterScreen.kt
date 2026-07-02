package com.fadeculture.barber.ui.screens.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fadeculture.barber.R
import com.fadeculture.barber.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current

    // variables
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    // Visibilidad de contraseñas
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // colores
    val darkBackground = Color(0xFF121212)
    val goldAccent = Color(0xFFD4AF37)

    // Scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crear Cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Campo: Nombres
        CustomTextField(
            value = nombres,
            onValueChange = { nombres = it },
            label = "Nombres",
            icon = Icons.Default.Person,
            goldAccent = goldAccent
        )

        // Campo: Apellidos
        CustomTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = "Apellidos",
            icon = Icons.Default.Person,
            goldAccent = goldAccent
        )

        // Campo: Teléfono
        CustomTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = "Teléfono",
            icon = Icons.Default.Phone,
            goldAccent = goldAccent,
            keyboardType = KeyboardType.Phone
        )

        // Campo: Correo
        CustomTextField(
            value = correo,
            onValueChange = { correo = it },
            label = "Correo Electrónico",
            icon = Icons.Default.Email,
            goldAccent = goldAccent,
            keyboardType = KeyboardType.Email
        )

        // Campo: Contraseña
        CustomPasswordField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = "Contraseña",
            passwordVisible = passwordVisible,
            onVisibilityChange = { passwordVisible = it },
            goldAccent = goldAccent
        )

        // Campo: Confirmar Contraseña
        CustomPasswordField(
            value = confirmarContrasena,
            onValueChange = { confirmarContrasena = it },
            label = "Confirmar Contraseña",
            passwordVisible = confirmPasswordVisible,
            onVisibilityChange = { confirmPasswordVisible = it },
            goldAccent = goldAccent
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Registro
        Button(
            onClick = {
                when {
                    nombres.isBlank() || apellidos.isBlank() || telefono.isBlank() || correo.isBlank() || contrasena.isBlank() -> {
                        Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                        Toast.makeText(context, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
                    }
                    telefono.length < 9 -> {
                        Toast.makeText(context, "Ingrese un número de teléfono válido", Toast.LENGTH_SHORT).show()
                    }
                    contrasena.length < 6 -> {
                        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    }
                    contrasena != confirmarContrasena -> {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val auth = FirebaseAuth.getInstance()
                        val db = FirebaseFirestore.getInstance()

                        // Creamos cuenta en authentication
                        auth.createUserWithEmailAndPassword(correo, contrasena)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid

                                    // datos adicionales
                                    val userMap = hashMapOf(
                                        "nombres" to nombres,
                                        "apellidos" to apellidos,
                                        "telefono" to telefono,
                                        "correo" to correo,
                                        "rol" to "cliente"
                                    )

                                    // Guardamos en la bd
                                    if (userId != null) {
                                        db.collection("usuarios").document(userId)
                                            .set(userMap)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show()
                                                // Redirigimos al Home
                                                navController.navigate(Screen.Login.route) {
                                                    popUpTo(Screen.Register.route) { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Error guardando datos: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = goldAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Volver al Login
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Text(text = "¿Ya tienes una cuenta? ", color = Color.LightGray)
            Text(
                text = "Inicia Sesión",
                color = goldAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}



@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    goldAccent: Color,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = goldAccent) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = goldAccent, unfocusedBorderColor = Color.Gray,
            focusedLabelColor = goldAccent, unfocusedLabelColor = Color.LightGray,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            cursorColor = goldAccent
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun CustomPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    goldAccent: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = goldAccent) },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { onVisibilityChange(!passwordVisible) }) {
                Icon(image, contentDescription = null, tint = Color.Gray)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = goldAccent, unfocusedBorderColor = Color.Gray,
            focusedLabelColor = goldAccent, unfocusedLabelColor = Color.LightGray,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            cursorColor = goldAccent
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}