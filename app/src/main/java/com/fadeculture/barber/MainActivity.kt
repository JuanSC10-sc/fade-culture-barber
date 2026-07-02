package com.fadeculture.barber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.fadeculture.barber.ui.navigation.SetupNavGraph
import com.fadeculture.barber.ui.screens.barber.BarberAgendaScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Creamos el controlador de navegación necesario
            val navController = rememberNavController()

            // 2. Llamamos directamente a la pantalla de la agenda
            BarberAgendaScreen(navController = navController)

          //  val navController = rememberNavController()

            // grafo de navegación
            //SetupNavGraph(navController = navController)
        }
    }
}