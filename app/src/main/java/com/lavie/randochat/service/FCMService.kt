package com.lavie.randochat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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

    companion object {
        private const val CHANNEL_ID = "chat_messages"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("onNewToken: $token")
        // Lưu tạm token vào SharedPreferences để ViewModel sử dụng khi user login/register
        val sharedPrefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("pending_fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("onMessageReceived data: ${remoteMessage.data}")

        // Xử lý "data message" - ưu tiên data để handle notification mọi trạng thái app
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        // Nếu server gửi notification message thì xử lý hiển thị luôn
        remoteMessage.notification?.let {
            showNotification(
                it.title ?: getString(R.string.app_name),
                it.body ?: "",
                remoteMessage.data["roomId"]
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"]
        val encryptedContent = data["content"]
        val roomId = data["roomId"]
        val senderId = data["senderId"]
        val key = CommonUtils.generateMessageKey(roomId ?: "", senderId ?: "")
        val content = if (encryptedContent != null && roomId != null && senderId != null) {
            CommonUtils.decryptMessage(encryptedContent, key)
        } else encryptedContent

        val title = senderId ?: getString(R.string.app_name)
        val body = when (messageType) {
            MessageType.TEXT.name -> content ?: "Bạn có tin nhắn mới"
            MessageType.IMAGE.name -> "Đã gửi một hình ảnh"
            MessageType.VOICE.name -> "Đã gửi một tin nhắn thoại"
            else -> content ?: "Bạn có tin nhắn mới"
        }

        showNotification(title, body, roomId)
    }

    private fun showNotification(title: String, body: String, roomId: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            roomId?.let { putExtra("roomId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat messages"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
