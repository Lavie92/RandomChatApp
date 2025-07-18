package com.lavie.randochat

import android.app.Application
import com.google.firebase.FirebaseApp
import com.lavie.randochat.di.firebaseModule
import com.lavie.randochat.di.repositoryModule
import com.lavie.randochat.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.lavie.randochat.di.prefsModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(firebaseModule, repositoryModule, viewModelModule, prefsModule))
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.channel_id)
            val channelName = getString(R.string.fcm_channel_name)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.fcm_channel_desc)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}