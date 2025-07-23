package com.lavie.randochat.di

import com.lavie.randochat.repository.ImageFileRepository
import com.lavie.randochat.repository.ImageFileRepositoryImpl
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.MatchViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { MatchViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
 	single<ImageFileRepository> { ImageFileRepositoryImpl() }
}
