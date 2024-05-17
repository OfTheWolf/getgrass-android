package com.grass.android.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.grass.android.InMemoryDataStore
import com.grass.android.data.Device
import com.grass.android.data.Login
import com.grass.android.data.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun readData(): Flow<UserData?> {
        return dataStore.data
            .map { preferences ->
                var userId: String? = null
                var deviceId: String? = null
                var email: String? = null
                var isLoggedIn = false

                preferences[DEVICES_DATA_KEY]?.let {
                    val device =
                        Gson().fromJson(it, Device.Data::class.java)
                    deviceId = device.deviceId
                    InMemoryDataStore.saveDevicesData(device)
                }

                preferences[LOGIN_DATA_KEY]?.let {
                    val login = Gson().fromJson(it, Login.Response::class.java)
                    InMemoryDataStore.saveLoginData(login)
                    userId = login.userId
                    email = login.email
                    isLoggedIn = login.accessToken.isNotEmpty()
                }

                UserData(userId, deviceId, email, isLoggedIn)
            }
    }

    suspend fun saveLoginData(data: Login.Response) {
        InMemoryDataStore.saveLoginData(data)
        val json = Gson().toJson(data)
        dataStore.edit { settings ->
            settings[LOGIN_DATA_KEY] = json
        }
    }

    suspend fun saveDevicesData(data: Device.Data) {
        InMemoryDataStore.saveDevicesData(data)
        val json = Gson().toJson(data)
        dataStore.edit { settings ->
            settings[DEVICES_DATA_KEY] = json
        }
    }

    companion object {
        private val LOGIN_DATA_KEY = stringPreferencesKey("logindata")
        private val DEVICES_DATA_KEY = stringPreferencesKey("devicedata")
    }
}