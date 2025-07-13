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
            obj.put("id", msg.id)
            obj.put("senderId", msg.senderId)
            obj.put("content", msg.content)
            msg.contentResId?.let { obj.put("contentResId", it) }
            obj.put("timestamp", msg.timestamp)
            obj.put("type", msg.type.name)
            obj.put("status", msg.status.name)
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
                    id = obj.optString("id"),
                    senderId = obj.optString("senderId"),
                    content = obj.optString("content"),
                    contentResId = if (obj.has("contentResId")) obj.getInt("contentResId") else null,
                    timestamp = obj.optLong("timestamp"),
                    type = MessageType.valueOf(obj.optString("type", MessageType.TEXT.name)),
                    status = MessageStatus.valueOf(obj.optString("status", MessageStatus.SENT.name))
                )
            )
        }
        return list
    }

    fun chatRoomToJson(room: ChatRoom): String {
        val obj = JSONObject()
        obj.put("id", room.id)
        val participants = JSONArray()
        room.participantIds.forEach { participants.put(it) }
        obj.put("participantIds", participants)
        obj.put("createdAt", room.createdAt)
        obj.put("lastMessage", room.lastMessage)
        obj.put("lastUpdated", room.lastUpdated)
        obj.put("isActive", room.isActive)
        obj.put("chatType", room.chatType.name)
        return obj.toString()
    }

    fun jsonToChatRoom(json: String?): ChatRoom? {
        if (json.isNullOrEmpty()) return null
        val obj = JSONObject(json)
        val participants = mutableListOf<String>()
        val arr = obj.optJSONArray("participantIds")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                participants.add(arr.getString(i))
            }
        }
        return ChatRoom(
            id = obj.optString("id"),
            participantIds = participants,
            createdAt = obj.optLong("createdAt"),
            lastMessage = obj.optString("lastMessage"),
            lastUpdated = obj.optLong("lastUpdated"),
            isActive = obj.optBoolean("isActive"),
            chatType = ChatType.valueOf(obj.optString("chatType", ChatType.RANDOM.name))
        )
    }
}
