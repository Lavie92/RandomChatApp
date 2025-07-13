package com.lavie.randochat.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.lavie.randochat.MainActivity
import com.lavie.randochat.R
import com.lavie.randochat.utils.Constants

class ChatHeadService : Service() {
    private lateinit var windowManager: WindowManager
    private var chatHeadView: View? = null
    private var roomId: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        roomId = intent?.getStringExtra(Constants.ROOM_ID)
        if (chatHeadView == null) {
            showChatHead()
        }
        return START_STICKY
    }

    private fun showChatHead() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        chatHeadView = inflater.inflate(R.layout.layout_chat_head, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.x = 0
        params.y = 100
        windowManager.addView(chatHeadView, params)

        val icon = chatHeadView!!.findViewById<ImageView>(R.id.chat_head_icon)
        icon.setOnTouchListener(object : View.OnTouchListener {
            private var lastX = 0
            private var lastY = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX.toInt() - lastX
                        val dy = event.rawY.toInt() - lastY
                        params.x += dx
                        params.y += dy
                        windowManager.updateViewLayout(chatHeadView, params)
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        v.performClick()
                        return true
                    }
                }
                return false
            }
        })

        icon.setOnClickListener {
            val chatIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                roomId?.let { putExtra(Constants.ROOM_ID, it) }
            }
            startActivity(chatIntent)
            stopSelf()
        }
    }

    override fun onDestroy() {
        chatHeadView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }
}
