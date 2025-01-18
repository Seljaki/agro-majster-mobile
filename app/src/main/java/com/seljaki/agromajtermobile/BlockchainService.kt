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
    private val channelId = "blockchain_channel"

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

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isServiceStarted) {
            isServiceStarted = true

            startForegroundServiceWithNotification()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Blockchain Service",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Notifications for Blockchain Service"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Blockchain Service")
            .setContentText("Listening for blockchain updates...")
            .setSmallIcon(com.seljaki.agromajtermobile.R.drawable.tractor) // Use your app's icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun sendNotification(message: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("New block added to the blockchain")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(com.seljaki.agromajtermobile.R.drawable.tractor)
            .build()
        manager.notify(2, notification)
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