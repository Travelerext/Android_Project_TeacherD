package com.example.teacherd.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherd.SettingPreference
import com.example.teacherd.db.ChatDao
import com.example.teacherd.model.Chat
import com.example.teacherd.model.ChatCompletionChunk
import com.example.teacherd.model.Choice
import com.example.teacherd.model.Delta
import com.example.teacherd.repository.DeepSeekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val dao: ChatDao,
    private val settingPreference: SettingPreference,
    private val deepSeekRepository: DeepSeekRepository
): ViewModel() {

    private var job by mutableStateOf<Job?>(null)

    var isThinking by mutableStateOf(false)

    var tempChunks = mutableStateListOf<ChatCompletionChunk>()

    private val _state = MutableStateFlow(ChatState())

    val state = dao.getAll()
        .onStart { ChatState(isLoading = true) }
        .map { chats ->
            _state.value.copy(
                isLoading = false,
                chats = chats
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatState())

    private val _selectedChatId = MutableStateFlow<Int?>(null)

    val selectedChat = _selectedChatId
        .flatMapLatest { id ->
            id?.let { dao.getChatById(it) } ?: flowOf(Chat())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Chat())

    val apiKey = settingPreference.getApiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val selectedModel = settingPreference.getSelectedModel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek-chat")

    fun selectChat(id: Int?) {
        _selectedChatId.value = id
        cancelJob()
    }

    fun setApiKey(apiKey: String) {
        viewModelScope.launch {
            settingPreference.saveApiKey(apiKey)
        }
    }

    fun selectChatModel() {
        viewModelScope.launch {
            settingPreference.selectChatModel()
        }
    }

    fun selectReasonerModel() {
        viewModelScope.launch {
            settingPreference.selectReasonerModel()
        }
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            if (chat == selectedChat.value)
                _selectedChatId.value = null
            dao.deleteChat(chat)
        }
    }

    fun deleteAllChats() {
        viewModelScope.launch {
            _selectedChatId.value = null
            dao.deleteAll()
        }
    }

    fun cancelJob() {
        tempChunks.clear()
        job?.cancel()
    }

    fun getResponse(question: String) {
        if (isThinking)
            return
        job = viewModelScope.launch {
            isThinking = true
            val questionChunk = ChatCompletionChunk().copy(
                choices = listOf(Choice(delta = Delta(content = question, role = "user")))
            )
            tempChunks += questionChunk
            val newChatChunks = selectedChat.value.chunks + questionChunk
            var isFirstChunk = true
            var responseChunk = ChatCompletionChunk()
            var responseIndex = 0
            deepSeekRepository.getResponseFlow(
                key = apiKey.value,
                messages = newChatChunks,
                model = selectedModel.value
            ).collect {
                if (isFirstChunk) {
                    isFirstChunk = false
                    responseChunk = it
                    tempChunks += responseChunk
                    responseIndex = tempChunks.lastIndex
                } else {
                    val responseDelta = responseChunk.choices[0].delta.copy(
                        content = (responseChunk.choices[0].delta.content
                            ?: "") + (it.choices[0].delta.content ?: "")
                    )
                    responseChunk =
                        it.copy(choices = listOf(it.choices[0].copy(delta = responseDelta)))
                    responseIndex =
                        if (responseIndex <= tempChunks.lastIndex) responseIndex else tempChunks.lastIndex
                    tempChunks[responseIndex] = responseChunk
                }
            }
            if (responseChunk.choices[0].delta.role == "") {
                return@launch
            }
            if (selectedChat.value.id == 0) {
                val title = deepSeekRepository.generateChatTitle(
                    key = apiKey.value,
                    messages = newChatChunks + responseChunk
                )
                dao.upsertChat(
                    Chat(
                        chunks = newChatChunks + responseChunk,
                        title = title.first()
                    )
                )
            } else {
                dao.upsertChat(selectedChat.value.copy(chunks = selectedChat.value.chunks + questionChunk + responseChunk))
            }
            if (selectedChat.value.id == 0) {
                _selectedChatId.value = dao.getAll().first().firstOrNull()?.id
            }
            tempChunks.clear()
        }

        job?.invokeOnCompletion{
            isThinking = false
        }
    }
}