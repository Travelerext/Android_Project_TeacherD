package com.example.teacherd.model

data class NoStreamChunk (
    val id: String = "",
    val choices: List<NoStreamChoice> = emptyList(),
    val created: Int = 0,
    val model: String = "",
    val system_fingerprint: String = "",
    val `object`: String = ""
)