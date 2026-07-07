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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Instancias de Firebase
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Variables de estado
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var ventanaReset by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }


    // Paleta de colores de la Barbería
    val darkBackground = Color(0xFF121212)
    val goldAccent = Color(0xFFD4AF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Iniciar Sesión",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bienvenido de vuelta a Fade Culture",
            fontSize = 14.sp,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Correo Electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = goldAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = goldAccent) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = null, tint = Color.Gray)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = goldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    resetEmail = email
                    ventanaReset = true
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón de Ingreso
        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> {
                        Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        Toast.makeText(context, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    else -> {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid ?: ""

                                    // LÓGICA DE ROLES: Consultamos la colección "usuarios"
                                    db.collection("usuarios").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            Toast.makeText(context, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()

                                            // Extraemos el rol (por defecto será cliente si algo falla)
                                            val rol = document.getString("rol") ?: "cliente"

                                            // Redirigimos según el rol
                                            when (rol) {
                                                "admin" -> {
                                                    navController.navigate(Screen.AdminHome.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                                "barbero" -> {
                                                    // Redirigimos al Main del Barbero
                                                    navController.navigate(Screen.BarberMain.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                                else -> {
                                                    // Redirigimos al Main del Cliente
                                                    navController.navigate(Screen.ClientMain.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al recuperar datos del usuario.", Toast.LENGTH_SHORT).show()
                                        }

                                } else {
                                    val exceptionMessage = task.exception?.message ?: ""

                                    val mensajeError = when {
                                        exceptionMessage.contains("password") -> "La contraseña es incorrecta."
                                        exceptionMessage.contains("user") -> "El correo electrónico no está registrado."
                                        else -> "Correo o contraseña incorrectos. Intente de nuevo."
                                    }

                                    Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = goldAccent,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Redirigir al Registro
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "¿Eres nuevo? ", color = Color.LightGray)
            Text(
                text = "Regístrate aquí",
                color = goldAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
    }
    if (ventanaReset) {

        AlertDialog(
            onDismissRequest = {
                ventanaReset = false
            },

            title = {
                Text("Recuperar contraseña")
            },

            text = {

                Column {

                    Text(
                        text = "Ingrese el correo con el que registró su cuenta. Se enviará un enlace para restablecer su contraseña.",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                        },
                        label = {
                            Text("Correo electrónico")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        when {

                            resetEmail.isBlank() -> {

                                Toast.makeText(
                                    context,
                                    "Ingrese su correo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches() -> {

                                Toast.makeText(
                                    context,
                                    "Ingrese un correo válido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {

                                auth.sendPasswordResetEmail(resetEmail)
                                    .addOnSuccessListener {

                                        Toast.makeText(
                                            context,
                                            "Se envió un enlace de recuperación a su correo.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        ventanaReset = false
                                    }

                                    .addOnFailureListener {

                                        Toast.makeText(
                                            context,
                                            "No fue posible enviar el correo de recuperación.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                    }
                ) {

                    Text("Enviar")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        ventanaReset = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}