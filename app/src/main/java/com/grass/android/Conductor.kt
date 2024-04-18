package com.grass.android

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.grass.android.data.RequestData
import com.grass.android.network.WebSocketListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

enum class Status {
    CONNECTED, DISCONNECTED
}

class Conductor {
    private val TAG = "GrassService"
    private var webSocket: WebSocket? = null

    companion object {
        var status = MutableLiveData(Status.DISCONNECTED)
        var messages = MutableLiveData<SnapshotStateList<String>>()
        var handler = Handler(Looper.getMainLooper())
        var updateUI = true
    }

    private var _messages = mutableStateListOf<String>()

    private var listener = WebSocketListener(this)

    val logging = HttpLoggingInterceptor()

    private val client: OkHttpClient = OkHttpClient.Builder()
//        .addInterceptor(logging)
        .build()

    private val WEBSOCKET_URLS = arrayOf(
        "wss://proxy.wynd.network:4650",
        "wss://proxy.wynd.network:4444",
    )

    private var retry = 0
    private val timer = Timer()

    init {
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
//                Log.d(TAG, "timer fired")
                if (status.value == Status.CONNECTED) {
                    ping()
                } else {
                    initialize()
                }
            }
        }, 1000L, 10000L)
    }

    fun close() {
        timer.cancel()
        retry = 0
        client.dispatcher.executorService.shutdown()
        _messages.clear()
        Handler(Looper.getMainLooper()).post {
            messages.postValue(_messages)
            status.value = Status.DISCONNECTED
        }
    }

    fun initialize() {
        val request: Request = Request.Builder()
            .url(WEBSOCKET_URLS[retry % WEBSOCKET_URLS.size])
            .build()
        webSocket = client.newWebSocket(request, listener)
        retry++
    }

    fun ping() {
        val json = JSONObject(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "version" to "1.0.0",
                "action" to "PING"
            )
        ).toString()
        webSocket?.send(json)
        setMessage(json)
//        Log.d(TAG, "PING $json")
    }

    fun send(requestData: RequestData) {
        val json = Gson().toJson(requestData)
//        Log.d(TAG, "REQUEST $json")
        webSocket?.send(json)
        setMessage(json)
    }

    fun setStatus(value: Status) {
//        if (updateUI) {
//            handler.post {
//                status.value = value
//            }
//        }
    }

    fun setMessage(value: String) {
        Log.d(TAG, value)
//        if (value.isEmpty()) return
//        val size = _messages.size
//        if (size > 10) {
//            _messages.removeLast()
//        }
//        _messages.add(0, value)
//
//        if (updateUI) {
//            handler.post {
//                messages.postValue(_messages)
//            }
//        }
    }
}