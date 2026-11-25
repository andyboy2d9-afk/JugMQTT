package com.example.jugmqtt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
//fun HomeScreen(onNavigateToDetails: () -> Unit, mqttViewModel: MqttViewModel = viewModel()) {
fun HomeScreen(onNavigateToDetails: () -> Unit, mqttViewModel: MqttViewModel = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Главный экран", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        SettingsButton(onClick = onNavigateToDetails)



        // Подписываемся на потоки данных из ViewModel

        val status by mqttViewModel.connectionStatus.collectAsState()
        val messages by mqttViewModel.messages.collectAsState()
        var inputText by remember { mutableStateOf("") }
        val sendError by mqttViewModel.sendError.collectAsState()

        val isConnected = status == "Подключено"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Статус подключения
            Text(
                text = "Статус: $status",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 1. Первое текстовое поле (для отправки)
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Введите сообщение для отправки") },
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Кнопка отправки
            Button(
                onClick = {
                    mqttViewModel.sendMessage("test/android/compose_demo", inputText)
                    inputText = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && inputText.isNotBlank()
            ) {
                Text("Отправить брокеру")
            }

            // Вывод ошибки отправки, если она возникла
            sendError?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider()

            // 3. Второе текстовое поле (отображает пришедшие данные)
            Text(
                text = "История сообщений:",
                style = MaterialTheme.typography.titleSmall
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }


        }
    }
}

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon_settings),
            // imageVector = Icons.Default.Settings,
            contentDescription = "Настройки", // Важно для доступности (Accessibility)
            // tint = Color.Unspecified // Или укажите нужный цвет, например MaterialTheme.colorScheme.primary

        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onNavigateToDetails = {})
}