package com.lavie.randochat.di

import org.koin.dsl.module
import okhttp3.OkHttpClient

val networkModule = module {
    single { OkHttpClient.Builder().build() }
}
