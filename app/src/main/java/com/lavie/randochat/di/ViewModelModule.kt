package com.lavie.randochat.di

import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.viewmodel.ChatViewModel
import com.lavie.randochat.viewmodel.EmojiViewModel
import com.lavie.randochat.viewmodel.MatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { MatchViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
    viewModel { EmojiViewModel() }
}
