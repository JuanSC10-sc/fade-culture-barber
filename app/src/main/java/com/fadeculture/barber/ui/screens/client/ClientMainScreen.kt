package com.fadeculture.barber.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun ClientMainScreen(navController: NavHostController) {
    // Controlamos la pestaña actual seleccionada
    var currentRoute by remember { mutableStateOf("client_home") }
    val darkBackground = Color(0xFF121212)

    Scaffold(
        bottomBar = {
            ClientBottomBar(
                currentRoute = currentRoute,
                onNavigate = { nuevaRuta -> currentRoute = nuevaRuta }
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        // El Box ocupa el espacio restante encima de la barra
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (currentRoute) {
                "client_home" -> ClientHomeScreen(
                    navController = navController,
                    onNavigateTab = { ruta -> currentRoute = ruta } // Permite saltar a otra pestaña desde botones internos
                )
                "client_reservar" -> ClientReservarScreen(navController)
                "client_mis_citas" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Pantalla Mis Citas (Próximamente)", color = Color.White) }
                "client_catalogo" -> CatalogoScreen() // Tu pantalla de catálogo actual
                "client_perfil" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Pantalla Perfil (Próximamente)", color = Color.White) }
            }
        }
    }
}