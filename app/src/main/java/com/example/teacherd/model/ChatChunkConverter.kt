package com.example.teacherd.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatChunkConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromJson(value: String): List<ChatCompletionChunk> {
        val type = object : TypeToken<List<ChatCompletionChunk>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun jsonToChatChunkList(value: List<ChatCompletionChunk>): String {
        return gson.toJson(value)
    }
}