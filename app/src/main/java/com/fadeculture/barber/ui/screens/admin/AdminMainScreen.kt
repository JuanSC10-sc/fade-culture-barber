package com.fadeculture.barber.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun AdminMainScreen(navController: NavHostController) {
    // Controlamos qué pestaña está activa. Empezamos en "admin_home"
    var currentRoute by remember { mutableStateOf("admin_home") }

    Scaffold(
        bottomBar = {
            AdminBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { nuevaRuta ->
                    currentRoute = nuevaRuta
                }
            )
        }
    ) { paddingValues ->
        // Este Box respeta el espacio de la barra inferior para que el contenido no quede oculto
        Box(modifier = Modifier.padding(paddingValues)) {
            // Evaluamos la ruta seleccionada y mostramos la pantalla correspondiente
            when (currentRoute) {
                "admin_home" -> AdminHomeScreen(navController = navController, onNavigateTab = {
                    ruta -> currentRoute = ruta
                })
                "admin_hoy" -> AdminAgendaHoyScreen(navController = navController)
                "admin_personal" -> AdminPersonalScreen(navController = navController)
                "admin_servicios" -> AdminServiciosScreen(navController = navController)
                "admin_perfil" -> AdminPerfilScreen(navController = navController)
            }
        }
    }
}