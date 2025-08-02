package com.lavie.randochat.localdata.mapper

import com.lavie.randochat.localdata.entity.MessageEntity
import com.lavie.randochat.model.Message

fun Message.toEntity(roomId: String) = MessageEntity(
    id, senderId, content, contentResId, timestamp, type, status, roomId
)

fun MessageEntity.toDomain() = Message(
    id, senderId, content, contentResId, timestamp, type, status
)
