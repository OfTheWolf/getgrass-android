package com.grass.android

import com.grass.android.data.Device
import com.grass.android.data.Login

object LocalDataStore {
    lateinit var userId: String
    lateinit var browserId: String
    var token: String? = null

    fun saveLoginData(data: Login.Response) {
        token = data.accessToken
        userId = data.userId
    }

    fun saveDevicesData(data: Device.Data) {
        browserId = data.deviceId
        userId = data.userId
    }
}
