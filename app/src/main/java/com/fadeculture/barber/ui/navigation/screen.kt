package com.fadeculture.barber.ui.navigation

sealed class Screen(val route: String) {
    // Módulo de Autenticación
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Dashboards principales según el Rol (Rol-based UI)
    object ClientHome : Screen("client_home")
    object ClientMain : Screen("client_main")

    object Comprobante : Screen("client_comprobante/{citaId}") {
        fun createRoute(citaId: String) = "client_comprobante/$citaId"
    }
    object Catalogo : Screen("client_catalogo")

    // Rutas del Barbero
    object BarberHome : Screen("barber_home")
    object BarberMain : Screen("barber_main") //
    object AdminHome : Screen("admin_home")
}