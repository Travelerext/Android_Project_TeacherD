package com.example.teacherd.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.teacherd.model.Chat

@Database(entities = [Chat::class], version = 1)
abstract class ChatDataBase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}