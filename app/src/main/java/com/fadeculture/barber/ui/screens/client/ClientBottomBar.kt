package com.fadeculture.barber.ui.screens.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

// Modelo de datos para cada botón
data class ClientBottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

// Lista de las 5 opciones del Cliente
val clientBottomNavItems = listOf(
    ClientBottomNavItem("Inicio", Icons.Default.Home, "client_home"),
    ClientBottomNavItem("Reservar", Icons.Default.AddCircle, "client_reservar"),
    ClientBottomNavItem("Mis Citas", Icons.Default.DateRange, "client_mis_citas"),
    ClientBottomNavItem("Catálogo", Icons.Default.ContentCut, "client_catalogo"),
    ClientBottomNavItem("Perfil", Icons.Default.Person, "client_perfil")
)

@Composable
fun ClientBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    NavigationBar(
        containerColor = cardBackground,
        contentColor = goldAccent
    ) {
        clientBottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = goldAccent,
                    indicatorColor = goldAccent,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}