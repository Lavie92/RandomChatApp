package com.lavie.randochat

import android.app.Application
import com.google.firebase.FirebaseApp
import com.lavie.randochat.di.firebaseModule
import com.lavie.randochat.di.repositoryModule
import com.lavie.randochat.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(firebaseModule, repositoryModule, viewModelModule))
        }
    }
}