package com.grass.android.data

import com.google.gson.annotations.SerializedName

object Login {
    data class Request(
        val username: String,
        val password: String
    )

    data class Response(
        @SerializedName("accessToken") val accessToken: String,
        @SerializedName("refreshToken") val refreshToken: String,
        @SerializedName("userId") val userId: String,
        @SerializedName("email") val email: String
    )

}
