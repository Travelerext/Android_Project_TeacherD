package com.example.teacherd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.teacherd.chat.ChatRoot
import com.example.teacherd.chat.ChatViewModel
import com.example.teacherd.db.ChatDataBase
import com.example.teacherd.repository.DeepSeekRepository
import com.example.teacherd.setting.SettingViewModel
import com.example.teacherd.ui.theme.TeacherDTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            ChatDataBase::class.java,
            "chat-database"
        ).build()
    }

    private val settingPreference by lazy { SettingPreference(applicationContext) }

    private val deepSeekRepository by lazy { DeepSeekRepository() }

    private val chatViewModel by viewModels<ChatViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(db.chatDao(), settingPreference, deepSeekRepository) as T
                }
            }
        }
    )

    private val settingViewModel by viewModels<SettingViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingViewModel(settingPreference) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeacherDTheme {
                ChatRoot(chatViewModel, settingViewModel)
            }
        }
    }
}
