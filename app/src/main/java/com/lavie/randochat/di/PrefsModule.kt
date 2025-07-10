package com.lavie.randochat.di

import com.lavie.randochat.service.PreferencesService
import com.lavie.randochat.service.SharedPreferencesService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val prefsModule = module {
    single<PreferencesService> { SharedPreferencesService(androidContext()) }
}