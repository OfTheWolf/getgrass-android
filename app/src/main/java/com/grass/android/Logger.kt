package com.grass.android

import android.util.Log

interface Logger {
    fun log(message: String)
    fun log(tagSuffix: String, message: String)
}

class DebugLogger(private val tag: String, private val tagSeparator: String = "::") : Logger {
    override fun log(message: String) {
        log(tag, message)
    }

    override fun log(tagSuffix: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$tag$tagSeparator$tagSuffix", message)
        }
    }
}