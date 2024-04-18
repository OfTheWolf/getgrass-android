package com.grass.android.network

import com.grass.android.data.BaseResponse
import com.grass.android.data.Device
import com.grass.android.data.Login
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST

interface GrassApiService {
    @POST("login")
    suspend fun login(@Body request: Login.Request): BaseResponse<Login.Response>

    @GET("devices?input=%7B%22limit%22:5%7D")
    suspend fun devices(): BaseResponse<Device.Response>
}