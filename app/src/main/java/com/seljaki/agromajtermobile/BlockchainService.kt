package com.seljaki.agromajtermobile

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.seljaki.lib.BlockchainClient
import java.time.Instant
import java.util.Date


class BlockchainService: Service() {
    lateinit var blockchainClient: BlockchainClient
    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()

        blockchainClient = BlockchainClient("android-notifications")
        blockchainClient.onConnect = {
            Log.d("blockchain_notifications", "connected to server")
        }
        blockchainClient.onNewBlockReceived = { block ->
            Log.d("blockchain_notifications", "new block recived $block")
            sendNotification(
                "Hash: ${block.hash}\n" +
                "Time mined: ${Date(block.timestamp * 1000)}\n" +
                "Tempature: ${block.data.temperature}\n" +
                "Long: ${block.data.longitude}, Lat: ${block.data.latitude}\n" +
                "Prediction: ${block.data.prediction.getPredicted()}"
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mqtt_channel",
                "MQTT Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isServiceStarted) {
            isServiceStarted = true

            startForegroundServiceWithNotification()
        }

        return START_STICKY
    }
    private fun startForegroundServiceWithNotification() {
        val notification = NotificationCompat.Builder(this, "blockchain_channel")
            .setContentTitle("Blockchain Service")
            .setContentText("Listening for blockchain updates...")
            .setSmallIcon(R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Create the notification channel if on API level 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mqtt_channel",
                "Blockchain Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Start the service as a foreground service
        startForeground(1, notification)
    }

    private fun sendNotification(message: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification = NotificationCompat.Builder(this, "mqtt_channel")
            .setContentTitle("New block added to the blockchain")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.stat_notify_chat)
            .build()
        manager.notify(1, notification)
    }

    override fun onDestroy() {
        blockchainClient.disconnect()
        Log.d("blockchain_notifications", "On destroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}