package com.example.teacherd.chat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.teacherd.setting.SettingScreen
import com.example.teacherd.setting.SettingViewModel
import kotlinx.serialization.Serializable

@Serializable
object ChatScreen

@Serializable
object SettingScreen

@Composable
fun ChatRoot(
    viewModel: ChatViewModel,
    settingViewModel: SettingViewModel
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ChatScreen
    ) {
        composable<ChatScreen> {
            ChatScreen(viewModel, navController)
        }
        composable<SettingScreen> {
            SettingScreen(navController, settingViewModel)
        }
    }
}