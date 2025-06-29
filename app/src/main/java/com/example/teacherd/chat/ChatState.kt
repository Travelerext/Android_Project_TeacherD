package com.example.teacherd.chat

import com.example.teacherd.model.Chat

data class ChatState(
    val isLoading: Boolean = true,
    val chats: List<Chat> = emptyList()
)
