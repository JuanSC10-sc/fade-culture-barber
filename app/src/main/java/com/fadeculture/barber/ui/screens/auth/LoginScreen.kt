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
import androidx.lint.kotlin.metadata.Visibility
import androidx.navigation.NavHostController
import com.fadeculture.barber.R
import com.fadeculture.barber.ui.navigation.Screen

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Variables de estado
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Ingreso
        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> {
                        Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        Toast.makeText(context, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
                    }
                    password.length < 6 -> {
                        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()

                                    // Redirigimos
                                    navController.navigate(Screen.ClientHome.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
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
                contentColor = Color.Black // Letra negra sobre fondo dorado destaca mucho
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
                    // Navegación segura usando nuestro Screen.kt
                    navController.navigate(Screen.Register.route)
                }
            )
        }
    }
}