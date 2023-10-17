package com.app.dicodingstoryapp.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map


class UserPreferences private constructor(private val dataStore: DataStore<Preferences>){
//    lateinit var userPreferences: UserPreferences

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        private val TOKEN = stringPreferencesKey("token")
        private val IS_LOGIN = booleanPreferencesKey("state")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

    fun getUser(): Flow<DataUser> {
        return dataStore.data.map {pref ->
            DataUser(
                pref[TOKEN] ?:"",
                pref[IS_LOGIN] ?: false
            )
        }
    }

    suspend fun saveUser(dataUser: DataUser) {
        dataStore.edit { pref ->
            pref[TOKEN] = dataUser.token
            pref[IS_LOGIN] = dataUser.isLogin
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { pref ->
            pref[TOKEN] = token
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val token = preferences[TOKEN]
            val isLogin = preferences[IS_LOGIN] ?: false
            token != null && isLogin
        }
    }



    suspend fun getToken(): String {
        val preferences = dataStore.data.firstOrNull()
        return preferences?.get(TOKEN) ?: ""
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}

