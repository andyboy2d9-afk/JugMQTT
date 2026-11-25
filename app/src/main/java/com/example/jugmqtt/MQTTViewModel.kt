package com.example.jugmqtt

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MqttViewModel : ViewModel() {
    // Пробрасываем потоки напрямую из репозитория
    val connectionStatus: StateFlow<String> = MqttRepository.connectionStatus
    val messages: StateFlow<List<String>> = MqttRepository.messages
    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError.asStateFlow()

    fun sendMessage(topic: String, text: String) {
        if (text.isBlank()) return

        MqttRepository.publishMessage(topic = topic, payload = text) { success, error ->
            _sendError.value = if (success) null else "Ошибка отправки: $error"
        }
    }
}
