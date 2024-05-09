package com.grass.android.network

import android.util.Log
import com.google.gson.Gson
import com.grass.android.ConductorEvents
import com.grass.android.Status
import com.grass.android.data.RequestData
import com.grass.android.data.ResponseData
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketListener(
    private val events: ConductorEvents
) : WebSocketListener() {
    private val TAG = "com.grass.android"

    override fun onOpen(webSocket: WebSocket, response: Response) {
//        Log.d(TAG, "onOpen $response")
        events.setMessage(response.message)
        events.setStatus(Status.CONNECTED)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val response = Gson().fromJson(text, ResponseData::class.java)
//        Log.d(TAG, "onMessage $response")
        events.setMessage(text)
        val request = RequestData(response.id, response.action, response.result())
        Log.d("SOCKET_REQUEST", response.result().toString())
        events.send(request)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//        Log.d(TAG, "onMessage " + bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
//        Log.d(TAG, "onClosing $code $reason\"")
        events.setStatus(Status.DISCONNECTED)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//        Log.d(TAG, "onFailure " + (t.localizedMessage ?: ""))
        events.setMessage(t.localizedMessage ?: "")
        events.setStatus(Status.DISCONNECTED)
    }
}