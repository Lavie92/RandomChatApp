package com.lavie.randochat.di

import com.google.firebase.storage.FirebaseStorage
import com.lavie.randochat.repository.MatchRepository
import com.lavie.randochat.repository.MatchRepositoryImpl
import com.lavie.randochat.repository.ChatRepository
import com.lavie.randochat.repository.ChatRepositoryImpl
import com.lavie.randochat.repository.UserRepository
import com.lavie.randochat.repository.UserRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { FirebaseStorage.getInstance() }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<MatchRepository> { MatchRepositoryImpl(get()) }
    single<ChatRepository> { ChatRepositoryImpl(get()) }
}
