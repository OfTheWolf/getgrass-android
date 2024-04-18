package com.grass.android.data

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("result") val result: Result<T>
)

data class Result<T>(
    @SerializedName("data") val data: T
)
