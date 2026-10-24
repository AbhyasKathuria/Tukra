package com.xsparks.tukra.core.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tukra_session")

class TokenStore(private val context: Context) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_CAMPUS_ID = stringPreferencesKey("campus_id")
        private val KEY_OUTLET_ID = stringPreferencesKey("outlet_id")
        private val KEY_BASE_URL = stringPreferencesKey("custom_base_url")
    }

    val baseUrlFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_BASE_URL]
    }

    suspend fun getBaseUrl(): String? {
        return context.dataStore.data.first()[KEY_BASE_URL]
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = url
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOKEN]
    }

    val roleFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ROLE]
    }

    val userFlow: Flow<UserDto?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        val email = prefs[KEY_USER_EMAIL] ?: ""
        val name = prefs[KEY_USER_NAME] ?: ""
        val role = prefs[KEY_USER_ROLE] ?: "STUDENT"
        val campusId = prefs[KEY_CAMPUS_ID]
        val outletId = prefs[KEY_OUTLET_ID]
        UserDto(
            id = id,
            email = email,
            name = name,
            role = role,
            campusId = campusId,
            outletId = outletId
        )
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[KEY_TOKEN]
    }

    suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_ROLE] = user.role
            user.campusId?.let { prefs[KEY_CAMPUS_ID] = it }
            user.outletId?.let { prefs[KEY_OUTLET_ID] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
