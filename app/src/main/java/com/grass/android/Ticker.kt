package com.grass.android

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.Timer
import java.util.TimerTask

class Ticker(private val timer: Timer) {
    fun schedule(delay: Long, interval: Long) = callbackFlow {
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    trySend(Unit)
                }
            }, delay, interval
        )
        awaitClose {
            this@Ticker.cancel()
        }
    }

    fun cancel() {
        timer.cancel()
    }
}

