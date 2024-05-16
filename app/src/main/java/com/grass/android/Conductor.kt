package com.grass.android

import android.util.Log
import com.grass.android.network.WebSocketFlow
import com.grass.android.network.WebSocketState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Conductor @Inject constructor(
    private val webSocketFlow: WebSocketFlow
) {
    private val TAG = "Conductor"

    companion object {
//        var status = MutableLiveData(Status.DISCONNECTED)
//        var messages = MutableLiveData<SnapshotStateList<String>>()
//        var handler = Handler(Looper.getMainLooper())
//        var updateUI = true
    }

    init {
        Log.d(TAG, this.toString())
    }


    val state: Flow<WebSocketState> = webSocketFlow._state


//    override fun setStatus(value: Status) {
////        if (updateUI) {
////            handler.post {
////                status.value = value
////            }
////        }
//    }

//    override fun setMessage(value: String) {
//        Log.d(TAG, value)
////        if (value.isEmpty()) return
////        val size = _messages.size
////        if (size > 10) {
////            _messages.removeLast()
////        }
////        _messages.add(0, value)
////
////        if (updateUI) {
////            handler.post {
////                messages.postValue(_messages)
////            }
////        }
//    }
}