package com.grass.android.network

import com.google.gson.Gson
import com.grass.android.GrassService
import com.grass.android.Logger
import com.grass.android.Ticker
import com.grass.android.data.RequestData
import com.grass.android.data.ResponseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
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
    CONNECTED, DISCONNECTED, DEAD
}

sealed interface WebSocketState {
    data class State(val status: Status) : WebSocketState
    data class Message(val message: String) : WebSocketState
}

class WebSocketFlow @Inject constructor(
    private val client: OkHttpClient,
    private val ticker: Ticker,
    private val logger: Logger,
    private val externalScope: CoroutineScope
) {
    private var job: Job? = null

    private var webSocket: WebSocket? = null

    private lateinit var listener: WebSocketListener

    private var retry = 0

    private var webSocketState = WebSocketState.State(Status.DEAD)

    private var messages = arrayOfNulls<String>(10)
    private var currentIndex: Int = 0

    private var scope: ProducerScope<WebSocketState>? = null

    val state = callbackFlow {
        scope = this
        val listener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                logger.log("onOpen", response.toString())
                sendStatus(scope, Status.CONNECTED)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.log("onMessage", text)
                val response = Gson().fromJson(text, ResponseData::class.java)
                val request = RequestData(response.id, response.action, response.result())
                val json = Gson().toJson(request)
                logger.log("send", json)
                webSocket.send(json)
                addMessage(json)
                addMessage(text)
                sendMessageFlow(this@callbackFlow)
                sendStatus(scope, Status.CONNECTED)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                logger.log("onClosing", "code: $code reason: $reason")
                webSocket.close(1000, null)
                sendStatus(scope, Status.DISCONNECTED)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.log("onFailure", t.localizedMessage ?: "Web socket error")
                logger.log("onFailure", response?.toString() ?: "")
                addMessage(t.localizedMessage ?: "")
                sendMessageFlow(this@callbackFlow)
                sendStatus(scope, Status.DISCONNECTED)
            }
        }

        attachListener(listener)

        awaitClose {

        }

    }.shareIn(
        scope = externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    init {
        setup()
    }

    private fun attachListener(listener: WebSocketListener) {
        this.listener = listener
        logger.log("attachListener", listener.toString())
        logger.log("webSocketFlow", this.toString())
    }

    private fun initialize() {
        logger.log("setup", "recreate web socket isInitialized: ${::listener.isInitialized}")
        if (::listener.isInitialized) {
            val request: Request =
                Request.Builder().url(WEBSOCKET_URLS[retry % WEBSOCKET_URLS.size]).build()
            webSocket = client.newWebSocket(request, listener)
            retry++
        }
    }

    fun setup() {
        job = externalScope.launch {
            ticker.schedule(DELAY, REFRESH_INTERVAL).collect {
                when (webSocketState.status) {
                    Status.CONNECTED -> {
                        ping()
                    }

                    Status.DISCONNECTED -> {
                        initialize()
                    }

                    else -> {}
                }
            }
        }
        initialize()
    }

    fun destroy() {
        logger.log("closeConnection", "closing web socket")
        ticker.cancel()
        job?.cancel()
        retry = 0
        sendStatus(scope, Status.DISCONNECTED)
        webSocket?.cancel()
    }

    private fun ping() {
        val json = JSONObject(
            mapOf(
                "id" to UUID.randomUUID().toString(), "version" to "1.0.0", "action" to "PING"
            )
        ).toString()
        logger.log("ping", json)
        webSocket?.send(json)
        addMessage(json)
        sendMessageFlow(scope)
    }

    private fun addMessage(text: String) {
        val size = messages.size
        val index = currentIndex % size
        currentIndex += 1
        messages[index] = text
    }

    private fun sendStatus(scope: ProducerScope<WebSocketState>?, status: Status) {
        logger.log("sendStatus", status.name)
        val state = WebSocketState.State(status)
        webSocketState = state
        GrassService.isConnected = status == Status.CONNECTED
        with(scope) {
            this?.trySend(state)
        }
    }

    private fun sendMessageFlow(scope: ProducerScope<WebSocketState>?) {
//        with(scope) {
//            this?.trySend(WebSocketState.Message(messages.joinToString("\n")))
//        }
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