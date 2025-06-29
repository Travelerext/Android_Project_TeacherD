package com.example.teacherd.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant

@Entity
@TypeConverters(InstantConverter::class, ChatChunkConverter::class)
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val createAt: Instant = Instant.now(),
    val chunks: List<ChatCompletionChunk> = emptyList()
)
