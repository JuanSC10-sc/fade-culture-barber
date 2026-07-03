package com.fadeculture.barber.ui.navigation

sealed class Screen(val route: String) {
    // Módulo de Autenticación
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Dashboards principales según el Rol (Rol-based UI)
    object ClientHome : Screen("client_home")
    object ClientMain : Screen("client_main")
    object Catalogo : Screen("client_catalogo")
    object BarberHome : Screen("barber_home")
    object AdminHome : Screen("admin_home")
}