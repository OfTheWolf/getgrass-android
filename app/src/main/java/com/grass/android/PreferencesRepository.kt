package com.grass.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grass.android.data.Device
import com.grass.android.data.Login
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type
import javax.inject.Inject


class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user-data")
    private val LOGIN_DATA_KEY = stringPreferencesKey("logindata")
    private val DEVICES_DATA_KEY = stringPreferencesKey("devicedata")

    fun readData(): Flow<Login.Response?> {
        return context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[DEVICES_DATA_KEY]?.let {
                    val device =
                        Gson().fromJson(it, Device.Data::class.java)
                    LocalDataStore.saveDevicesData(device)
                }

                preferences[LOGIN_DATA_KEY]?.let {
                    val login = Gson().fromJson(it, Login.Response::class.java)
                    LocalDataStore.saveLoginData(login)
                    login
                }
            }
    }

    suspend fun saveLoginData(data: Login.Response) {
        LocalDataStore.saveLoginData(data)
        val json = Gson().toJson(data)
        context.dataStore.edit { settings ->
            settings[LOGIN_DATA_KEY] = json
        }
    }

    suspend fun saveDevicesData(data: List<Device.Data>) {
        LocalDataStore.saveDevicesData(data.first())
        val json = Gson().toJson(data.first())
        context.dataStore.edit { settings ->
            settings[DEVICES_DATA_KEY] = json
        }
    }
}