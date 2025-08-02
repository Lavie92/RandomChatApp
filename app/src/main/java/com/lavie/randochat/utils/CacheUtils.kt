package com.lavie.randochat.utils

import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.Message
import org.json.JSONArray
import org.json.JSONObject

object CacheUtils {
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
