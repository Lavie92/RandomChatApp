package com.lavie.randochat.localdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lavie.randochat.localdata.entity.MessageEntity
import com.lavie.randochat.utils.Constants

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    suspend fun getMessagesByRoom(roomId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT id FROM messages WHERE roomId = :roomId ORDER BY timestamp DESC LIMIT -1 OFFSET ${Constants.PAGE_SIZE_MESSAGES}")
    suspend fun getMessageIdsToDelete(roomId: String): List<String>

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    suspend fun deleteMessagesById(ids: List<String>)

    @Query("DELETE from messages WHERE roomId = :roomId")
    suspend fun deleteMessagesByRoom(roomId: String)
}