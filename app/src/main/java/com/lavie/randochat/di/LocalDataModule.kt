package com.lavie.randochat.di

import androidx.room.Room
import com.lavie.randochat.localdata.datasource.MessageCacheDataSource
import com.lavie.randochat.localdata.datasource.MessageCacheDataSourceImpl
import com.lavie.randochat.localdata.db.ChatDatabase
import com.lavie.randochat.utils.Constants
import org.koin.dsl.module

val localDataModule = module {

    single {
        Room.databaseBuilder(
            get(),
            ChatDatabase::class.java,
            Constants.CHAT_DB
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<ChatDatabase>().messageDao() }

    single<MessageCacheDataSource> {
        MessageCacheDataSourceImpl(get())
    }
}
