package com.example.teacherd

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class SettingPreference(private val context: Context) {

    private val API_KEY = stringPreferencesKey("api_key")
    private val SELECTED_MODEL = stringPreferencesKey("selected_model")

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    fun getApiKey(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[API_KEY] ?: ""
        }
    }

    suspend fun selectChatModel() {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = "deepseek-chat"
        }
    }

    suspend fun selectReasonerModel() {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = "deepseek-reasoner"
        }
    }

    fun getSelectedModel(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[SELECTED_MODEL] ?: "deepseek-chat"
        }
    }
}