package com.lavie.randochat.localdata.datasource

import com.lavie.randochat.model.Message

interface MessageCacheDataSource {
    suspend fun getCachedMessages(roomId: String): List<Message>

    suspend fun cacheMessage(roomId: String ,messages: List<Message>)

    suspend fun clearCachedMessage(roomId: String)
}