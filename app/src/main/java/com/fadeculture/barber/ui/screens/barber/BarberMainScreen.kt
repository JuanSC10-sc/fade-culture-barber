package com.fadeculture.barber.ui.screens.barber

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun BarberMainScreen(rootNavController: NavHostController) {
    // Controlador interno para cambiar entre las 4 pestañas del barbero
    val internalNavController = rememberNavController()
    val darkBackground = Color(0xFF121212)

    Scaffold(
        bottomBar = {
            // Llamamos a nuestro componente modularizado
            BarberBottomBar(internalNavController = internalNavController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(paddingValues)
        ) {
            NavHost(
                navController = internalNavController,
                startDestination = BarberNavItem.Home.route
            ) {
                // Pestaña de Inicio (Home)
                composable(BarberNavItem.Home.route) {
                    BarberHomeScreen(
                        navController = rootNavController,
                        onNavigateTab = { route ->
                            internalNavController.navigate(route) {
                                popUpTo(internalNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // Pestaña de Agenda (Trabajo del día)
                composable(BarberNavItem.Agenda.route) {
                    BarberAgendaScreen(navController = rootNavController)
                }

                // Pestaña de Historial
                composable(BarberNavItem.Historial.route) {
                    BarberHistorialScreen(navController = rootNavController) //
                }

                // Pestaña de Perfil
                composable(BarberNavItem.Perfil.route) {
                    BarberPerfilScreen(navController = rootNavController) //
                }
            }
        }
    }
}

// Pantalla temporal (Placeholder)
@Composable
fun PlaceholderScreen(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "Pantalla de $titulo en construcción",
            color = Color.Gray
        )
    }
}