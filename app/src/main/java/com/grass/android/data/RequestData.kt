package com.grass.android.data

import com.google.gson.annotations.SerializedName

data class RequestData(
    val id: String,
    @SerializedName("origin_action") val action: String,
    val result: Map<String, Any>?
)
