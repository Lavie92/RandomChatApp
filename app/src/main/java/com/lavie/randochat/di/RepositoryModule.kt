package com.lavie.randochat.di

import com.lavie.randochat.repository.UserRepository
import com.lavie.randochat.repository.UserRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
}
