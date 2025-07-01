package com.example.teacherd.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherd.SettingPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingPreference: SettingPreference
) : ViewModel() {

    private val _state = MutableStateFlow(SettingState())
    val state = combine(
        settingPreference.getApiKey(),
        settingPreference.getSelectedModel()
    ) { apiKey, model ->
        _state.value.copy(
            apiKey = apiKey,
            model = model
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingState())

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
}