package com.example.teacherd.model

data class Choice(
    val delta: Delta,
    val finish_reason: String? = null,
    val index: Int = 0
)
