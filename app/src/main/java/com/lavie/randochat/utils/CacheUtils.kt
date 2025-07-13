package com.lavie.randochat.utils

import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.Message
import org.json.JSONArray
import org.json.JSONObject

object CacheUtils {
    fun messagesToJson(messages: List<Message>): String {
        val arr = JSONArray()
        messages.forEach { msg ->
            val obj = JSONObject()
            obj.put(Constants.ID, msg.id)
            obj.put(Constants.SENDER_ID, msg.senderId)
            obj.put(Constants.CONTENT, msg.content)
            obj.put(Constants.TIMESTAMP, msg.timestamp)
            obj.put(Constants.TYPE, msg.type.name)
            obj.put(Constants.STATUS, msg.status.name)
            arr.put(obj)
        }

        return arr.toString()
    }

    fun jsonToMessages(json: String?): List<Message> {
        if (json.isNullOrEmpty()) return emptyList()
        val arr = JSONArray(json)
        val list = mutableListOf<Message>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Message(
                    id = obj.optString(Constants.ID),
                    senderId = obj.optString(Constants.SENDER_ID),
                    content = obj.optString(Constants.CONTENT),
                    timestamp = obj.optLong(Constants.TIMESTAMP),
                    type = MessageType.valueOf(obj.optString(Constants.TYPE, MessageType.TEXT.name)),
                    status = MessageStatus.valueOf(obj.optString(Constants.STATUS, MessageStatus.SENT.name))
                )
            )
        }
        return list
    }

    fun chatRoomToJson(room: ChatRoom): String {
        val obj = JSONObject()
        obj.put(Constants.ID, room.id)
        val participants = JSONArray()
        room.participantIds.forEach { participants.put(it) }
        obj.put(Constants.PARTICIPANTS_ID, participants)
        obj.put(Constants.IS_ACTIVE, room.isActive)
        obj.put(Constants.CHAT_TYPE, room.chatType.name)
        return obj.toString()
    }

    fun jsonToChatRoom(json: String?): ChatRoom? {
        if (json.isNullOrEmpty()) return null
        val obj = JSONObject(json)
        val participants = mutableListOf<String>()
        val arr = obj.optJSONArray(Constants.PARTICIPANTS_ID)
        if (arr != null) {
            for (i in 0 until arr.length()) {
                participants.add(arr.getString(i))
            }
        }
        return ChatRoom(
            id = obj.optString(Constants.ID),
            participantIds = participants,
            isActive = obj.optBoolean(Constants.IS_ACTIVE),
            chatType = ChatType.valueOf(obj.optString(Constants.CHAT_TYPE, ChatType.RANDOM.name))
        )
    }
}
