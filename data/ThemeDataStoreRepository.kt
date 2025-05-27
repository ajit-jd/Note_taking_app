package com.example.project7.data // Or your appropriate package

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeDataStoreRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
    }

    val themeSettingFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Default to "SYSTEM" if no preference is set
            preferences[PreferencesKeys.THEME_SETTING] ?: "SYSTEM" 
        }

    suspend fun saveThemeSetting(themeSetting: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_SETTING] = themeSetting
        }
    }
}
