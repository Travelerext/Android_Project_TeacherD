package com.example.teacherd.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.teacherd.model.ChatCompletionChunk

@Composable
fun ChatItem(chat: ChatCompletionChunk) {
    if (chat.choices.isNotEmpty()) {
        val delta = chat.choices[0].delta
        Column(modifier = Modifier.fillMaxWidth()) {
            val align = if (delta.role == "user") Alignment.End else Alignment.Start
            val cardColor = if (delta.role == "user") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            val textColor = if (delta.role == "user") Color.White else Color.Black
            if (!delta.content.isNullOrEmpty()){
                Card(
                    modifier = Modifier.align(align),
                    colors = CardDefaults.cardColors().copy(containerColor = cardColor)
                ) {
                    Text(
                        text = delta.content,
                        color = textColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}