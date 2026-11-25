package com.example.jugmqtt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MqttService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        setContent {
            AppNavigation()
        }
    }
}

// 1. Настройка маршрутов (навигации)
@Composable
fun AppNavigation() {
    // Контроллер, который запоминает историю переходов
    val navController = rememberNavController()

    // Контейнер, внутри которого меняются экраны. startDestination — первый экран
    NavHost(navController = navController, startDestination = "home") {

        // Маршрут для главного экрана
        composable("home") {
            HomeScreen(
                onNavigateToDetails = { navController.navigate("settings") }
            )
        }

        // Маршрут для второго экрана
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}


