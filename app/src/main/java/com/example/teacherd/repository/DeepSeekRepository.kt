package com.example.teacherd.repository

import com.example.teacherd.model.ChatCompletionChunk
import com.example.teacherd.model.Message
import com.example.teacherd.model.NoStreamChunk
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class DeepSeekRepository {

    val httpClient = OkHttpClient().newBuilder().build()
    val mediaType = "application/json".toMediaType()

    val gson = Gson()

    data class MessageRequest(
        val messages: List<Message>,
        val model: String,
        val stream: Boolean = true
    )

    fun getResponseFlow( key: String, messages: List<ChatCompletionChunk>, model: String): Flow<ChatCompletionChunk> = callbackFlow {
        val formattedMessages = messages.map {
            val delta = it.choices.firstOrNull()?.delta
            Message(delta?.content ?: "", delta?.role ?: "")
        }

        val json = gson.toJson(
            MessageRequest(
                messages = formattedMessages,
                model = model
            )
        )

        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.deepseek.com/chat/completions")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $key")
            .build()

        val call = httpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("Request failed with code ${response.code}")
                        close()
                        return
                    }

                    response.body?.source()?.let { source ->
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line()?.removePrefix("data: ")
                            if (!line.isNullOrEmpty() && line != "[DONE]") {
                                val chunk = gson.fromJson(line, ChatCompletionChunk::class.java)
                                trySend(chunk).isSuccess
                            }
                        }
                    } ?: close(throw Exception("Response body is null"))
                    close()
                }
            }
        })

        awaitClose { call.cancel() }
    }

    fun generateChatTitle(key: String, messages: List<ChatCompletionChunk>): Flow<String> = callbackFlow {
        val formattedMessages = messages.map {
            val delta = it.choices.firstOrNull()?.delta
            Message(delta?.content ?: "", delta?.role ?: "")
        }

        val requestMessages = formattedMessages + Message(
            content = "根据以上对话生成一句15个字以内的对话标题",
            role = "system"
        )

        val json = gson.toJson(
            MessageRequest(
                messages = requestMessages,
                model = "deepseek-chat",
                stream = false
            )
        )

        println(json)

        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.deepseek.com/chat/completions")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $key")
            .build()

        val call = httpClient.newCall(request)
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    close(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            println("Request failed with code ${response.code}")
                            close()
                        }
                        else {
                            response.body?.string()?.let {
                                val json = it.removePrefix("data: ")
                                val chunk = gson.fromJson(json, NoStreamChunk::class.java)
                                trySend(chunk.choices.firstOrNull()?.message?.content ?: "").isSuccess
                                println("title: ${chunk.choices.firstOrNull()?.message?.content}")
                            } ?: close(throw Exception("Response body is null"))
                            close()
                        }
                    }
                }
            }
        )
        awaitClose { call.cancel() }
    }
}