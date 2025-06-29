package com.example.teacherd.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.teacherd.model.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat ORDER BY createAt DESC")
    fun getAll(): Flow<List<Chat>>

    @Query("SELECT * FROM chat WHERE id = :id")
    fun getChatById(id: Int): Flow<Chat>

    @Upsert
    suspend fun upsertChat(chat: Chat)

    @Delete
    suspend fun deleteChat(chat: Chat)

    @Query("DELETE FROM chat")
    suspend fun deleteAll()
}