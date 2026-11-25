package com.example.jugmqtt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit

class MqttViewModel_old : ViewModel() {

    // 1. Укажите ваши данные от брокера
    private val brokerHost = "192.168.100.2"
    private val brokerPort = 1883 // Обычно 8883 для TLS (SSL) или 1883 без шифрования
    private val username = "andy"
    private val password = "FreeArtistmqtt"

    private val topic = "test/android/compose_demo"

    private val _receivedMessage = MutableStateFlow("Ожидание данных...")
    val receivedMessage: StateFlow<String> = _receivedMessage.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Подключение...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // 2. Настраиваем клиент с учетом SSL (если порт 8883)
    private val client: Mqtt5AsyncClient = MqttClient.builder()
        .useMqttVersion5()
        .identifier("AndroidClient_" + UUID.randomUUID().toString())
        .serverHost(brokerHost)
        .serverPort(brokerPort)
        //.sslWithDefaultConfig() // <-- Удалите эту строку, если ваш брокер работает без SSL (на порту 1883)
        .simpleAuth()
        .username(username)
        .password(password.toByteArray(StandardCharsets.UTF_8))
        .applySimpleAuth()
        .automaticReconnect()
        .initialDelay(1, TimeUnit.SECONDS)
        .maxDelay(30, TimeUnit.SECONDS)
        .applyAutomaticReconnect()

        // Слушатели для отслеживания статуса
        .addConnectedListener {
            // UI обновится автоматически
            _connectionStatus.value = "Подключено"
        }
        .addDisconnectedListener { context ->
            // UI обновится автоматически
            _connectionStatus.value = "Разрыв связи: ${context.cause.message}"
        }

        .buildAsync()

    init {
        connectAndSubscribe()
    }

    private fun connectAndSubscribe() {
        viewModelScope.launch(Dispatchers.IO) {
            // 3. Передаем логин и пароль при подключении
            client.connectWith()
                //.simpleAuth()
                //.username(username)
                //.password(password.toByteArray(StandardCharsets.UTF_8))
                //.applySimpleAuth()
                .keepAlive(60) // Пинг каждые 60 секунд
                .cleanStart(false) // Не очищать сессию при переподключении
                .sessionExpiryInterval(3600) // Хранить сессию на брокере 1 час при потере связи
                .send()
                .whenComplete { _, throwable ->
                    if (throwable != null) {
                        _connectionStatus.value = "Ошибка: ${throwable.localizedMessage}"
                    } else {
                        _connectionStatus.value = "Подключено к $brokerHost"
                        subscribeToTopic()
                    }
                }
        }
    }

    private fun subscribeToTopic() {
        client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                val payload = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                _receivedMessage.value = payload
            }
            .send()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            client.publishWith()
                .topic(topic)
                .payload(text.toByteArray(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.disconnect()
    }
}
