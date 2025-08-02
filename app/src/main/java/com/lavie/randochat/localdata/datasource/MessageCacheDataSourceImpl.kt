package com.lavie.randochat.localdata.datasource

import com.lavie.randochat.localdata.dao.MessageDao
import com.lavie.randochat.localdata.mapper.toDomain
import com.lavie.randochat.localdata.mapper.toEntity
import com.lavie.randochat.model.Message

class MessageCacheDataSourceImpl (
    private val messageDao: MessageDao
) : MessageCacheDataSource {
    override suspend fun getCachedMessages(roomId: String): List<Message> {
        return messageDao.getMessagesByRoom(roomId).map { it.toDomain() }
    }

    override suspend fun cacheMessage(roomId: String, messages: List<Message>) {
        messageDao.insertMessages(messages.map { it.toEntity(roomId) })

        val excessIds = messageDao.getMessageIdsToDelete(roomId)
        if (excessIds.isNotEmpty()) {
            messageDao.deleteMessagesById(excessIds)
        }
    }

    override suspend fun clearCachedMessage(roomId: String) {
        messageDao.deleteMessagesByRoom(roomId)
    }
}