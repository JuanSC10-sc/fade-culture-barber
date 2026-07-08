package com.fadeculture.barber.ui.screens.barber

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

// efinimos las rutas de la barra del barbero
sealed class BarberNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BarberNavItem("barber_home", "Inicio", Icons.Default.Home)
    object Agenda : BarberNavItem("barber_agenda", "Agenda", Icons.Default.EventNote)
    object Historial : BarberNavItem("barber_historial", "Historial", Icons.Default.Assignment)
    object Perfil : BarberNavItem("barber_perfil", "Perfil", Icons.Default.Person)
}

// Componente Visual de la Barra Inferior
@Composable
fun BarberBottomBar(internalNavController: NavHostController) {
    val cardBackground = Color(0xFF1E1E1E)
    val goldAccent = Color(0xFFD4AF37)

    val navItems = listOf(
        BarberNavItem.Home,
        BarberNavItem.Agenda,
        BarberNavItem.Historial,
        BarberNavItem.Perfil
    )

    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = cardBackground,
        contentColor = Color.White
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        internalNavController.navigate(item.route) {
                            popUpTo(internalNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = goldAccent,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = goldAccent
                )
            )
        }
    }
}