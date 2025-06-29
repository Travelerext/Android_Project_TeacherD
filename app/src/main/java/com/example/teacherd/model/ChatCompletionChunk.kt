package com.example.teacherd.model

data class ChatCompletionChunk(
    val id: String = "",
    val choices: List<Choice> = emptyList(),
    val created: Int = 0,
    val model: String = "",
    val system_fingerprint: String = "",
    val `object`: String = ""
)
