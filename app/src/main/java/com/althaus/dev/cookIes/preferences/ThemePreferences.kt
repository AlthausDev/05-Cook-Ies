package com.althaus.dev.cookIes.preferences

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Configuración de DataStore
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class ThemePreferences(private val context: Context) {

    // Clave para guardar el tema (0 = Sistema, 1 = Claro, 2 = Oscuro)
    private val themeModeKey = intPreferencesKey("theme_mode")

    // Función para guardar el modo de tema
    suspend fun saveThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = mode
        }
    }

    // Flujo para observar el modo de tema actual
    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[themeModeKey] ?: 0 // Por defecto, "basado en el sistema"
    }
}
