package com.example.jugmqtt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import java.nio.charset.StandardCharsets

class MqttService : Service() {

    private val brokerHost = "192.168.100.2"
    private val brokerPort = 1883 // Обычно 8883 для TLS (SSL) или 1883 без шифрования
    private val username = "andy"
    private val password = "FreeArtistmqtt"

    private val topic = "test/android/compose_demo"

    private var client: Mqtt5AsyncClient? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Переводим сервис в режим Foreground (обязательно в первые 5 секунд)
        startForeground(1, buildServiceNotification("Подключение к серверу..."))
        startMqttClient()
    }

    private fun startMqttClient() {
        client = MqttClient.builder()
            .useMqttVersion5()
            .identifier("android-client-fixed-id")
            .serverHost(brokerHost)
            .serverPort(brokerPort)
            //.sslWithDefaultConfig()
            .simpleAuth()
            .username(username)
            .password(password.toByteArray())
            .applySimpleAuth()
            .automaticReconnectWithDefaultConfig()

            .addConnectedListener {
                val status = "Подключено"
                updateServiceNotification("MQTT: $status")
                MqttRepository.updateStatus(status) // Передаем статус в UI
            }
            .addDisconnectedListener { context ->
                val status = "Переподключение (${context.cause.message ?: "обрыв"})"
                updateServiceNotification("MQTT: $status")
                MqttRepository.updateStatus(status) // Передаем статус в UI
            }
            .buildAsync()

        // Передаем клиент в репозиторий для отправки сообщений из UI
        MqttRepository.setClient(client)

        // Глобальный слушатель сообщений (работает всегда и после реконнектов)
        client?.publishes(MqttGlobalPublishFilter.ALL) { publish ->
            val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)

            // 1. Сохраняем в историю для экрана
            MqttRepository.addMessage(message)

            playAlertSound()
            showAlertNotification(message)
        }

        client?.connectWith()
            ?.keepAlive(30)
            ?.cleanStart(false)
            ?.sessionExpiryInterval(3600)
            ?.send()
            ?.whenComplete { _, error ->
                if (error == null) {
                    client?.subscribeWith()
                        ?.topicFilter(topic)
                        ?.qos(MqttQos.AT_LEAST_ONCE)
                        ?.send()
                }
            }
    }

    // Воспроизведение звука напрямую из сервиса (работает при выключенном экране)
    private fun playAlertSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Всплывающее уведомление с текстом сообщения
    private fun showAlertNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "mqtt_alerts_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Новое сообщение")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun buildServiceNotification(statusText: String): Notification {
        return NotificationCompat.Builder(this, "mqtt_service_channel")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Фоновый мониторинг")
            .setContentText(statusText)
            .setOngoing(true) // Уведомление нельзя смахнуть
            .build()
    }

    private fun updateServiceNotification(statusText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, buildServiceNotification(statusText))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            // Тихий канал для постоянного статуса сервиса
            val serviceChannel = NotificationChannel(
                "mqtt_service_channel", "Статус MQTT", NotificationManager.IMPORTANCE_LOW
            )
            // Громкий канал для входящих событий
            val alertChannel = NotificationChannel(
                "mqtt_alerts_channel", "Входящие сигналы", NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY заставит Android перезапустить сервис, если системе не хватило памяти
        return START_STICKY
    }

    override fun onDestroy() {
        MqttRepository.setClient(null)
        MqttRepository.updateStatus("Отключено")
        client?.disconnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
