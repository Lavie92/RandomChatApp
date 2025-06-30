package com.lavie.randochat.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lavie.randochat.utils.Constants
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single {
        FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL)
    }
    single { get<FirebaseDatabase>().reference }
}

