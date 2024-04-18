package com.grass.android.data

import com.google.gson.annotations.SerializedName

object Device {
    data class Request(
        val limit: Int
    )

    data class Response(
        @SerializedName("data") val data: List<Data>,
    )

    data class Data(
        @SerializedName("deviceId") val deviceId: String,
        @SerializedName("userId") val userId: String
    )

}