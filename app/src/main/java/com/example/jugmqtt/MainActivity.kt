package com.example.jugmqtt

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
                onNavigateToDetails = { navController.navigate("details") }
            )
        }

        // Маршрут для второго экрана
        composable("details") {
            DetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// 2. Первый экран
@Composable
fun HomeScreen(onNavigateToDetails: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Главный экран", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToDetails) {
            Text(text = "Перейти ко второму экрану")
        }
    }
}

// 3. Второй экран
@Composable
fun DetailsScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Второй экран", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text(text = "Вернуться назад")
        }
    }
}

// 4. Превью для удобной верстки каждого экрана отдельно
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onNavigateToDetails = {})
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    DetailsScreen(onNavigateBack = {})
}
