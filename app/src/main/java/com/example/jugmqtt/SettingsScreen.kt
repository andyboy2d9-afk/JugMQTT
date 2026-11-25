package com.example.jugmqtt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Настройки", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text(text = "Вернуться назад")
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onNavigateBack = {})
}