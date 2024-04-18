package com.grass.android

import com.grass.android.data.Device
import com.grass.android.data.Login

object DataStore {
    lateinit var userId: String
    lateinit var browserId: String
    var token: String? = null

    fun storeLogin(data: Login.Response) {
        token = data.accessToken
        userId = data.userId
    }

    fun storeDevices(data: List<Device.Data>) {
        browserId = data.first().deviceId
    }
}