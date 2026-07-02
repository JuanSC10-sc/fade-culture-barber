package com.fadeculture.barber.ui.screens.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Estructura de cada botón
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AdminBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Definimos las 4 opciones del menú
    val items = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, "admin_home"),
        BottomNavItem("Turnos Hoy", Icons.Default.Today, "admin_hoy"),
        BottomNavItem("Personal", Icons.Default.Group, "admin_personal"),
        BottomNavItem("Servicios", Icons.Default.ContentCut, "admin_servicios"),
        BottomNavItem("Perfil", Icons.Default.Person, "admin_perfil")
    )

    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    NavigationBar(
        containerColor = cardBackground,
        contentColor = Color.White
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) cardBackground else Color.Gray // Si está seleccionado, el ícono se vuelve oscuro porque el fondo será dorado
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) goldAccent else Color.Gray
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = goldAccent // La burbuja que resalta la opción seleccionada
                )
            )
        }
    }
}