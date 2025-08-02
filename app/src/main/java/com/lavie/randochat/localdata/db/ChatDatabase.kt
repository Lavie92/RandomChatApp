package com.lavie.randochat.localdata.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lavie.randochat.localdata.dao.MessageDao
import com.lavie.randochat.localdata.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}