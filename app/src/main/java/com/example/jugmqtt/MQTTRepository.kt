package com.example.jugmqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.StandardCharsets

object MqttRepository {

    private var mqttClient: Mqtt5AsyncClient? = null

    private val _connectionStatus = MutableStateFlow("Отключено")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    // Вызывается из MqttService при создании и остановке клиента
    fun setClient(client: Mqtt5AsyncClient?) {
        this.mqttClient = client
    }

    fun updateStatus(newStatus: String) {
        _connectionStatus.value = newStatus
    }

    fun addMessage(message: String) {
        _messages.update { currentList ->
            (listOf(message) + currentList).take(50)
        }
    }

    // Метод для отправки сообщений из любого места приложения
    fun publishMessage(
        topic: String,
        payload: String,
        qos: MqttQos = MqttQos.AT_LEAST_ONCE,
        retain: Boolean = false,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        val client = mqttClient
        if (client == null || !client.state.isConnected) {
            onResult?.invoke(false, "Нет соединения с сервером")
            return
        }

        client.publishWith()
            .topic(topic)
            .payload(payload.toByteArray(StandardCharsets.UTF_8))
            .qos(qos)
            .retain(retain)
            .send()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    onResult?.invoke(true, null)
                } else {
                    onResult?.invoke(false, throwable.message)
                }
            }
    }
}
