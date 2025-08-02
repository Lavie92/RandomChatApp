package com.lavie.randochat.localdata.datasource

import com.lavie.randochat.localdata.dao.MessageDao
import com.lavie.randochat.localdata.mapper.toDomain
import com.lavie.randochat.localdata.mapper.toEntity
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.Constants

class MessageCacheDataSourceImpl (
    private val messageDao: MessageDao
) : MessageCacheDataSource {
    override suspend fun getCachedMessages(roomId: String): List<Message> {
        return messageDao.getMessagesByRoom(roomId).map { it.toDomain() }
    }

    override suspend fun cacheMessages(roomId: String, messages: List<Message>) {
        messageDao.insertMessages(messages.map { it.toEntity(roomId) })

        val allMessages = messageDao.getMessagesByRoom(roomId)
        val overLimit = allMessages.size - Constants.PAGE_SIZE_MESSAGES

        if (overLimit > 0) {
            val toDelete = allMessages
                .sortedBy { it.timestamp }
                .take(overLimit)
                .map { it.id }
            messageDao.deleteMessagesById(toDelete)
        }
    }

    override suspend fun clearCachedMessage(roomId: String) {
        messageDao.deleteMessagesByRoom(roomId)
    }
}