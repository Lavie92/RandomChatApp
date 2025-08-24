package com.lavie.randochat.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lavie.randochat.MainActivity
import com.lavie.randochat.R
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageType
import timber.log.Timber
class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("onNewToken: $token")
        val prefs = SharedPreferencesService(this)
        prefs.putString(Constants.FCM_TOKENS, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("onMessageReceived data: ${remoteMessage.data}")

        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        if (!CommonUtils.isAppInForeground(this)) {
            val messageType = data[Constants.TYPE]
            val decryptedContent = data[Constants.CONTENT]

            val title = getString(R.string.app_name)
            val body = when (messageType) {
                MessageType.TEXT.name -> decryptedContent ?: getString(R.string.new_message_text)
                MessageType.IMAGE.name -> getString(R.string.new_message_image)
                MessageType.VOICE.name -> getString(R.string.new_message_voice)
                else -> decryptedContent ?: getString(R.string.new_message_text)
            }

            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setSmallIcon(R.drawable.vector_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}
