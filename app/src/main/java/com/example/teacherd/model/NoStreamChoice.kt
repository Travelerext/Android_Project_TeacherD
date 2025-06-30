package com.example.teacherd.model

data class NoStreamChoice(
    val finish_reason: String = "",
    val index: Int = 0,
    val message: Message
)
