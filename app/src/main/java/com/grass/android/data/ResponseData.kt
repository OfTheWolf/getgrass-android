package com.grass.android.data

import com.grass.android.LocalDataStore

data class ResponseData(
    val id: String,
    val version: String,
    val action: String,
    val data: Map<String, Any>?
) {
    fun result() = if (action == "AUTH") mapOf(
        "browser_id" to LocalDataStore.browserId,
        "user_id" to LocalDataStore.userId,
        "user_agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "timestamp" to System.currentTimeMillis()/1000L,
        "device_type" to "extension",
        "version" to "3.3.2",
    ) else null
}
