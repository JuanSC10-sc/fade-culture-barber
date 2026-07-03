package com.fadeculture.barber.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fadeculture.barber.ui.screens.admin.AdminHomeScreen
import com.fadeculture.barber.ui.screens.admin.AdminMainScreen
import com.fadeculture.barber.ui.screens.auth.LoginScreen
import com.fadeculture.barber.ui.screens.auth.RegisterScreen
import com.fadeculture.barber.ui.screens.auth.SplashScreen
import com.fadeculture.barber.ui.screens.barber.BarberAgendaScreen
import com.fadeculture.barber.ui.screens.client.CatalogoScreen
import com.fadeculture.barber.ui.screens.client.ClientHomeScreen
import com.fadeculture.barber.ui.screens.client.ClientMainScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    // startDestination Splash Screen
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Pantalla de Splash
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // 2. Pantalla de Login
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // 3. Pantalla de Registro
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // 4. Dashboard del Cliente
        composable(route = Screen.ClientMain.route) {
            ClientMainScreen(navController = navController)
        }

        // Catalogo para el Cliente
        composable(Screen.Catalogo.route) {
            CatalogoScreen()
        }

        // 5. Dashboard del Barbero
        composable(route = Screen.BarberHome.route) {
            BarberAgendaScreen(navController = navController)
        }

        // 6. Dashboard del Administrador / Recepción
        composable(route = Screen.AdminHome.route) {
            AdminMainScreen(navController = navController)
        }
    }
}