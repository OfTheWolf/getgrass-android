package com.grass.android.network

import com.google.gson.Gson
import com.grass.android.Logger
import com.grass.android.Ticker
import com.grass.android.data.RequestData
import com.grass.android.data.ResponseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

enum class Status {
    CONNECTED, DISCONNECTED
}

sealed interface WebSocketState {
    data class State(val status: Status) : WebSocketState
    data class Message(val message: String) : WebSocketState
}

class WebSocketFlow @Inject constructor(
    private val client: OkHttpClient, private val ticker: Ticker, private val logger: Logger
) {
    private var job: Job? = null

    private var webSocket: WebSocket? = null

    private var listener: WebSocketListener? = null

    private var retry = 0

    private var webSocketState = WebSocketState.State(Status.DISCONNECTED)

    val _state = callbackFlow {
        val listener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                logger.log("onOpen", response.toString())
                val state = WebSocketState.State(Status.CONNECTED)
                webSocketState = state
                trySend(state)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.log("onMessage", text)
                val response = Gson().fromJson(text, ResponseData::class.java)
                val request = RequestData(response.id, response.action, response.result())
                val json = Gson().toJson(request)
                logger.log("send", json)
                webSocket.send(json)
                trySend(WebSocketState.Message(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                logger.log("onClosing", "code: $code reason: $reason")
                webSocket.close(1000, null)
                val state = WebSocketState.State(Status.DISCONNECTED)
                webSocketState = state
                trySend(state)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.log("onFailure", t.localizedMessage ?: "Web socket error")
                logger.log("onFailure", response?.toString() ?: "")
                trySend(WebSocketState.Message(t.localizedMessage ?: ""))
                val state = WebSocketState.State(Status.DISCONNECTED)
                webSocketState = state
                trySend(state)
            }
        }

        attachListener(listener)

        awaitClose {
//            closeConnection()
        }

    }.shareIn(
        scope = CoroutineScope(Dispatchers.IO),
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    init {
        job = CoroutineScope(Dispatchers.IO).launch {
            ticker.schedule(DELAY, REFRESH_INTERVAL).collect {
                if (webSocketState.status == Status.CONNECTED) {
                    ping()
                } else {
                    setup(listener)
                }
            }
        }
    }

    private fun attachListener(listener: WebSocketListener) {
        if (this.listener == null) {
            setup(listener)
        }
        this.listener = listener
        logger.log("attachListener", listener.toString())
    }

    private fun setup(listener: WebSocketListener?) {
        logger.log("setup", "recreate web socket retry: $retry")
        this.listener = listener
        listener?.let {
            val request: Request =
                Request.Builder().url(WEBSOCKET_URLS[retry % WEBSOCKET_URLS.size]).build()
            webSocket = client.newWebSocket(request, it)
            retry++
        }
    }

    fun closeConnection() {
        logger.log("closeConnection", "closing web socket")
        ticker.cancel()
        job?.cancel()
        retry = 0
        client.dispatcher.executorService.shutdown()
    }

    private fun ping() {
        val json = JSONObject(
            mapOf(
                "id" to UUID.randomUUID().toString(), "version" to "1.0.0", "action" to "PING"
            )
        ).toString()
        logger.log("ping", json)
        webSocket?.send(json)
    }

    companion object {
        private const val DELAY = 1000L
        private const val REFRESH_INTERVAL = 5000L
        private val WEBSOCKET_URLS = arrayOf(
            "wss://proxy.wynd.network:4650",
            "wss://proxy.wynd.network:4444",
        )
    }

}