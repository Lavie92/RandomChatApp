package com.lavie.randochat.localdata.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType

@Entity(tableName = Constants.MESSAGES)
data class MessageEntity(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = Constants.SENDER_ID) val senderId: String = "",
    @ColumnInfo(name = Constants.CONTENT) val content: String = "",
    @ColumnInfo(name = Constants.CONTENT_RES_ID) val contentResId: Int? = null,
    @ColumnInfo(name = Constants.TIMESTAMP) val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = Constants.TYPE) val type: MessageType = MessageType.TEXT,
    @ColumnInfo(name = Constants.STATUS) val status: MessageStatus =  MessageStatus.SENDING,
    @ColumnInfo(name = Constants.ROOM_ID) val roomId: String = "",
)